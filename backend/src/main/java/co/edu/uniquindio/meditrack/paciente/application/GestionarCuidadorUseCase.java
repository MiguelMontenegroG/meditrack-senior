package co.edu.uniquindio.meditrack.paciente.application;

import co.edu.uniquindio.meditrack.paciente.application.port.AsignacionCuidadorRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.AsignacionCuidador;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.RecursoNoEncontradoException;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso: gestionar la asignacion de un cuidador a un paciente
 * (solo administrador).
 *
 * <p>Reglas: el usuario debe existir, estar activo y tener rol
 * {@code CUIDADOR_ENFERMERO}; no se asigna a un paciente inactivo; 409 si ya
 * tiene una asignacion vigente con ese paciente. Desasignar cierra la asignacion
 * poniendo {@code hasta = hoy}.</p>
 */
public class GestionarCuidadorUseCase {

    private final PacienteRepositoryPort pacientes;
    private final AsignacionCuidadorRepositoryPort asignaciones;
    private final UsuarioRepositoryPort usuarios;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public GestionarCuidadorUseCase(
            PacienteRepositoryPort pacientes,
            AsignacionCuidadorRepositoryPort asignaciones,
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria,
            Clock reloj) {
        this.pacientes = pacientes;
        this.asignaciones = asignaciones;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    public List<AsignacionCuidador> listar(Long pacienteId) {
        return asignaciones.listarVigentesPorPaciente(pacienteId);
    }

    public AsignacionCuidador asignar(Long pacienteId, Long usuarioId, Long autorId) {
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
        if (!paciente.isActivo()) {
            throw new ConflictoDeNegocioException("No se puede asignar cuidadores a un paciente inactivo");
        }

        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!usuario.isActivo()) {
            throw new ConflictoDeNegocioException("El usuario no esta activo");
        }
        if (usuario.getRol().getNombre() != RolNombre.CUIDADOR_ENFERMERO) {
            throw new ConflictoDeNegocioException("El usuario no tiene rol CUIDADOR_ENFERMERO");
        }
        if (asignaciones.buscarVigente(usuarioId, pacienteId).isPresent()) {
            throw new ConflictoDeNegocioException("El cuidador ya tiene una asignacion vigente con este paciente");
        }

        AsignacionCuidador asignacion = AsignacionCuidador.nueva(usuarioId, pacienteId, LocalDate.now(reloj));
        AsignacionCuidador persistida = asignaciones.guardar(asignacion);

        auditoria.registrar(autorId, AccionAuditoria.CREATE, "asignacion_cuidador",
                persistida.getId(), ResultadoAuditoria.EXITOSO, "Asignacion de cuidador");

        return persistida;
    }

    public void desasignar(Long pacienteId, Long usuarioId, Long autorId) {
        AsignacionCuidador asignacion = asignaciones.buscarVigente(usuarioId, pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignacion vigente no encontrada"));

        asignacion.cerrar(LocalDate.now(reloj));
        asignaciones.guardar(asignacion);

        auditoria.registrar(autorId, AccionAuditoria.DELETE, "asignacion_cuidador",
                asignacion.getId(), ResultadoAuditoria.EXITOSO, "Desasignacion de cuidador");
    }
}
