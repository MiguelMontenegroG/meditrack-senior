package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;

import java.util.Optional;

/**
 * Caso de uso: crear, actualizar y eliminar contactos de emergencia
 * (solo administrador).
 *
 * <p>Regla: como maximo un contacto principal por paciente. Al marcar uno como
 * principal, el anterior deja de serlo en la misma transaccion.</p>
 */
public class GestionarContactoUseCase {

    private final PacienteRepositoryPort pacientes;
    private final ContactoEmergenciaRepositoryPort contactos;
    private final AuditoriaPort auditoria;

    public GestionarContactoUseCase(
            PacienteRepositoryPort pacientes,
            ContactoEmergenciaRepositoryPort contactos,
            AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.contactos = contactos;
        this.auditoria = auditoria;
    }

    public ContactoEmergencia crear(Long pacienteId, ComandoContacto comando, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        if (comando.principal()) {
            degradarPrincipal(paciente.getId());
        }

        ContactoEmergencia contacto = ContactoEmergencia.nuevo(
                paciente.getId(), comando.nombre(), comando.parentesco(), comando.telefono(), comando.principal());
        ContactoEmergencia persistido = contactos.guardar(contacto);

        auditoria.registrar(autorId, AccionAuditoria.CREATE, "contacto_emergencia",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Creacion de contacto de emergencia");

        return persistido;
    }

    public ContactoEmergencia actualizar(Long pacienteId, Long contactoId, ComandoContacto comando, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
        ContactoEmergencia contacto = contactos.buscarPorId(contactoId)
                .filter(c -> c.getPacienteId().equals(paciente.getId()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Contacto no encontrado"));

        if (comando.principal() && !contacto.isPrincipal()) {
            degradarPrincipal(paciente.getId());
            contacto.marcarPrincipal();
        }
        contacto.actualizar(comando.nombre(), comando.parentesco(), comando.telefono());

        ContactoEmergencia persistido = contactos.guardar(contacto);

        auditoria.registrar(autorId, AccionAuditoria.UPDATE, "contacto_emergencia",
                persistido.getId(), ResultadoAuditoria.EXITOSO, "Actualizacion de contacto de emergencia");

        return persistido;
    }

    public void eliminar(Long pacienteId, Long contactoId, Long autorId) {
        ContactoEmergencia contacto = contactos.buscarPorId(contactoId)
                .filter(c -> c.getPacienteId().equals(pacienteId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Contacto no encontrado"));

        contactos.eliminar(contacto.getId());

        auditoria.registrar(autorId, AccionAuditoria.DELETE, "contacto_emergencia",
                contacto.getId(), ResultadoAuditoria.EXITOSO, "Eliminacion de contacto de emergencia");
    }

    private void degradarPrincipal(Long pacienteId) {
        Optional<ContactoEmergencia> principal = contactos.buscarPrincipal(pacienteId);
        principal.ifPresent(c -> {
            c.quitarPrincipal();
            contactos.guardar(c);
        });
    }
}
