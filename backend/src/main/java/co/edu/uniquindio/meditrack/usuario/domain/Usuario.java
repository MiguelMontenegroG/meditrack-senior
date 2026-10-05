package co.edu.uniquindio.meditrack.usuario.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

import java.time.Instant;

/**
 * Usuario del sistema (modelo de dominio, sin dependencias de Spring ni JPA).
 *
 * <p>Concentra las reglas de negocio del usuario: activacion/desactivacion y
 * actualizacion de datos. El hash de la contrasena nunca se expone fuera del
 * dominio ni se registra en logs.</p>
 */
public class Usuario {

    private final Long id;
    private final Correo correo;
    private String nombreCompleto;
    private String telefono;
    private String hashContrasena;
    private Rol rol;
    private boolean activo;
    private Instant ultimoAccesoEn;
    private Instant creadoEn;
    private Instant actualizadoEn;

    public Usuario(
            Long id,
            Correo correo,
            String nombreCompleto,
            String telefono,
            String hashContrasena,
            Rol rol,
            boolean activo,
            Instant ultimoAccesoEn,
            Instant creadoEn,
            Instant actualizadoEn) {
        this.id = id;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.activo = activo;
        this.ultimoAccesoEn = ultimoAccesoEn;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    /**
     * Crea un usuario nuevo (sin id aun) listo para persistir.
     */
    public static Usuario nuevo(
            Correo correo,
            String nombreCompleto,
            String telefono,
            String hashContrasena,
            Rol rol) {
        return new Usuario(
                null, correo, nombreCompleto, telefono, hashContrasena, rol, true, null, null, null);
    }

    /**
     * Marca el momento del ultimo acceso (columna ultimo_acceso_en).
     */
    public void registrarAcceso() {
        this.ultimoAccesoEn = Instant.now();
    }

    /**
     * Actualiza datos basicos editables. El rol se cambia con {@link #asignarRol(Rol)}.
     */
    public void actualizarDatos(String nombreCompleto, String telefono) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new ConflictoDeNegocioException("El nombre completo es obligatorio");
        }
        this.nombreCompleto = nombreCompleto.trim();
        this.telefono = telefono;
    }

    /**
     * Cambia el rol del usuario.
     */
    public void asignarRol(Rol nuevoRol) {
        if (nuevoRol == null) {
            throw new ConflictoDeNegocioException("El rol es obligatorio");
        }
        this.rol = nuevoRol;
    }

    /**
     * Actualiza el hash de la contrasena (ya cifrado por el puerto correspondiente).
     */
    public void actualizarHashContrasena(String nuevoHash) {
        if (nuevoHash == null || nuevoHash.isBlank()) {
            throw new ConflictoDeNegocioException("La contrasena no puede estar vacia");
        }
        this.hashContrasena = nuevoHash;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public boolean esAdministrador() {
        return rol != null && rol.getNombre() == RolNombre.ADMINISTRADOR;
    }

    public Long getId() {
        return id;
    }

    public Correo getCorreo() {
        return correo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getUltimoAccesoEn() {
        return ultimoAccesoEn;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }
}
