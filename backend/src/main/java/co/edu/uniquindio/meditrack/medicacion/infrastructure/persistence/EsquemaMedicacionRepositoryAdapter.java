package co.edu.uniquindio.meditrack.medicacion.infrastructure.persistence;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.HorarioMedicacion;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia de esquemas de medicacion (con sus horarios).
 *
 * <p>Los horarios forman parte del agregado: al guardar se persisten los
 * horarios faltantes y al reemplazar se borran los previos. La actualizacion de
 * horarios de un esquema existente es idempotente.</p>
 */
@Component
public class EsquemaMedicacionRepositoryAdapter implements EsquemaMedicacionRepositoryPort {

    /** Fecha centinela para representar vigencia abierta (sin fecha de fin). */
    private static final LocalDate FECHA_MAX = LocalDate.of(9999, 12, 31);

    private final EsquemaMedicacionJpaRepository esquemas;
    private final HorarioMedicacionJpaRepository horarios;

    public EsquemaMedicacionRepositoryAdapter(
            EsquemaMedicacionJpaRepository esquemas,
            HorarioMedicacionJpaRepository horarios) {
        this.esquemas = esquemas;
        this.horarios = horarios;
    }

    @Override
    @Transactional
    public EsquemaMedicacion guardar(EsquemaMedicacion esquema) {
        EsquemaMedicacionEntity entity = esquema.getId() == null
                ? new EsquemaMedicacionEntity()
                : esquemas.findById(esquema.getId()).orElseThrow();
        entity.setPacienteId(esquema.getPacienteId());
        entity.setMedicamentoId(esquema.getMedicamentoId());
        entity.setDosis(esquema.getDosis());
        entity.setFrecuenciaHoras(esquema.getFrecuenciaHoras());
        entity.setVia(esquema.getVia());
        entity.setIndicaciones(esquema.getIndicaciones());
        entity.setFechaInicio(esquema.getFechaInicio());
        entity.setFechaFin(esquema.getFechaFin());
        entity.setActivo(esquema.isActivo());
        EsquemaMedicacionEntity guardado = esquemas.save(entity);

        if (esquema.getId() == null) {
            for (HorarioMedicacion horario : esquema.getHorarios()) {
                horarios.save(new HorarioMedicacionEntity(guardado.getId(), horario.hora()));
            }
        }

        return aDominioConHorarios(guardado);
    }

    @Override
    @Transactional
    public void reemplazarHorarios(Long esquemaId, List<LocalTime> horas) {
        horarios.deleteByEsquemaId(esquemaId);
        horarios.flush();
        for (LocalTime hora : horas) {
            horarios.save(new HorarioMedicacionEntity(esquemaId, hora));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EsquemaMedicacion> buscarPorId(Long id) {
        return esquemas.findById(id).map(this::aDominioConHorarios);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EsquemaMedicacion> listarPorPaciente(
            Long pacienteId, Boolean activo, Boolean vigente, LocalDate hoy) {
        return elegirListado(pacienteId, activo, vigente, hoy).stream()
                .map(this::aDominioConHorarios)
                .toList();
    }

    /**
     * Elige la consulta segun que filtros llegan informados. Nunca se envia un
     * parametro nulo a PostgreSQL (causa del fallo
     * {@code cannot cast type bytea to boolean}).
     */
    private List<EsquemaMedicacionEntity> elegirListado(
            Long pacienteId, Boolean activo, Boolean vigente, LocalDate hoy) {
        if (activo != null && vigente != null) {
            return vigente
                    ? esquemas.listarActivoVigente(pacienteId, activo, hoy)
                    : esquemas.listarActivoNoVigente(pacienteId, activo, hoy);
        }
        if (activo != null) {
            return esquemas.listarPorActivo(pacienteId, activo);
        }
        if (vigente != null) {
            return vigente
                    ? esquemas.listarVigente(pacienteId, hoy)
                    : esquemas.listarNoVigente(pacienteId, hoy);
        }
        return esquemas.listarTodos(pacienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EsquemaMedicacion> listarUtilizables(Long pacienteId, LocalDate hoy) {
        return esquemas.listarUtilizables(pacienteId, hoy).stream()
                .map(this::aDominioConHorarios)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeSolapamiento(
            Long pacienteId, Long medicamentoId, LocalDate fechaInicio, LocalDate fechaFin, Long idExcluido) {
        LocalDate fin = fechaFin == null ? FECHA_MAX : fechaFin;
        long idExcluir = idExcluido == null ? 0L : idExcluido;
        return esquemas.contarSolapamientos(pacienteId, medicamentoId, fechaInicio, fin, idExcluir) > 0;
    }

    private EsquemaMedicacion aDominioConHorarios(EsquemaMedicacionEntity entity) {
        List<HorarioMedicacion> horariosDominio = horarios
                .findByEsquemaIdOrderByHora(entity.getId()).stream()
                .map(h -> HorarioMedicacion.de(h.getHora()))
                .toList();
        return new EsquemaMedicacion(
                entity.getId(), entity.getPacienteId(), entity.getMedicamentoId(),
                entity.getDosis(), entity.getFrecuenciaHoras(), entity.getVia(),
                entity.getIndicaciones(), entity.getFechaInicio(), entity.getFechaFin(),
                entity.isActivo(), horariosDominio);
    }
}
