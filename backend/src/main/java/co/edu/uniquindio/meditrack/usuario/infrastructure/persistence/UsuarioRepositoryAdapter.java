package co.edu.uniquindio.meditrack.usuario.infrastructure.persistence;

import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;
import co.edu.uniquindio.meditrack.usuario.domain.Correo;
import co.edu.uniquindio.meditrack.usuario.domain.Rol;
import co.edu.uniquindio.meditrack.usuario.domain.RolNombre;
import co.edu.uniquindio.meditrack.usuario.domain.Usuario;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de usuarios: implementa el puerto de aplicacion
 * sobre el repositorio Spring Data y traduce entre entidad JPA y modelo de dominio.
 */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repositorio;
    private final RolJpaRepository roles;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository repositorio, RolJpaRepository roles) {
        this.repositorio = repositorio;
        this.roles = roles;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entidad = usuario.getId() == null
                ? new UsuarioEntity()
                : repositorio.findById(usuario.getId()).orElseGet(UsuarioEntity::new);

        entidad.setNombreCompleto(usuario.getNombreCompleto());
        entidad.setCorreo(usuario.getCorreo().valor());
        entidad.setHashContrasena(usuario.getHashContrasena());
        entidad.setTelefono(usuario.getTelefono());
        entidad.setActivo(usuario.isActivo());
        entidad.setUltimoAccesoEn(usuario.getUltimoAccesoEn());
        entidad.setRol(roles.findByNombre(usuario.getRol().getNombre()).orElseThrow());

        return aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repositorio.findById(id).map(this::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(Correo correo) {
        return repositorio.buscarPorCorreo(correo.valor()).map(this::aDominio);
    }

    @Override
    public boolean existePorCorreo(Correo correo) {
        return repositorio.existePorCorreo(correo.valor());
    }

    @Override
    public List<Usuario> listar(RolNombre rol, Boolean activo, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "id"));
        return repositorio.listar(rol, activo, pageable).getContent().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public long contar(RolNombre rol, Boolean activo) {
        return repositorio.contar(rol, activo);
    }

    @Override
    public long contarAdministradoresActivos() {
        return repositorio.contarAdministradoresActivos();
    }

    private Usuario aDominio(UsuarioEntity entidad) {
        Rol rol = new Rol(
                entidad.getRol().getId(),
                entidad.getRol().getNombre(),
                entidad.getRol().getDescripcion());
        return new Usuario(
                entidad.getId(),
                Correo.de(entidad.getCorreo()),
                entidad.getNombreCompleto(),
                entidad.getTelefono(),
                entidad.getHashContrasena(),
                rol,
                entidad.isActivo(),
                entidad.getUltimoAccesoEn(),
                entidad.getCreadoEn(),
                entidad.getActualizadoEn());
    }
}
