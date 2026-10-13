package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: activar o desactivar un medicamento (solo administrador).
 *
 * <p>Borrado logico: el medicamento nunca se elimina fisicamente.</p>
 */
public class CambiarEstadoMedicamentoUseCase {

    private final MedicamentoRepositoryPort medicamentos;
    private final AuditoriaPort auditoria;

    public CambiarEstadoMedicamentoUseCase(MedicamentoRepositoryPort medicamentos, AuditoriaPort auditoria) {
        this.medicamentos = medicamentos;
        this.auditoria = auditoria;
    }

    public Medicamento ejecutar(Long id, boolean activo, Long autorId) {
        Medicamento medicamento = medicamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado"));

        if (activo) {
            medicamento.activar();
        } else {
            medicamento.desactivar();
        }
        Medicamento persistido = medicamentos.guardar(medicamento);

        auditoria.registrar(autorId, AccionAuditoria.UPDATE, "catalogo_medicamento",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Cambio de estado de medicamento");

        return persistido;
    }
}
