package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia del catalogo de medicamentos: traduce entre el
 * modelo de dominio y las entidades JPA.
 */
@Component
public class MedicamentoRepositoryAdapter implements MedicamentoRepositoryPort {

    private final MedicamentoJpaRepository repositorio;

    public MedicamentoRepositoryAdapter(MedicamentoJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Medicamento guardar(Medicamento medicamento) {
        MedicamentoEntity entity = medicamento.getId() == null
                ? new MedicamentoEntity()
                : repositorio.findById(medicamento.getId()).orElseThrow();
        entity.setNombre(medicamento.getNombre());
        entity.setPresentacion(medicamento.getPresentacion());
        entity.setActivo(medicamento.isActivo());
        return aDominio(repositorio.save(entity));
    }

    @Override
    public Optional<Medicamento> buscarPorId(Long id) {
        return repositorio.findById(id).map(MedicamentoRepositoryAdapter::aDominio);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return repositorio.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public boolean existePorNombreEnOtro(String nombre, Long idExcluido) {
        return repositorio.existsByNombreIgnoreCaseAndIdNot(nombre, idExcluido);
    }

    @Override
    public List<Medicamento> listar(String texto, Boolean activo, int pagina, int tamano) {
        return elegirListado(texto, activo, PageRequest.of(pagina, tamano))
                .getContent().stream()
                .map(MedicamentoRepositoryAdapter::aDominio)
                .toList();
    }

    @Override
    public long contar(String texto, Boolean activo) {
        boolean hayTexto = texto != null && !texto.isBlank();
        if (hayTexto && activo != null) {
            return repositorio.contarPorTextoYActivo(texto, activo);
        }
        if (hayTexto) {
            return repositorio.contarPorTexto(texto);
        }
        if (activo != null) {
            return repositorio.contarPorActivo(activo);
        }
        return repositorio.contarTodos();
    }

    /**
     * Elige la consulta segun que filtros llegan informados. Nunca se envia un
     * parametro nulo a PostgreSQL.
     */
    private Page<MedicamentoEntity> elegirListado(String texto, Boolean activo, PageRequest pageable) {
        boolean hayTexto = texto != null && !texto.isBlank();
        if (hayTexto && activo != null) {
            return repositorio.listarPorTextoYActivo(texto, activo, pageable);
        }
        if (hayTexto) {
            return repositorio.listarPorTexto(texto, pageable);
        }
        if (activo != null) {
            return repositorio.listarPorActivo(activo, pageable);
        }
        return repositorio.listarTodos(pageable);
    }

    private static Medicamento aDominio(MedicamentoEntity entity) {
        return new Medicamento(
                entity.getId(), entity.getNombre(), entity.getPresentacion(), entity.isActivo());
    }
}
