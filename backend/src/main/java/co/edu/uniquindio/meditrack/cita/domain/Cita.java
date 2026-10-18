package co.edu.uniquindio.meditrack.cita.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Instant;

/**
 * Cita medica de un residente (modelo de dominio, sin Spring ni JPA).
 *
 * <p>Mapea la tabla {@code cita} de la migracion V2: paciente, especialidad
 * (opcional en el esquema), profesional, fecha/hora, lugar y motivo. El autor
 * ({@code registradaPor}) sale siempre del usuario autenticado, nunca del
 * cuerpo de la peticion.</p>
 *
 * <p>Reglas de negocio (documentadas en {@code docs/seguridad.md}):</p>
 * <ul>
 *   <li>Al agendar, la fecha/hora no puede estar en el pasado.</li>
 *   <li>Solo se edita en estado inicial (PROGRAMADA).</li>
 *   <li>CUMPLIDA/INCUMPLIDA solo desde el estado inicial y solo si la fecha/hora
 *   de la cita ya llego (no se puede marcar el futuro).</li>
 *   <li>CANCELADA solo desde el estado inicial.</li>
 *   <li>Una cita en estado final no se edita ni vuelve a cambiar.</li>
 * </ul>
 *
 * <p>Las reglas que dependen del tiempo reciben el instante actual
 * ({@code ahora}), calculado con el bean {@code Clock} en la capa de aplicacion,
 * para que sean deterministas y testeables.</p>
 */
public class Cita {

    private final Long id;
    private final Long pacienteId;
    private final Long registradaPor;
    private Long especialidadId;
    private String profesional;
    private Instant fechaHora;
    private String lugar;
    private String motivo;
    private EstadoCita estado;

    public Cita(
            Long id,
            Long pacienteId,
            Long registradaPor,
            Long especialidadId,
            String profesional,
            Instant fechaHora,
            String lugar,
            String motivo,
            EstadoCita estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.registradaPor = registradaPor;
        this.especialidadId = especialidadId;
        this.profesional = profesional;
        this.fechaHora = fechaHora;
        this.lugar = lugar;
        this.motivo = motivo;
        this.estado = estado;
    }

    /**
     * Crea una cita nueva (sin id aun) en estado PROGRAMADA.
     *
     * @param ahora instante actual obtenido del bean {@code Clock}.
     * @throws ValidacionDeNegocioException si la fecha/hora es nula o ya paso.
     */
    public static Cita nuevo(
            Long pacienteId,
            Long registradaPor,
            Long especialidadId,
            String profesional,
            Instant fechaHora,
            String lugar,
            String motivo,
            Instant ahora) {
        if (pacienteId == null) {
            throw new ValidacionDeNegocioException("El residente es obligatorio");
        }
        validarFechaHoraFutura(fechaHora, ahora);
        return new Cita(
                null,
                pacienteId,
                registradaPor,
                especialidadId,
                normalizar(profesional),
                fechaHora,
                normalizar(lugar),
                normalizar(motivo),
                EstadoCita.inicial());
    }

    /**
     * Edita fecha/hora, especialidad, profesional, lugar y motivo. Solo se
     * permite en estado inicial (PROGRAMADA); en cualquier otro estado responde
     * 409.
     *
     * @param ahora instante actual obtenido del bean {@code Clock}.
     */
    public void editar(
            Long especialidadId,
            String profesional,
            Instant fechaHora,
            String lugar,
            String motivo,
            Instant ahora) {
        exigirEstadoInicial("No se puede editar una cita que ya no esta en estado inicial");
        validarFechaHoraFutura(fechaHora, ahora);
        this.especialidadId = especialidadId;
        this.profesional = normalizar(profesional);
        this.fechaHora = fechaHora;
        this.lugar = normalizar(lugar);
        this.motivo = normalizar(motivo);
    }

    /**
     * Marca la cita como CUMPLIDA o INCUMPLIDA. Solo desde el estado inicial y
     * solo si la fecha/hora de la cita ya llego (no se puede marcar una cita
     * futura).
     *
     * @param nuevo estado de destino (CUMPLIDA o INCUMPLIDA).
     * @param ahora instante actual obtenido del bean {@code Clock}.
     */
    public void marcarCumplimiento(EstadoCita nuevo, Instant ahora) {
        if (nuevo != EstadoCita.CUMPLIDA && nuevo != EstadoCita.INCUMPLIDA) {
            throw new ValidacionDeNegocioException("Solo se puede marcar CUMPLIDA o INCUMPLIDA");
        }
        exigirEstadoInicial("Solo se puede marcar estado desde el estado inicial de la cita");
        if (fechaHora != null && fechaHora.isAfter(ahora)) {
            throw new ValidacionDeNegocioException(
                    "No se puede marcar una cita cuya fecha y hora aun no ha llegado");
        }
        this.estado = nuevo;
    }

    /**
     * Cancela la cita. Solo desde el estado inicial.
     */
    public void cancelar() {
        exigirEstadoInicial("Solo se puede cancelar una cita en estado inicial");
        this.estado = EstadoCita.CANCELADA;
    }

    /** Indica si la cita admite edicion de sus datos. */
    public boolean editable() {
        return estado != null && estado.esInicial();
    }

    private void exigirEstadoInicial(String mensaje) {
        if (estado == null || !estado.esInicial()) {
            throw new ConflictoDeNegocioException(mensaje);
        }
    }

    private static void validarFechaHoraFutura(Instant fechaHora, Instant ahora) {
        if (fechaHora == null) {
            throw new ValidacionDeNegocioException("La fecha y hora de la cita es obligatoria");
        }
        if (fechaHora.isBefore(ahora)) {
            throw new ValidacionDeNegocioException("La fecha y hora de la cita no puede estar en el pasado");
        }
    }

    private static String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getRegistradaPor() {
        return registradaPor;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public String getProfesional() {
        return profesional;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public String getLugar() {
        return lugar;
    }

    public String getMotivo() {
        return motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }
}
