package co.edu.uniquindio.meditrack.reporte.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.infrastructure.persistence.AlertaEntity;
import co.edu.uniquindio.meditrack.bitacora.infrastructure.persistence.RegistroSignosVitalesEntity;
import co.edu.uniquindio.meditrack.cita.infrastructure.persistence.CitaEntity;
import co.edu.uniquindio.meditrack.cita.infrastructure.persistence.EspecialidadJpaRepository;
import co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence.EsquemaMedicacionEntity;
import co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence.HorarioMedicacionEntity;
import co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence.RegistroTomaEntity;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.reporte.application.port.DatosReportePort;
import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte;
import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptador de solo lectura que recolecta el contenido de un reporte clinico.
 *
 * <p>Compone la cabecera del residente (via {@link PacienteRepositoryPort}, que ya
 * resolvio el alcance el caso de uso) con las secciones del {@link TipoReporte}
 * usando consultas de lectura propias ({@link DatosReporteJpaRepository}) y el
 * catalogo de especialidades. No modifica datos ni usa los adaptadores de
 * escritura de otros modulos.</p>
 *
 * <p>Todos los textos que viajan al PDF se calculan aqui: el generador de PDF no
 * consulta la base de datos.</p>
 */
@Component
public class DatosReporteAdapter implements DatosReportePort {

    private final DatosReporteJpaRepository datos;
    private final PacienteRepositoryPort pacientes;
    private final EspecialidadJpaRepository especialidades;

    public DatosReporteAdapter(
            DatosReporteJpaRepository datos,
            PacienteRepositoryPort pacientes,
            EspecialidadJpaRepository especialidades) {
        this.datos = datos;
        this.pacientes = pacientes;
        this.especialidades = especialidades;
    }

    @Override
    @Transactional(readOnly = true)
    public ContenidoReporte recolectar(
            Long pacienteId,
            TipoReporte tipo,
            Instant desde,
            Instant hasta,
            Instant generadoEn,
            String autor) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Residente no encontrado"));

        SeccionReporte.Cabecera cabecera = cabecera(paciente, generadoEn);
        List<SeccionReporte.FilaMedicacion> medicacion = tipo.incluyeMedicacion()
                ? medicacion(pacienteId, desde, hasta)
                : List.of();
        List<SeccionReporte.FilaCita> citas = tipo.incluyeCitas()
                ? citas(pacienteId, desde, hasta)
                : List.of();
        List<SeccionReporte.FilaBitacora> bitacora = tipo.incluyeBitacora()
                ? bitacora(pacienteId, desde, hasta)
                : List.of();
        List<SeccionReporte.FilaAlerta> alertas = tipo.incluyeAlertas()
                ? alertas(pacienteId, desde, hasta)
                : List.of();

        return new ContenidoReporte(
                tipo, desde, hasta, generadoEn, cabecera, medicacion, citas, bitacora, alertas);
    }

    private SeccionReporte.Cabecera cabecera(Paciente paciente, Instant generadoEn) {
        int edad = edad(paciente.getFechaNacimiento(), generadoEn);
        return new SeccionReporte.Cabecera(
                paciente.getNombres() + " " + paciente.getApellidos(),
                paciente.getDocumento(),
                edad,
                paciente.getSexo().name(),
                paciente.getHabitacion(),
                paciente.getSituacionClinica(),
                paciente.getFechaIngreso(),
                paciente.isActivo());
    }

    private static int edad(LocalDate nacimiento, Instant referencia) {
        LocalDate hoy = referencia.atZone(java.time.ZoneId.of("America/Bogota")).toLocalDate();
        return Period.between(nacimiento, hoy).getYears();
    }

    private List<SeccionReporte.FilaMedicacion> medicacion(
            Long pacienteId, Instant desde, Instant hasta) {
        Map<Long, long[]> tomasPorEsquema = new HashMap<>();
        for (RegistroTomaEntity toma : datos.tomasEnRango(pacienteId, desde, hasta)) {
            long[] contadores = tomasPorEsquema.computeIfAbsent(toma.getEsquemaId(), k -> new long[2]);
            if (toma.getEstado() == co.edu.uniquindio.meditrack.medicacion.domain.EstadoToma.ADMINISTRADA) {
                contadores[0]++;
            } else {
                contadores[1]++;
            }
        }

        LocalDate hoy = desde.atZone(java.time.ZoneId.of("America/Bogota")).toLocalDate();
        List<SeccionReporte.FilaMedicacion> filas = new ArrayList<>();
        for (EsquemaMedicacionEntity esquema : datos.esquemasDelPaciente(pacienteId)) {
            long[] contadores = tomasPorEsquema.getOrDefault(esquema.getId(), new long[2]);
            String medicamento = Optional.ofNullable(datos.nombreMedicamento(esquema.getMedicamentoId()))
                    .orElse("Medicamento #" + esquema.getMedicamentoId());
            filas.add(new SeccionReporte.FilaMedicacion(
                    medicamento,
                    esquema.getDosis(),
                    esquema.getVia().name(),
                    esquema.getFrecuenciaHoras(),
                    esquema.getIndicaciones(),
                    esquema.getFechaInicio(),
                    esquema.getFechaFin(),
                    esquema.isActivo(),
                    vigencia(esquema, hoy),
                    contadores[0],
                    contadores[1]));
        }
        return filas;
    }

    private static String vigencia(EsquemaMedicacionEntity esquema, LocalDate hoy) {
        if (!esquema.isActivo()) {
            return "INACTIVO";
        }
        if (esquema.getFechaInicio().isAfter(hoy)) {
            return "NO_VIGENTE";
        }
        if (esquema.getFechaFin() != null && esquema.getFechaFin().isBefore(hoy)) {
            return "NO_VIGENTE";
        }
        return "VIGENTE";
    }

    private List<SeccionReporte.FilaCita> citas(Long pacienteId, Instant desde, Instant hasta) {
        List<SeccionReporte.FilaCita> filas = new ArrayList<>();
        for (CitaEntity cita : datos.citasEnRango(pacienteId, desde, hasta)) {
            String especialidad = cita.getEspecialidadId() == null
                    ? "Visita general"
                    : especialidades.findById(cita.getEspecialidadId())
                            .map(e -> e.getNombre())
                            .orElse("Especialidad #" + cita.getEspecialidadId());
            filas.add(new SeccionReporte.FilaCita(
                    cita.getFechaHora(),
                    especialidad,
                    cita.getProfesional(),
                    cita.getLugar(),
                    cita.getMotivo(),
                    cita.getEstado().name()));
        }
        return filas;
    }

    private List<SeccionReporte.FilaBitacora> bitacora(Long pacienteId, Instant desde, Instant hasta) {
        List<SeccionReporte.FilaBitacora> filas = new ArrayList<>();
        for (RegistroSignosVitalesEntity registro : datos.bitacoraEnRango(pacienteId, desde, hasta)) {
            filas.add(new SeccionReporte.FilaBitacora(
                    registro.getFechaHora(),
                    registro.getPresionSistolica(),
                    registro.getPresionDiastolica(),
                    registro.getGlucosaMgDl(),
                    registro.getTemperaturaC(),
                    registro.getObservaciones()));
        }
        return filas;
    }

    private List<SeccionReporte.FilaAlerta> alertas(Long pacienteId, Instant desde, Instant hasta) {
        List<SeccionReporte.FilaAlerta> filas = new ArrayList<>();
        for (AlertaEntity alerta : datos.alertasEnRango(pacienteId, desde, hasta)) {
            filas.add(new SeccionReporte.FilaAlerta(
                    alerta.getCreadoEn(),
                    alerta.getTipo().name(),
                    alerta.getSeveridad().name(),
                    alerta.getEstado().name(),
                    alerta.getMensaje()));
        }
        return filas;
    }
}