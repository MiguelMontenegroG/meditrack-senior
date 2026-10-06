package co.edu.uniquindio.meditrack.paciente.domain;

import java.time.Instant;

/**
 * Vinculacion entre un usuario familiar y un paciente (modelo de dominio,
 * sin Spring ni JPA). Corresponde a la tabla {@code vinculacion_familiar}.
 *
 * <p>Revocar una vinculacion no borra la fila: pone {@code autorizado = false}.
 * Si el familiar se vuelve a vincular, se reactiva el registro existente, de
 * modo que se respeta el indice unico {@code uq_vincfam_usuario_paciente}.</p>
 */
public class VinculacionFamiliar {

    private final Long id;
    private final Long usuarioId;
    private final Long pacienteId;
    private String parentesco;
    private boolean autorizado;
    private Instant creadoEn;

    public VinculacionFamiliar(
            Long id,
            Long usuarioId,
            Long pacienteId,
            String parentesco,
            boolean autorizado,
            Instant creadoEn) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.pacienteId = pacienteId;
        this.parentesco = parentesco;
        this.autorizado = autorizado;
        this.creadoEn = creadoEn;
    }

    /**
     * Crea una vinculacion nueva (sin id aun) ya autorizada.
     */
    public static VinculacionFamiliar nueva(Long usuarioId, Long pacienteId, String parentesco) {
        return new VinculacionFamiliar(null, usuarioId, pacienteId, parentesco, true, null);
    }

    /**
     * Reactiva una vinculacion existente que estaba revocada.
     */
    public void autorizar(String parentesco) {
        this.autorizado = true;
        this.parentesco = parentesco;
    }

    /**
     * Revoca el acceso del familiar al paciente.
     */
    public void revocar() {
        this.autorizado = false;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getParentesco() {
        return parentesco;
    }

    public boolean isAutorizado() {
        return autorizado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
