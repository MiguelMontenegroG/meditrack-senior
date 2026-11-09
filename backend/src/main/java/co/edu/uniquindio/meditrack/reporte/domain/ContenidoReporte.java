package co.edu.uniquindio.meditrack.reporte.domain;

import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte.Cabecera;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte.FilaAlerta;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte.FilaBitacora;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte.FilaCita;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte.FilaMedicacion;

import java.util.List;

/**
 * Contenido completo de un reporte clinico listo para renderizar en PDF.
 *
 * <p>Se compone de la cabecera del residente (identificacion y datos clinicos
 * generales) y de las secciones que corresponden al {@link TipoReporte}, cada una
 * con sus filas ya acotadas al rango de fechas del reporte. Es un objeto inmutable
 * que viaja del caso de uso al adaptador de generacion de PDF; el adaptador no
 * consulta la base de datos.</p>
 *
 * <p>Las listas nunca son nulas (lista vacia cuando la seccion no aplica o no hay
 * datos): el PDF imprime "Sin registros en el periodo" cuando corresponde.</p>
 */
public record ContenidoReporte(
        TipoReporte tipo,
        java.time.Instant desde,
        java.time.Instant hasta,
        java.time.Instant generadoEn,
        Cabecera cabecera,
        List<FilaMedicacion> medicacion,
        List<FilaCita> citas,
        List<FilaBitacora> bitacora,
        List<FilaAlerta> alertas) {

    /** Normaliza a listas inmutables y no nulas. */
    public ContenidoReporte {
        medicacion = medicacion == null ? List.of() : List.copyOf(medicacion);
        citas = citas == null ? List.of() : List.copyOf(citas);
        bitacora = bitacora == null ? List.of() : List.copyOf(bitacora);
        alertas = alertas == null ? List.of() : List.copyOf(alertas);
    }
}
