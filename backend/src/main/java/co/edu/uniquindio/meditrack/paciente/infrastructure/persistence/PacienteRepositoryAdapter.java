package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
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
    public List<Paciente> listarTodos(String texto, Boolean activo, int pagina, int tamano) {
        return map(repositorio.listarTodos(textoVacioANulo(texto), activo, pageable(pagina, tamano)).getContent());
    }

    @Override
    public long contarTodos(String texto, Boolean activo) {
        return repositorio.contarTodos(textoVacioANulo(texto), activo);
    }

    @Override
    public List<Paciente> listarPorCuidador(String texto, Boolean activo, Long cuidadorId, int pagina, int tamano) {
        return map(repositorio.listarPorCuidador(textoVacioANulo(texto), activo, cuidadorId, pageable(pagina, tamano)).getContent());
    }

    @Override
    public long contarPorCuidador(String texto, Boolean activo, Long cuidadorId) {
        return repositorio.contarPorCuidador(textoVacioANulo(texto), activo, cuidadorId);
    }

    @Override
    public List<Paciente> listarPorFamiliar(String texto, Boolean activo, Long familiarId, int pagina, int tamano) {
        return map(repositorio.listarPorFamiliar(textoVacioANulo(texto), activo, familiarId, pageable(pagina, tamano)).getContent());
    }

    @Override
    public long contarPorFamiliar(String texto, Boolean activo, Long familiarId) {
        return repositorio.contarPorFamiliar(textoVacioANulo(texto), activo, familiarId);
    }

    @Override
    public boolean esVisibleParaAdmin(Long pacienteId) {
        return repositorio.existsById(pacienteId);
    }

    @Override
    public boolean esVisibleParaCuidador(Long pacienteId, Long cuidadorId) {
        return repositorio.esVisibleParaCuidador(pacienteId, cuidadorId);
    }

    @Override
    public boolean esVisibleParaFamiliar(Long pacienteId, Long familiarId) {
        return repositorio.esVisibleParaFamiliar(pacienteId, familiarId);
    }

    private Pageable pageable(int pagina, int tamano) {
        return PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "id"));
    }
    private List<Paciente> map(List<PacienteEntity> entidades) {
        return entidades.stream().map(this::aDominio).toList();
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
