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

import java.util.List;

/**
 * Caso de uso: crear un esquema de medicacion de un residente (solo
 * administrador), con sus horarios en la misma operacion.
 *
 * <p>Reglas: el residente debe ser visible y estar activo; el medicamento debe
 * existir y estar activo; los horarios deben ser coherentes con la frecuencia
 * (ver {@link EsquemaMedicacion}). Si ya existe un esquema activo del mismo
 * medicamento con vigencia solapada, se responde 409 (decision documentada).</p>
 */
public class CrearEsquemaUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final MedicamentoRepositoryPort medicamentos;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public CrearEsquemaUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            MedicamentoRepositoryPort medicamentos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.esquemas = esquemas;
        this.medicamentos = medicamentos;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public EsquemaMedicacion ejecutar(Long pacienteId, ComandoEsquema comando, AlcanceMedicacion autor) {
        alcance.exigirVisibleYActivo(pacienteId, autor);

        if (comando.medicamentoId() == null
                || medicamentos.buscarPorId(comando.medicamentoId())
                        .filter(m -> m.isActivo())
                        .isEmpty()) {
            throw new RecursoNoEncontradoException("Medicamento no encontrado o inactivo");
        }

        List<HorarioMedicacion> horarios = comando.horarios() == null ? List.of()
                : comando.horarios().stream().map(HorarioMedicacion::de).toList();

        EsquemaMedicacion esquema = EsquemaMedicacion.nuevo(
                pacienteId,
                comando.medicamentoId(),
                comando.dosis(),
                comando.frecuenciaHoras(),
                comando.via(),
                comando.indicaciones(),
                comando.fechaInicio(),
                comando.fechaFin(),
                horarios);

        if (esquemas.existeSolapamiento(
                pacienteId, comando.medicamentoId(), comando.fechaInicio(), comando.fechaFin(), null)) {
            throw new ConflictoDeNegocioException(
                    "Ya existe un esquema activo del mismo medicamento con vigencia solapada");
        }

        EsquemaMedicacion persistido = esquemas.guardar(esquema);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.CREATE, "esquema_medicacion",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Creacion de esquema de medicacion");

        return persistido;
    }
}
