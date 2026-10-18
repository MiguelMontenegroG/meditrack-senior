package co.edu.uniquindio.meditrack.cita.domain;

/**
 * Estado de una cita, coincidente con el CHECK {@code chk_cita_estado} de la
 * migracion V2.
 *
 * <p>El estado inicial de toda cita es {@code PROGRAMADA}. Desde el se puede
 * pasar a un estado final: {@code CUMPLIDA}, {@code INCUMPLIDA} o
 * {@code CANCELADA}. {@code REPROGRAMADA} existe en el esquema pero el producto
 * no reprograma citas (no hay operacion para ello), por lo que se mantiene
 * declarado pero no se usa como estado de destino en esta entrega.</p>
 */
public enum EstadoCita {

    PROGRAMADA,
    CUMPLIDA,
    INCUMPLIDA,
    CANCELADA,
    REPROGRAMADA;

    /** Estado inicial de una cita recien agendada. */
    public static EstadoCita inicial() {
        return PROGRAMADA;
    }

    /** Indica si es el estado inicial (unico desde el que se puede editar o cambiar). */
    public boolean esInicial() {
        return this == PROGRAMADA;
    }

    /** Indica si es un estado final (la cita ya no admite cambios). */
    public boolean esFinal() {
        return this == CUMPLIDA || this == INCUMPLIDA || this == CANCELADA;
    }

    /**
     * Indica si el estado es uno de los que el cuidador puede marcar
     * (CUMPLIDA o INCUMPLIDA). CANCELADA es exclusiva del administrador.
     */
    public boolean esMarcablePorCuidador() {
        return this == CUMPLIDA || this == INCUMPLIDA;
    }
}
