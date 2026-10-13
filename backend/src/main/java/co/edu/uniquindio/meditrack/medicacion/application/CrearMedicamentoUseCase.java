package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

/**
 * Caso de uso: crear un medicamento en el catalogo (solo administrador).
 *
 * <p>Regla: el nombre es unico sin distinguir mayusculas (409 si se repite).</p>
 */
public class CrearMedicamentoUseCase {

    private final MedicamentoRepositoryPort medicamentos;
    private final AuditoriaPort auditoria;

    public CrearMedicamentoUseCase(MedicamentoRepositoryPort medicamentos, AuditoriaPort auditoria) {
        this.medicamentos = medicamentos;
        this.auditoria = auditoria;
    }

    public Medicamento ejecutar(ComandoMedicamento comando, Long autorId) {
        if (comando.nombre() == null || comando.nombre().isBlank()) {
            throw new ValidacionDeNegocioException("El nombre del medicamento es obligatorio");
        }
        if (medicamentos.existePorNombre(comando.nombre().trim())) {
            throw new ConflictoDeNegocioException("Ya existe un medicamento con ese nombre");
        }

        Medicamento persistido = medicamentos.guardar(
                Medicamento.nuevo(comando.nombre(), comando.presentacion()));

        auditoria.registrar(autorId, AccionAuditoria.CREATE, "catalogo_medicamento",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Creacion de medicamento");

        return persistido;
    }
}
