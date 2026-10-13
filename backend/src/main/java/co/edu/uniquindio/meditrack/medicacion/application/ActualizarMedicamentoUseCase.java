package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: actualizar un medicamento del catalogo (solo administrador).
 *
 * <p>El nombre sigue siendo unico sin distinguir mayusculas, excluyendo al
 * propio medicamento.</p>
 */
public class ActualizarMedicamentoUseCase {

    private final MedicamentoRepositoryPort medicamentos;
    private final AuditoriaPort auditoria;

    public ActualizarMedicamentoUseCase(MedicamentoRepositoryPort medicamentos, AuditoriaPort auditoria) {
        this.medicamentos = medicamentos;
        this.auditoria = auditoria;
    }

    public Medicamento ejecutar(Long id, ComandoMedicamento comando, Long autorId) {
        Medicamento medicamento = medicamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado"));

        if (comando.nombre() == null || comando.nombre().isBlank()) {
            throw new ConflictoDeNegocioException("El nombre del medicamento es obligatorio");
        }
        if (medicamentos.existePorNombreEnOtro(comando.nombre().trim(), id)) {
            throw new ConflictoDeNegocioException("Ya existe un medicamento con ese nombre");
        }

        medicamento.actualizar(comando.nombre(), comando.presentacion());
        Medicamento persistido = medicamentos.guardar(medicamento);

        auditoria.registrar(autorId, AccionAuditoria.UPDATE, "catalogo_medicamento",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Actualizacion de medicamento");

        return persistido;
    }
}
