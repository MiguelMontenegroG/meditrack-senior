package co.edu.uniquindio.meditrack.medicacion.application.port;

import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;

/**
 * Puerto de consulta del alcance de un residente para el modulo de medicacion.
 *
 * <p>Desacopla medicacion de las entidades JPA de paciente: medicacion solo
 * pregunta "¿puede este usuario ver/operar sobre este residente?" y "¿esta el
 * residente activo?". La implementacion vive en infraestructura y reutiliza las
 * consultas de alcance del modulo paciente.</p>
 *
 * <p>Regla (igual que en paciente): ADMINISTRADOR ve todo residente existente;
 * CUIDADOR_ENFERMERO solo con asignacion vigente; FAMILIAR_AUTORIZADO solo con
 * vinculacion autorizada. Un residente inexistente o fuera de alcance responde
 * como no visible (404 en la capa web), nunca 403.</p>
 */
public interface PacienteAlcancePort {

    /** Indica si el residente existe. */
    boolean existe(Long pacienteId);

    /** Indica si el residente esta activo. */
    boolean estaActivo(Long pacienteId);

    /**
     * Decide la visibilidad del residente para el usuario segun su rol, sin pasar
     * el rol como parametro a la consulta (igual que el patron del modulo
     * paciente, para evitar el problema de tipado de Hibernate 6.6).
     */
    boolean visible(Long pacienteId, Long usuarioId, RolUsuario rol);
}
