package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.MedicamentoRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.Medicamento;

import java.util.List;

/**
 * Caso de uso: listar el catalogo de medicamentos (paginado, con busqueda por
 * nombre y filtro de estado). Lectura para ADMINISTRADOR y CUIDADOR_ENFERMERO
 * (la autorizacion de rol se aplica en la capa web).
 */
public class ListarMedicamentosUseCase {

    private final MedicamentoRepositoryPort medicamentos;

    public ListarMedicamentosUseCase(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public List<Medicamento> ejecutar(String texto, Boolean activo, int pagina, int tamano) {
        return medicamentos.listar(texto, activo, pagina, tamano);
    }

    public long contar(String texto, Boolean activo) {
        return medicamentos.contar(texto, activo);
    }
}
