package co.edu.uniquindio.meditrack.cita.infrastructure.persistence;

import co.edu.uniquindio.meditrack.cita.application.port.EspecialidadRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia del catalogo de especialidades (solo lectura).
 */
@Component
public class EspecialidadRepositoryAdapter implements EspecialidadRepositoryPort {

    private final EspecialidadJpaRepository especialidades;

    public EspecialidadRepositoryAdapter(EspecialidadJpaRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return especialidades.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> nombrePorId(Long id) {
        return especialidades.findById(id).map(EspecialidadEntity::getNombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Especialidad> listarTodas() {
        return especialidades.findAll().stream()
                .map(e -> new Especialidad(e.getId(), e.getNombre()))
                .toList();
    }
}
