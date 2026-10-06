package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.VinculacionFamiliarRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.VinculacionFamiliar;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

import java.util.List;

/**
 * Caso de uso: gestionar la vinculacion de un familiar a un paciente
 * (solo administrador).
 *
 * <p>Reglas: el usuario debe existir, estar activo y tener rol
 * {@code FAMILIAR_AUTORIZADO}; no se vincula a un paciente inactivo; 409 si ya
 * esta vinculado y autorizado. Revocar pone {@code autorizado = false}; revincular
 * reactiva el registro existente.</p>
 */
public class GestionarFamiliarUseCase {

    private final PacienteRepositoryPort pacientes;
    private final VinculacionFamiliarRepositoryPort vinculaciones;
    private final UsuarioRepositoryPort usuarios;
    private final AuditoriaPort auditoria;

    public GestionarFamiliarUseCase(
            PacienteRepositoryPort pacientes,
            VinculacionFamiliarRepositoryPort vinculaciones,
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria) {
        this.pacientes = pacientes;
        this.vinculaciones = vinculaciones;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    public List<VinculacionFamiliar> listar(Long pacienteId) {
        return vinculaciones.listarPorPaciente(pacienteId);
    }

    public VinculacionFamiliar vincular(Long pacienteId, Long usuarioId, String parentesco, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
        if (!paciente.isActivo()) {
            throw new ConflictoDeNegocioException("No se puede vincular familiares a un paciente inactivo");
        }

        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!usuario.isActivo()) {
            throw new ConflictoDeNegocioException("El usuario no esta activo");
        }
        if (usuario.getRol().getNombre() != RolNombre.FAMILIAR_AUTORIZADO) {
            throw new ConflictoDeNegocioException("El usuario no tiene rol FAMILIAR_AUTORIZADO");
        }

        VinculacionFamiliar existente = vinculaciones.buscarPorUsuarioYPaciente(usuarioId, pacienteId).orElse(null);
        if (existente != null && existente.isAutorizado()) {
            throw new ConflictoDeNegocioException("El familiar ya esta vinculado a este paciente");
        }

        VinculacionFamiliar persistida;
        if (existente != null) {
            existente.autorizar(parentesco);
            persistida = vinculaciones.guardar(existente);
        } else {
            persistida = vinculaciones.guardar(VinculacionFamiliar.nueva(usuarioId, pacienteId, parentesco));
        }

        auditoria.registrar(autorId, AccionAuditoria.CREATE, "vinculacion_familiar",
                persistida.getId(), ResultadoAuditoria.EXITOSO, "Vinculacion de familiar");

        return persistida;
    }

    public void revocar(Long pacienteId, Long usuarioId, Long autorId) {
        VinculacionFamiliar vinculacion = vinculaciones.buscarPorUsuarioYPaciente(usuarioId, pacienteId)
                .filter(VinculacionFamiliar::isAutorizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vinculacion no encontrada"));

        vinculacion.revocar();
        vinculaciones.guardar(vinculacion);

        auditoria.registrar(autorId, AccionAuditoria.DELETE, "vinculacion_familiar",
                vinculacion.getId(), ResultadoAuditoria.EXITOSO, "Revocacion de vinculacion de familiar");
    }
}
