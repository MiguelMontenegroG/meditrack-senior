package co.edu.uniquindio.meditrack.paciente.infrastructure.persistence;

import co.edu.uniquindio.meditrack.paciente.domain.RolUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data de pacientes, con consultas de alcance.
 *
 * <p>El filtrado por rol se hace en la consulta (no en memoria): el
 * administrador ve todo; el cuidador solo los pacientes con asignacion vigente;
 * el familiar solo las vinculaciones autorizadas.</p>
 */
public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    /**
     * Listado paginado con busqueda por nombre/apellido/documento, filtro de
     * estado y filtro de alcance en la misma consulta.
     */
    @Query("""
            select p from PacienteEntity p
            where (:texto is null
                   or lower(p.nombres)   like lower(concat('%', :texto, '%'))
                   or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                   or lower(p.documento) like lower(concat('%', :texto, '%')))
              and (:activo is null or p.activo = :activo)
              and (
                    :rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.ADMINISTRADOR
                    or (:rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.CUIDADOR_ENFERMERO
                        and exists (select 1 from AsignacionCuidadorEntity a
                                    where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null))
                    or (:rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.FAMILIAR_AUTORIZADO
                        and exists (select 1 from VinculacionFamiliarEntity v
                                    where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true))
                  )
            """)
    Page<PacienteEntity> listar(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("rol") RolUsuario rol,
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);

    @Query("""
            select count(p) from PacienteEntity p
            where (:texto is null
                   or lower(p.nombres)   like lower(concat('%', :texto, '%'))
                   or lower(p.apellidos) like lower(concat('%', :texto, '%'))
                   or lower(p.documento) like lower(concat('%', :texto, '%')))
              and (:activo is null or p.activo = :activo)
              and (
                    :rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.ADMINISTRADOR
                    or (:rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.CUIDADOR_ENFERMERO
                        and exists (select 1 from AsignacionCuidadorEntity a
                                    where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null))
                    or (:rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.FAMILIAR_AUTORIZADO
                        and exists (select 1 from VinculacionFamiliarEntity v
                                    where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true))
                  )
            """)
    long contar(
            @Param("texto") String texto,
            @Param("activo") Boolean activo,
            @Param("rol") RolUsuario rol,
            @Param("usuarioId") Long usuarioId);

    /** Comprueba la visibilidad de un paciente concreto para el alcance dado. */
    @Query("""
            select count(p) > 0 from PacienteEntity p
            where p.id = :pacienteId
              and (
                    :rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.ADMINISTRADOR
                    or (:rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.CUIDADOR_ENFERMERO
                        and exists (select 1 from AsignacionCuidadorEntity a
                                    where a.pacienteId = p.id and a.usuarioId = :usuarioId and a.hasta is null))
                    or (:rol = co.edu.uniquindio.meditrack.paciente.domain.RolUsuario.FAMILIAR_AUTORIZADO
                        and exists (select 1 from VinculacionFamiliarEntity v
                                    where v.pacienteId = p.id and v.usuarioId = :usuarioId and v.autorizado = true))
                  )
            """)
    boolean esVisiblePara(
            @Param("pacienteId") Long pacienteId,
            @Param("rol") RolUsuario rol,
            @Param("usuarioId") Long usuarioId);
}
