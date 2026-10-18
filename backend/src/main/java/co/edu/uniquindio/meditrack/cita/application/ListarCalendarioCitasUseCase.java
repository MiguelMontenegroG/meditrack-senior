package co.edu.uniquindio.meditrack.cita.application;

import co.edu.uniquindio.meditrack.cita.application.port.CitaRepositoryPort;
import co.edu.uniquindio.meditrack.cita.domain.AlcanceCita;
import co.edu.uniquindio.meditrack.cita.domain.EstadoCita;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: calendario de citas en un rango de fechas, paginado, filtrado de
 * forma opcional por un residente concreto y por el alcance del usuario.
 *
 * <p>El rango {@code desde}/{@code hasta} es obligatorio: el calendario siempre
 * se consulta por rango, nunca sin filtro de fecha. Un rango invertido responde
 * 400.</p>
 *
 * <p>Resolucion del alcance:</p>
 * <ul>
 *   <li>Si llega {@code pacienteId}, se resuelve la visibilidad de ese residente
 *   con {@link PacienteAlcancePort}. Si no es visible (inexistente o fuera de
 *   alcance) se responde una pagina <b>vacia</b> ({@code total = 0}) sin revelar
 *   su existencia ni consultar citas. Si es visible, se consultan solo sus
 *   citas.</li>
 *   <li>Si no llega {@code pacienteId}, se resuelven los ids de los residentes
 *   visibles con las consultas de alcance del modulo paciente (metodos separados
 *   por rol, igual que en los demas modulos) y se consultan solo las citas de
 *   esos residentes. Si no hay ninguno visible se responde pagina vacia sin
 *   consultar citas.</li>
 * </ul>
 *
 * <p>El {@code total} lo calcula el mismo predicado que el contenido (paginacion
 * y conteo coherentes). El filtro por residente es combinable con estado y rango
 * de fechas.</p>
 */
public class ListarCalendarioCitasUseCase {

    /** Tamano de pagina para enumerar los residentes visibles. */
    private static final int TAMANO_PAGINA = 500;

    private final CitaRepositoryPort citas;
    private final PacienteRepositoryPort pacientes;
    private final PacienteAlcancePort alcancePaciente;
    private final AuditoriaPort auditoria;

    public ListarCalendarioCitasUseCase(
            CitaRepositoryPort citas,
            PacienteRepositoryPort pacientes,
            PacienteAlcancePort alcancePaciente,
            AuditoriaPort auditoria) {
        this.citas = citas;
        this.pacientes = pacientes;
        this.alcancePaciente = alcancePaciente;
        this.auditoria = auditoria;
    }

    public PaginaCitasDominio ejecutar(
            Instant desde, Instant hasta, EstadoCita estado, Long pacienteId,
            int pagina, int tamano, AlcanceCita autor) {
        if (desde == null || hasta == null) {
            throw new ValidacionDeNegocioException("El rango del calendario exige 'desde' y 'hasta'");
        }
        if (desde.isAfter(hasta)) {
            throw new ValidacionDeNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        if (pacienteId != null) {
            return porResidente(desde, hasta, estado, pacienteId, pagina, tamano, autor);
        }
        return porConjuntoVisible(desde, hasta, estado, pagina, tamano, autor);
    }

    /**
     * Listado filtrado a un residente concreto. Si el residente no es visible se
     * responde pagina vacia sin revelar su existencia.
     */
    private PaginaCitasDominio porResidente(
            Instant desde, Instant hasta, EstadoCita estado, Long pacienteId,
            int pagina, int tamano, AlcanceCita autor) {
        if (!alcancePaciente.visible(pacienteId, autor.usuarioId(), aRolUsuario(autor))) {
            auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "cita",
                    pacienteId, ResultadoAuditoria.EXITOSO,
                    "Calendario de citas sin residente visible");
            return new PaginaCitasDominio(List.of(), 0L);
        }

        PaginaCitasDominio resultado =
                citas.listarPorPaciente(pacienteId, estado, desde, hasta, pagina, tamano);
        auditarCalendario(autor);
        return resultado;
    }

    /** Listado del conjunto de residentes visibles para el usuario. */
    private PaginaCitasDominio porConjuntoVisible(
            Instant desde, Instant hasta, EstadoCita estado,
            int pagina, int tamano, AlcanceCita autor) {
        List<Long> visibles = pacientesVisibles(autor);
        if (visibles.isEmpty()) {
            auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "cita",
                    null, ResultadoAuditoria.EXITOSO, "Calendario de citas sin residentes visibles");
            return new PaginaCitasDominio(List.of(), 0L);
        }

        PaginaCitasDominio resultado =
                citas.listarPorPacientes(visibles, estado, desde, hasta, pagina, tamano);
        auditarCalendario(autor);
        return resultado;
    }

    private void auditarCalendario(AlcanceCita autor) {
        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "cita",
                null, ResultadoAuditoria.EXITOSO, "Calendario de citas");
    }

    /**
     * Ids de residentes visibles para el usuario. Se pagina para no cargar en
     * memoria una lista arbitrariamente grande de una sola vez y para no usar un
     * tamano de pagina desproporcionado.
     */
    private List<Long> pacientesVisibles(AlcanceCita autor) {
        List<Long> ids = new ArrayList<>();
        int pagina = 0;
        List<Paciente> lote;
        do {
            lote = listarLote(autor, pagina);
            lote.forEach(p -> ids.add(p.getId()));
            pagina++;
        } while (lote.size() == TAMANO_PAGINA);
        return ids;
    }

    private List<Paciente> listarLote(AlcanceCita autor, int pagina) {
        return switch (autor.rol()) {
            case ADMINISTRADOR -> pacientes.listarTodos(null, null, pagina, TAMANO_PAGINA);
            case CUIDADOR_ENFERMERO -> pacientes.listarPorCuidador(
                    null, null, autor.usuarioId(), pagina, TAMANO_PAGINA);
            case FAMILIAR_AUTORIZADO -> pacientes.listarPorFamiliar(
                    null, null, autor.usuarioId(), pagina, TAMANO_PAGINA);
        };
    }

    private static RolUsuario aRolUsuario(AlcanceCita alcance) {
        return RolUsuario.valueOf(alcance.rol().name());
    }
}