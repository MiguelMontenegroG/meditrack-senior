package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.application.port.ContactoEmergenciaRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.ContactoEmergencia;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de contactos de emergencia.
 */
@Component
public class ContactoEmergenciaRepositoryAdapter implements ContactoEmergenciaRepositoryPort {

    private final ContactoEmergenciaJpaRepository repositorio;

    public ContactoEmergenciaRepositoryAdapter(ContactoEmergenciaJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ContactoEmergencia guardar(ContactoEmergencia contacto) {
        ContactoEmergenciaEntity entidad = contacto.getId() == null
                ? new ContactoEmergenciaEntity()
                : repositorio.findById(contacto.getId()).orElseGet(ContactoEmergenciaEntity::new);

        entidad.setPacienteId(contacto.getPacienteId());
        entidad.setNombre(contacto.getNombre());
        entidad.setParentesco(contacto.getParentesco());
        entidad.setTelefono(contacto.getTelefono());
        entidad.setEsPrincipal(contacto.isPrincipal());

        return aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<ContactoEmergencia> buscarPorId(Long id) {
        return repositorio.findById(id).map(this::aDominio);
    }

    @Override
    public List<ContactoEmergencia> listarPorPaciente(Long pacienteId) {
        return repositorio.findByPacienteIdOrderByIdAsc(pacienteId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<ContactoEmergencia> buscarPrincipal(Long pacienteId) {
        return repositorio.findFirstByPacienteIdAndEsPrincipalTrue(pacienteId).map(this::aDominio);
    }

    @Override
    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }

    private ContactoEmergencia aDominio(ContactoEmergenciaEntity entidad) {
        return new ContactoEmergencia(
                entidad.getId(),
                entidad.getPacienteId(),
                entidad.getNombre(),
                entidad.getParentesco(),
                entidad.getTelefono(),
                entidad.isEsPrincipal(),
                entidad.getCreadoEn());
    }
}
