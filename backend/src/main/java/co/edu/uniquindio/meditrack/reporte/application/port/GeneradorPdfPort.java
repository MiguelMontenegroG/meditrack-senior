package co.edu.uniquindio.meditrack.reporte.application.port;

import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;

/**
 * Puerto de generacion del PDF de un reporte clinico.
 *
 * <p>Aisla el motor concreto de PDF (OpenPDF) del dominio y de la applicacion: el
 * caso de uso pide "un PDF de este contenido" y recibe los bytes, sin conocer la
 * libreria. La implementacion vive en infraestructura.</p>
 */
public interface GeneradorPdfPort {

    /**
     * Genera el PDF del contenido indicado.
     *
     * @param contenido contenido ya recolectado del reporte.
     * @return bytes del documento PDF.
     */
    byte[] generar(ContenidoReporte contenido);
}
