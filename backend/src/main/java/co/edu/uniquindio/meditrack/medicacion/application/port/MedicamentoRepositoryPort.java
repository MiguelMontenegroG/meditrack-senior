package co.edu.uniquindio.meditrack.medicacion.application.port;

import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia del catalogo de medicamentos.
 *
 * <p>Consultas JPQL simples (sin parametros opcionales comparados con literales
 * de enum, causa del 500 de Hibernate 6.6 en el modulo paciente). La busqueda y
 * el filtro se resuelven con predicados tipados.</p>
 */
public interface MedicamentoRepositoryPort {

    Medicamento guardar(Medicamento medicamento);

    Optional<Medicamento> buscarPorId(Long id);

    /** Indica si ya existe un medicamento con ese nombre (sin distinguir mayusculas). */
    boolean existePorNombre(String nombre);

    /** Indica si existe el nombre en otro medicamento distinto del indicado. */
    boolean existePorNombreEnOtro(String nombre, Long idExcluido);

    /** Listado paginado con busqueda por nombre y filtro de estado (ambos opcionales). */
    List<Medicamento> listar(String texto, Boolean activo, int pagina, int tamano);

    long contar(String texto, Boolean activo);
}
