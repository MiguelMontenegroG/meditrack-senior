package co.edu.uniquindio.meditrack.paciente.application.port;

import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de contactos de emergencia (entidades dependientes
 * del paciente; se pueden borrar).
 */
public interface ContactoEmergenciaRepositoryPort {

    ContactoEmergencia guardar(ContactoEmergencia contacto);

    Optional<ContactoEmergencia> buscarPorId(Long id);

    List<ContactoEmergencia> listarPorPaciente(Long pacienteId);

    /** Contacto principal actual del paciente, si existe. */
    Optional<ContactoEmergencia> buscarPrincipal(Long pacienteId);

    void eliminar(Long id);
}
