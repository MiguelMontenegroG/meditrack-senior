package co.edu.uniquindio.meditrack.shared.infrastructure.persistence;

import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de auditoria: inserta eventos en auditoria_acceso.
 *
 * <p>Usa una transaccion nueva para que el registro de un acceso denegado
 * (por ejemplo, un login fallido) no se pierda si la operacion principal falla.</p>
 */
@Component
public class AuditoriaRepositoryAdapter implements AuditoriaPort {

    private final AuditoriaJpaRepository repositorio;

    public AuditoriaRepositoryAdapter(AuditoriaJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            Long usuarioId,
            AccionAuditoria accion,
            String entidad,
            Long entidadId,
            ResultadoAuditoria resultado,
            String detalle) {
        repositorio.save(new AuditoriaEntity(usuarioId, accion, entidad, entidadId, resultado, detalle));
    }
}
