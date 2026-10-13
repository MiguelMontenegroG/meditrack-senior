package co.edu.uniquindio.meditrack.medicacion.domain;

import java.time.LocalTime;

/**
 * Hora exacta del dia en que corresponde una toma de un esquema.
 *
 * <p>Un esquema no puede tener dos horarios con la misma hora (indice unico
 * {@code uq_horario_esquema_hora} en V2).</p>
 */
public record HorarioMedicacion(Long id, LocalTime hora) {

    public static HorarioMedicacion de(LocalTime hora) {
        return new HorarioMedicacion(null, hora);
    }
}
