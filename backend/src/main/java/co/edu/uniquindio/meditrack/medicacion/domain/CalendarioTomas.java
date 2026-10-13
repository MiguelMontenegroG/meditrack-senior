package co.edu.uniquindio.meditrack.medicacion.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculo puro de las tomas de un dia a partir de los horarios de un esquema y
 * de las tomas registradas.
 *
 * <p>Lo "pendiente" y "atrasado" no se persiste: se deriva aqui.</p>
 * <ul>
 *   <li>Si la hora programada (fecha + hora en la zona del centro) ya tiene un
 *   registro, el estado del slot es el registrado (ADMINISTRADA u OMITIDA).</li>
 *   <li>Si no tiene registro y la hora programada ya paso (es anterior a
 *   "ahora"), el slot se marca ATRASADA.</li>
 *   <li>Si no tiene registro y la hora programada aun no llega, PENDIENTE.</li>
 * </ul>
 *
 * <p>Es logica de dominio pura (sin Spring ni JPA) y testeable con un reloj
 * fijo.</p>
 */
public final class CalendarioTomas {

    private CalendarioTomas() {
        // Utilidad: no se instancia.
    }

    /**
     * Estado calculado de un slot de toma del dia.
     */
    public enum EstadoSlot {
        PENDIENTE,
        ATRASADA,
        ADMINISTRADA,
        OMITIDA
    }

    /**
     * Slot calculado: instante programado y estado derivado.
     */
    public record SlotToma(Instant fechaHoraProgramada, EstadoSlot estado) {
    }

    /**
     * Construye los slots del dia indicado para un esquema.
     *
     * @param esquema          esquema con sus horarios
     * @param tomasDelDia      tomas registradas (se usan su instante programado y su estado)
     * @param fecha            dia a calcular (en la zona del centro)
     * @param zona             zona horaria del centro
     * @param ahora            instante actual inyectado
     */
    public static List<SlotToma> calcularDelDia(
            EsquemaMedicacion esquema,
            List<RegistroToma> tomasDelDia,
            LocalDate fecha,
            ZoneId zona,
            Instant ahora) {
        Map<Instant, EstadoToma> registradas = tomasDelDia.stream()
                .collect(Collectors.toMap(
                        RegistroToma::getFechaHoraProgramada,
                        RegistroToma::getEstado,
                        (a, b) -> a));

        List<SlotToma> slots = new ArrayList<>();
        for (LocalTime hora : esquema.horas()) {
            Instant programada = fecha.atTime(hora).atZone(zona).toInstant();
            slots.add(new SlotToma(programada, estadoDelSlot(programada, registradas, ahora)));
        }
        slots.sort((a, b) -> a.fechaHoraProgramada().compareTo(b.fechaHoraProgramada()));
        return slots;
    }

    private static EstadoSlot estadoDelSlot(
            Instant programada,
            Map<Instant, EstadoToma> registradas,
            Instant ahora) {
        EstadoToma registrado = registradas.get(programada);
        if (registrado != null) {
            return registrado == EstadoToma.ADMINISTRADA
                    ? EstadoSlot.ADMINISTRADA
                    : EstadoSlot.OMITIDA;
        }
        return programada.isBefore(ahora) ? EstadoSlot.ATRASADA : EstadoSlot.PENDIENTE;
    }
}
