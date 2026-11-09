package co.edu.uniquindio.meditrack.reporte.application.port;

import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;

import java.time.Instant;

/**
 * Puerto de recoleccion de los datos que alimentan un reporte clinico.
 *
 * <p>El modulo de reportes es un <b>consumidor de solo lectura</b> de los demas
 * modulos: no crea ni modifica datos clinicos y no accede a sus tablas de forma
 * directa. La implementacion de este puerto vive en infraestructura y compone las
 * consultas de alcance del modulo paciente con consultas propias de lectura sobre
 * las tablas de medicacion, cita, bitacora y alerta (todas ellas de solo lectura,
 * acotadas al residente y al rango de fechas del reporte).</p>
 *
 * <p>El metodo {@link #recolectar} devuelve un {@code ContenidoReporte} ya listo
 * para imprimir: el adaptador de generacion de PDF no consulta la base de datos.</p>
 */
public interface DatosReportePort {

    /**
     * Recolecta el contenido del reporte para el residente y rango indicados.
     *
     * @param pacienteId residente (su alcance ya fue validado por el caso de uso).
     * @param tipo       tipo de reporte; decide que secciones se recolectan.
     * @param desde      inicio del rango (incluido).
     * @param hasta      fin del rango (incluido).
     * @param generadoEn instante de generacion (lo usa la cabecera y el pie).
     * @param autor      nombre del usuario que genera el reporte (para la cabecera).
     * @return contenido completo del reporte con listas no nulas.
     */
    co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte recolectar(
            Long pacienteId,
            TipoReporte tipo,
            Instant desde,
            Instant hasta,
            Instant generadoEn,
            String autor);
}
