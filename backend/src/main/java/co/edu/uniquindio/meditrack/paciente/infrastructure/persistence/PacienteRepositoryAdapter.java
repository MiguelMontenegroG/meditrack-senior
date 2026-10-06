package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.paciente.domain.RolUsuario;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de pacientes: implementa el puerto de aplicacion
 * sobre el repositorio Spring Data y traduce entidad JPA a modelo de dominio.
 *
 * <p>La paginacion y el filtrado por alcance se delegan a la consulta; nunca se
 * cargan todos los pacientes en memoria en la capa de aplicacion.</p>
 */
@Component
public class PacienteRepositoryAdapter implements PacienteRepositoryPort {

    private final PacienteJpaRepository repositorio;

    public PacienteRepositoryAdapter(PacienteJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Paciente guardar(Paciente paciente) {
        PacienteEntity entidad = paciente.getId() == null
                ? new PacienteEntity()
                : repositorio.findById(paciente.getId()).orElseGet(PacienteEntity::new);

        entidad.setDocumento(paciente.getDocumento());
        entidad.setNombres(paciente.getNombres());
        entidad.setApellidos(paciente.getApellidos());
        entidad.setFechaNacimiento(paciente.getFechaNacimiento());
        entidad.setSexo(paciente.getSexo());
        entidad.setHabitacion(paciente.getHabitacion());
        entidad.setSituacionClinica(paciente.getSituacionClinica());
        entidad.setFechaIngreso(paciente.getFechaIngreso());
        entidad.setActivo(paciente.isActivo());

        return aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return repositorio.findById(id).map(this::aDominio);
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return repositorio.existsByDocumento(documento);
    }

    @Override
    public boolean existePorDocumentoEnOtro(String documento, Long idExcluido) {
        return repositorio.existsByDocumentoAndIdNot(documento, idExcluido);
    }

    @Override
    public List<Paciente> listar(String texto, Boolean activo, RolUsuario rol, Long usuarioId, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "id"));
        return repositorio.listar(textoVacioANulo(texto), activo, rol, usuarioId, pageable).getContent().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public long contar(String texto, Boolean activo, RolUsuario rol, Long usuarioId) {
        return repositorio.contar(textoVacioANulo(texto), activo, rol, usuarioId);
    }

    @Override
    public boolean esVisiblePara(Long pacienteId, RolUsuario rol, Long usuarioId) {
        return repositorio.esVisiblePara(pacienteId, rol, usuarioId);
    }

    private String textoVacioANulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private Paciente aDominio(PacienteEntity entidad) {
        return new Paciente(
                entidad.getId(),
                entidad.getDocumento(),
                entidad.getNombres(),
                entidad.getApellidos(),
                entidad.getFechaNacimiento(),
                entidad.getSexo(),
                entidad.getHabitacion(),
                entidad.getSituacionClinica(),
                entidad.getFechaIngreso(),
                entidad.isActivo(),
                entidad.getCreadoEn(),
                entidad.getActualizadoEn());
    }
}
