package co.edu.uniquindio.meditrack.reporte.infrastructure.persistence;

import co.edu.uniquindio.meditrack.alerta.infrastructure.persistence.AlertaEntity;
import co.edu.uniquindio.meditrack.bitacora.infrastructure.persistence.RegistroSignosVitalesEntity;
import co.edu.uniquindio.meditrack.cita.infrastructure.persistence.CitaEntity;
import co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence.EsquemaMedicacionEntity;
import co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence.HorarioMedicacionEntity;
import co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence.RegistroTomaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Consultas de <b>solo lectura</b> que alimentan un reporte clinico.
 *
 * <p>El modulo de reportes es un consumidor de lectura de los demas modulos: no
 * usa sus adaptadores de escritura ni modifica datos. Para evitar acoplarse a las
 * reglas de negocio de cada modulo, este repositorio declara sus propias consultas
 * JPQL sobre las entidades ya mapeadas (esquema de lectura estricto), acotadas al
 * residente y al rango de fechas del reporte. El residente se resuelve desde
 * paciente; el resto se filtra por su id.</p>
 *
 * <p>Se usa JpaRepository solo como ancla de Spring Data; no se invocan sus
 * metodos de escritura. Las proyecciones son entidades existentes o tuplas de
 * columnas, y el adaptador las traduce a {@code SeccionReporte} (texto plano).</p>
 */
public interface DatosReporteJpaRepository
        extends JpaRepository<RegistroSignosVitalesEntity, Long> {

    // ------------------------------------------------------------------
    // Bitacora: registros de signos vitales del residente en el periodo.
    // ------------------------------------------------------------------

    @Query("""
            select r from RegistroSignosVitalesEntity r
            where r.pacienteId = :pacienteId
              and r.fechaHora >= :desde
              and r.fechaHora <= :hasta
            order by r.fechaHora desc
            """)
    List<RegistroSignosVitalesEntity> bitacoraEnRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    // ------------------------------------------------------------------
    // Citas: citas del residente cuyo horario cae en el periodo.
    // ------------------------------------------------------------------

    @Query("""
            select c from CitaEntity c
            where c.pacienteId = :pacienteId
              and c.fechaHora >= :desde
              and c.fechaHora <= :hasta
            order by c.fechaHora desc
            """)
    List<CitaEntity> citasEnRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    // ------------------------------------------------------------------
    // Medicacion: esquemas del residente (todos, activos o no), vigentes a la
    // fecha de referencia, con su medicamento. El adaptador decide que mostrar.
    // ------------------------------------------------------------------

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> esquemasDelPaciente(@Param("pacienteId") Long pacienteId);

    @Query("""
            select e from EsquemaMedicacionEntity e
            where e.pacienteId = :pacienteId
              and e.activo = true
              and e.fechaInicio <= :hoy
              and (e.fechaFin is null or e.fechaFin >= :hoy)
            order by e.id desc
            """)
    List<EsquemaMedicacionEntity> esquemasVigentes(
            @Param("pacienteId") Long pacienteId, @Param("hoy") java.time.LocalDate hoy);

    @Query("""
            select h from HorarioMedicacionEntity h
            where h.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
            order by h.hora
            """)
    List<HorarioMedicacionEntity> horariosDelPaciente(@Param("pacienteId") Long pacienteId);

    @Query("""
            select t from RegistroTomaEntity t
            where t.esquemaId in (
                select e.id from EsquemaMedicacionEntity e where e.pacienteId = :pacienteId)
              and t.fechaHoraProgramada >= :desde
              and t.fechaHoraProgramada <= :hasta
            """)
    List<RegistroTomaEntity> tomasEnRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    /** Nombre del medicamento por id, para etiquetar esquemas. */
    @Query("select m.nombre from MedicamentoEntity m where m.id = :id")
    String nombreMedicamento(@Param("id") Long id);

    // ------------------------------------------------------------------
    // Alertas creadas en el periodo (para el reporte GENERAL).
    // ------------------------------------------------------------------

    @Query("""
            select a from AlertaEntity a
            where a.pacienteId = :pacienteId
              and a.creadoEn >= :desde
              and a.creadoEn <= :hasta
            order by a.creadoEn desc
            """)
    List<AlertaEntity> alertasEnRango(
            @Param("pacienteId") Long pacienteId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);
}