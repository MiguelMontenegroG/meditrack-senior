package co.edu.uniquindio.meditrack.medicacion.domain;

import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Instant;

/**
 * Registro de una toma efectivamente administrada u omitida (historial clinico).
 *
 * <p>Las tomas nunca se editan ni se borran (Ley 1581): un error se corrige con
 * una nueva observacion, no modificando el registro. Por eso el registro no
 * expone mutadores.</p>
 *
 * <p>{@code registradoPor} sale siempre del usuario autenticado, nunca del
 * cuerpo de la peticion.</p>
 */
public class RegistroToma {

    private final Long id;
    private final Long esquemaId;
    private final Long registradoPor;
    private final Instant fechaHoraProgramada;
    private final Instant fechaHoraRegistro;
    private final EstadoToma estado;
    private final String observaciones;

    public RegistroToma(
            Long id,
            Long esquemaId,
            Long registradoPor,
            Instant fechaHoraProgramada,
            Instant fechaHoraRegistro,
            EstadoToma estado,
            String observaciones) {
        this.id = id;
        this.esquemaId = esquemaId;
        this.registradoPor = registradoPor;
        this.fechaHoraProgramada = fechaHoraProgramada;
        this.fechaHoraRegistro = fechaHoraRegistro;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    /**
     * Crea un registro de toma nuevo validando sus reglas.
     *
     * @param ahora instante actual (inyectado para que sea determinista)
     */
    public static RegistroToma registrar(
            Long esquemaId,
            Long registradoPor,
            Instant fechaHoraProgramada,
            EstadoToma estado,
            String observaciones,
            Instant ahora) {
        if (esquemaId == null) {
            throw new ValidacionDeNegocioException("El esquema es obligatorio");
        }
        if (fechaHoraProgramada == null) {
            throw new ValidacionDeNegocioException("La fecha y hora programada es obligatoria");
        }
        if (estado == null) {
            throw new ValidacionDeNegocioException("El estado de la toma es obligatorio");
        }
        if (estado == EstadoToma.OMITIDA && (observaciones == null || observaciones.isBlank())) {
            throw new ValidacionDeNegocioException("Una toma omitida debe registrar observaciones");
        }
        return new RegistroToma(
                null, esquemaId, registradoPor, fechaHoraProgramada, ahora, estado,
                observaciones == null ? null : observaciones.trim());
    }

    public Long getId() {
        return id;
    }

    public Long getEsquemaId() {
        return esquemaId;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public Instant getFechaHoraProgramada() {
        return fechaHoraProgramada;
    }

    public Instant getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public EstadoToma getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }
}
