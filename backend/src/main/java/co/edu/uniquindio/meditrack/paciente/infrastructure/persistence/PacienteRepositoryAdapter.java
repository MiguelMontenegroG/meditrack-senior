package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import org.springframework.data.domain.Page;
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
 *
 * <p>Los filtros opcionales ({@code texto} y {@code activo}) se resuelven con
 * una consulta por combinacion de filtros presentes (y una por alcance): asi
 * nunca se envia a PostgreSQL un parametro nulo, que era lo que obligaba a los
 * {@code cast(... as string)} / {@code cast(... as boolean)} y provocaba errores
 * de tipado. La busqueda por texto vacio se normaliza a nulo antes de decidir
 * la combinacion.</p>
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

    // --- Listado y conteo por alcance (una consulta por combinacion de filtros). ---

    @Override
    public List<Paciente> listarTodos(String texto, Boolean activo, int pagina, int tamano) {
        String t = textoVacioANulo(texto);
        Pageable pageable = pageable(pagina, tamano);
        Page<PacienteEntity> resultado;
        if (t != null && activo != null) {
            resultado = repositorio.listarTodosPorTextoYActivo(t, activo, pageable);
        } else if (t != null) {
            resultado = repositorio.listarTodosPorTexto(t, pageable);
        } else if (activo != null) {
            resultado = repositorio.listarTodosPorActivo(activo, pageable);
        } else {
            resultado = repositorio.listarTodosSinFiltros(pageable);
        }
        return map(resultado.getContent());
    }

    @Override
    public long contarTodos(String texto, Boolean activo) {
        String t = textoVacioANulo(texto);
        if (t != null && activo != null) {
            return repositorio.contarTodosPorTextoYActivo(t, activo);
        }
        if (t != null) {
            return repositorio.contarTodosPorTexto(t);
        }
        if (activo != null) {
            return repositorio.contarTodosPorActivo(activo);
        }
        return repositorio.contarTodosSinFiltros();
    }

    @Override
    public List<Paciente> listarPorCuidador(String texto, Boolean activo, Long cuidadorId, int pagina, int tamano) {
        String t = textoVacioANulo(texto);
        Pageable pageable = pageable(pagina, tamano);
        Page<PacienteEntity> resultado;
        if (t != null && activo != null) {
            resultado = repositorio.listarCuidadorPorTextoYActivo(t, activo, cuidadorId, pageable);
        } else if (t != null) {
            resultado = repositorio.listarCuidadorPorTexto(t, cuidadorId, pageable);
        } else if (activo != null) {
            resultado = repositorio.listarCuidadorPorActivo(activo, cuidadorId, pageable);
        } else {
            resultado = repositorio.listarCuidadorSinFiltros(cuidadorId, pageable);
        }
        return map(resultado.getContent());
    }

    @Override
    public long contarPorCuidador(String texto, Boolean activo, Long cuidadorId) {
        String t = textoVacioANulo(texto);
        if (t != null && activo != null) {
            return repositorio.contarCuidadorPorTextoYActivo(t, activo, cuidadorId);
        }
        if (t != null) {
            return repositorio.contarCuidadorPorTexto(t, cuidadorId);
        }
        if (activo != null) {
            return repositorio.contarCuidadorPorActivo(activo, cuidadorId);
        }
        return repositorio.contarCuidadorSinFiltros(cuidadorId);
    }

    @Override
    public List<Paciente> listarPorFamiliar(String texto, Boolean activo, Long familiarId, int pagina, int tamano) {
        String t = textoVacioANulo(texto);
        Pageable pageable = pageable(pagina, tamano);
        Page<PacienteEntity> resultado;
        if (t != null && activo != null) {
            resultado = repositorio.listarFamiliarPorTextoYActivo(t, activo, familiarId, pageable);
        } else if (t != null) {
            resultado = repositorio.listarFamiliarPorTexto(t, familiarId, pageable);
        } else if (activo != null) {
            resultado = repositorio.listarFamiliarPorActivo(activo, familiarId, pageable);
        } else {
            resultado = repositorio.listarFamiliarSinFiltros(familiarId, pageable);
        }
        return map(resultado.getContent());
    }

    @Override
    public long contarPorFamiliar(String texto, Boolean activo, Long familiarId) {
        String t = textoVacioANulo(texto);
        if (t != null && activo != null) {
            return repositorio.contarFamiliarPorTextoYActivo(t, activo, familiarId);
        }
        if (t != null) {
            return repositorio.contarFamiliarPorTexto(t, familiarId);
        }
        if (activo != null) {
            return repositorio.contarFamiliarPorActivo(activo, familiarId);
        }
        return repositorio.contarFamiliarSinFiltros(familiarId);
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