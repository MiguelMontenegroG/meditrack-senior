package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

/**
 * Caso de uso: activar o desactivar un paciente (solo administrador).
 *
 * <p>Nunca se borra fisicamente: el borrado es logico con {@code activo}. La
 * desactivacion exige un motivo, que queda registrado en la auditoria; la
 * activacion exige que el perfil clinico este completo (regla del dominio).</p>
 */
public class CambiarEstadoPacienteUseCase {

    private final PacienteRepositoryPort pacientes;
    private final AuditoriaPort auditoria;

    public CambiarEstadoPacienteUseCase(PacienteRepositoryPort pacientes, AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.auditoria = auditoria;
    }

    /**
     * @param pacienteId residente a modificar
     * @param activar    {@code true} activa, {@code false} desactiva
     * @param motivo     obligatorio al desactivar; se ignora al activar
     * @param autorId    administrador que realiza el cambio (auditoria)
     */
    public Paciente ejecutar(Long pacienteId, boolean activar, String motivo, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        if (activar) {
            paciente.activar();
        } else {
            // El dominio rechaza un motivo vacio (400 VALIDACION); aqui solo se
            // normaliza para no guardar espacios sobrantes en la auditoria ni
            // romper con una referencia nula.
            motivo = motivo != null ? motivo.trim() : "";
            paciente.desactivar(motivo);
        }

        Paciente persistido = pacientes.guardar(paciente);

        String detalle = activar
                ? "Activacion de paciente"
                : "Desactivacion de paciente. Motivo: " + motivo;

        auditoria.registrar(
                autorId,
                AccionAuditoria.UPDATE,
                "paciente",
                persistido.getId(),
                ResultadoAuditoria.EXITOSO,
                detalle);

        return persistido;
    }
}
