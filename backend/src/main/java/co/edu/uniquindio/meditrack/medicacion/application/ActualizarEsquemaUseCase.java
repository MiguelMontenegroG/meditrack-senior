package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.HorarioMedicacion;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

import java.time.LocalTime;
import java.util.List;

/**
 * Caso de uso: actualizar un esquema de medicacion (solo administrador).
 *
 * <p>El medicamento no se cambia (si se requiere otro, se crea un esquema
 * nuevo). Se reemplazan los horarios del esquema. La deteccion de solapamiento
 * excluye al propio esquema.</p>
 */
public class ActualizarEsquemaUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final MedicamentoRepositoryPort medicamentos;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ActualizarEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            MedicamentoRepositoryPort medicamentos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.esquemas = esquemas;
        this.medicamentos = medicamentos;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public EsquemaMedicacion ejecutar(Long pacienteId, Long esquemaId, ComandoEsquema comando, AlcanceMedicacion autor) {
        alcance.exigirVisibleYActivo(pacienteId, autor);

        EsquemaMedicacion esquema = esquemas.buscarPorId(esquemaId)
                .filter(e -> e.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Esquema no encontrado"));

        if (medicamentos.buscarPorId(esquema.getMedicamentoId())
                .filter(m -> m.isActivo())
                .isEmpty()) {
            throw new ConflictoDeNegocioException("El medicamento del esquema esta inactivo");
        }

        List<HorarioMedicacion> horarios = comando.horarios() == null ? List.of()
                : comando.horarios().stream().map(HorarioMedicacion::de).toList();

        esquema.actualizar(
                comando.dosis(),
                comando.frecuenciaHoras(),
                comando.via(),
                comando.indicaciones(),
                comando.fechaInicio(),
                comando.fechaFin(),
                horarios);

        if (esquemas.existeSolapamiento(
                pacienteId, esquema.getMedicamentoId(), comando.fechaInicio(), comando.fechaFin(), esquemaId)) {
            throw new ConflictoDeNegocioException(
                    "Ya existe otro esquema activo del mismo medicamento con vigencia solapada");
        }

        EsquemaMedicacion persistido = esquemas.guardar(esquema);
        List<LocalTime> horas = comando.horarios() == null ? List.of() : List.copyOf(comando.horarios());
        esquemas.reemplazarHorarios(esquemaId, horas);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.UPDATE, "esquema_medicacion",
                esquemaId, ResultadoAuditoria.EXITOSO, "Actualizacion de esquema de medicacion");

        // Se recarga para devolver los horarios recien reemplazados.
        return esquemas.buscarPorId(esquemaId).orElse(persistido);
    }
}
