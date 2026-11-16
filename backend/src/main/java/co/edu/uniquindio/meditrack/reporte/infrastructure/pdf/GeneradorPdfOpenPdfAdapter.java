package co.edu.uniquindio.meditrack.reporte.infrastructure.pdf;

import co.edu.uniquindio.meditrack.reporte.application.port.GeneradorPdfPort;
import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adaptador de generacion de PDF sobre OpenPDF 3.0.5 (paquete org.openpdf).
 *
 * <p>Unica clase del proyecto que depende de la libreria de PDF: implementa el
 * puerto {@link GeneradorPdfPort} y recibe el {@link ContenidoReporte} ya
 * recolectado, de modo que no consulta la base de datos. Se usa el paquete
 * {@code org.openpdf} (serie 3.x) y no {@code com.lowagie} (serie 2.x), que es la
 * vigente.</p>
 *
 * <p>El documento se compone en memoria (no se escribe en disco) y se devuelve
 * como bytes para que el controlador lo entregue como descarga. Las fechas se
 * formatean en la zona del centro (America/Bogota).</p>
 */
@Component
public class GeneradorPdfOpenPdfAdapter implements GeneradorPdfPort {

    private static final ZoneId ZONA_CENTRO = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZONA_CENTRO);
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZONA_CENTRO);

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
    private static final Font TEXTO_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
    private static final Font PIE = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

    @Override
    public byte[] generar(ContenidoReporte contenido) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 42, 42, 48, 42);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.addAuthor("MediTrack Senior");
            documento.addCreator("MediTrack Senior");
            documento.addTitle("Reporte " + contenido.tipo().name());
            documento.open();

            agregarTitulo(documento, contenido);
            agregarCabecera(documento, contenido.cabecera());
            agregarMedicacion(documento, contenido.medicacion());
            agregarCitas(documento, contenido.citas());
            agregarBitacora(documento, contenido.bitacora());
            agregarAlertas(documento, contenido.alertas());
            agregarPie(documento, contenido);

            documento.close();
        } catch (DocumentException ex) {
            // Un fallo de generacion no debe exponer detalles internos: se convierte
            // en error de negocio controlado (500 controlado por el manejador).
            throw new IllegalStateException("No se pudo generar el PDF del reporte", ex);
        }
        return salida.toByteArray();
    }

    private void agregarTitulo(Document documento, ContenidoReporte contenido) {
        Paragraph titulo = new Paragraph("Reporte clinico - " + tituloTipo(contenido), TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(4);
        documento.add(titulo);

        Instant desde = contenido.desde();
        Instant hasta = contenido.hasta();
        Paragraph rango = new Paragraph(
                "Periodo: " + FORMATO_FECHA_HORA.format(desde) + " a " + FORMATO_FECHA_HORA.format(hasta),
                TEXTO);
        rango.setAlignment(Element.ALIGN_CENTER);
        rango.setSpacingAfter(12);
        documento.add(rango);
    }

    private void agregarCabecera(Document documento, SeccionReporte.Cabecera cabecera) {
        documento.add(seccion("Residente"));

        PdfPTable tabla = new PdfPTable(new float[] { 30, 70 });
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(12);
        filaEtiquetada(tabla, "Nombre", valorONulo(cabecera.nombreCompleto()));
        filaEtiquetada(tabla, "Documento", valorONulo(cabecera.documento()));
        filaEtiquetada(tabla, "Edad", cabecera.edad() == null ? "-" : cabecera.edad() + " anios");
        filaEtiquetada(tabla, "Sexo", valorONulo(cabecera.sexo()));
        filaEtiquetada(tabla, "Habitacion", valorONulo(cabecera.habitacion()));
        filaEtiquetada(tabla, "Situacion clinica", valorONulo(cabecera.situacionClinica()));
        filaEtiquetada(tabla, "Fecha de ingreso",
                cabecera.fechaIngreso() == null ? "-" : FORMATO_FECHA.format(cabecera.fechaIngreso().atStartOfDay(ZONA_CENTRO).toInstant()));
        filaEtiquetada(tabla, "Estado", cabecera.activo() ? "Activo" : "Inactivo");
        documento.add(tabla);
    }

    private void agregarMedicacion(Document documento, List<SeccionReporte.FilaMedicacion> filas)
            throws DocumentException {
        if (filas.isEmpty()) {
            return;
        }
        documento.add(seccion("Medicacion"));
        PdfPTable tabla = tabla(new float[] { 24, 14, 14, 10, 14, 12, 12 });
        encabezado(tabla, "Medicamento", "Dosis", "Via", "Frec.", "Vigencia", "Adm.", "Omit.");
        for (SeccionReporte.FilaMedicacion fila : filas) {
            celda(tabla, valorONulo(fila.medicamento()), TEXTO);
            celda(tabla, valorONulo(fila.dosis()), TEXTO);
            celda(tabla, valorONulo(fila.via()), TEXTO);
            celda(tabla, fila.frecuenciaHoras() + " h", TEXTO);
            celda(tabla, fila.vigencia(), TEXTO);
            celda(tabla, String.valueOf(fila.tomasAdministradas()), TEXTO);
            celda(tabla, String.valueOf(fila.tomasOmitidas()), TEXTO);
        }
        documento.add(tabla);
        documento.add(new Paragraph(" ", TEXTO));
    }

    private void agregarCitas(Document documento, List<SeccionReporte.FilaCita> filas)
            throws DocumentException {
        if (filas.isEmpty()) {
            return;
        }
        documento.add(seccion("Citas"));
        PdfPTable tabla = tabla(new float[] { 18, 20, 20, 18, 14, 12 });
        encabezado(tabla, "Fecha", "Especialidad", "Profesional", "Lugar", "Motivo", "Estado");
        for (SeccionReporte.FilaCita fila : filas) {
            celda(tabla, FORMATO_FECHA_HORA.format(fila.fechaHora()), TEXTO);
            celda(tabla, valorONulo(fila.especialidad()), TEXTO);
            celda(tabla, valorONulo(fila.profesional()), TEXTO);
            celda(tabla, valorONulo(fila.lugar()), TEXTO);
            celda(tabla, valorONulo(fila.motivo()), TEXTO);
            celda(tabla, valorONulo(fila.estado()), TEXTO);
        }
        documento.add(tabla);
        documento.add(new Paragraph(" ", TEXTO));
    }

    private void agregarBitacora(Document documento, List<SeccionReporte.FilaBitacora> filas)
            throws DocumentException {
        if (filas.isEmpty()) {
            return;
        }
        documento.add(seccion("Bitacora de signos vitales"));
        PdfPTable tabla = tabla(new float[] { 20, 14, 16, 14, 36 });
        encabezado(tabla, "Fecha", "Presion", "Glucosa", "Temp.", "Observaciones");
        for (SeccionReporte.FilaBitacora fila : filas) {
            celda(tabla, FORMATO_FECHA_HORA.format(fila.fechaHora()), TEXTO);
            celda(tabla, fila.presionFormateada(), TEXTO);
            celda(tabla, fila.glucosaFormateada(), TEXTO);
            celda(tabla, fila.temperaturaFormateada(), TEXTO);
            celda(tabla, valorONulo(fila.observaciones()), TEXTO);
        }
        documento.add(tabla);
        documento.add(new Paragraph(" ", TEXTO));
    }

    private void agregarAlertas(Document documento, List<SeccionReporte.FilaAlerta> filas)
            throws DocumentException {
        if (filas.isEmpty()) {
            return;
        }
        documento.add(seccion("Alertas del periodo"));
        PdfPTable tabla = tabla(new float[] { 18, 18, 12, 12, 40 });
        encabezado(tabla, "Fecha", "Tipo", "Severidad", "Estado", "Mensaje");
        for (SeccionReporte.FilaAlerta fila : filas) {
            celda(tabla, FORMATO_FECHA_HORA.format(fila.creadoEn()), TEXTO);
            celda(tabla, valorONulo(fila.tipo()), TEXTO);
            celda(tabla, valorONulo(fila.severidad()), TEXTO);
            celda(tabla, valorONulo(fila.estado()), TEXTO);
            celda(tabla, valorONulo(fila.mensaje()), TEXTO);
        }
        documento.add(tabla);
        documento.add(new Paragraph(" ", TEXTO));
    }

    private void agregarPie(Document documento, ContenidoReporte contenido) {
        Paragraph pie = new Paragraph(
                "Generado por MediTrack Senior el "
                        + FORMATO_FECHA_HORA.format(contenido.generadoEn())
                        + ". Documento confidencial: contiene datos de salud protegidos por la Ley 1581 "
                        + "de 2012. Uso exclusivo del personal autorizado.",
                PIE);
        pie.setSpacingBefore(12);
        documento.add(pie);
    }

    // ------------------------------------------------------------------
    // Utilidades de render.
    // ------------------------------------------------------------------

    private static String tituloTipo(ContenidoReporte contenido) {
        return contenido.tipo().name().replace('_', ' ').toLowerCase();
    }

    private static Paragraph seccion(String titulo) {
        Paragraph parrafo = new Paragraph(titulo, SUBTITULO);
        parrafo.setSpacingBefore(6);
        parrafo.setSpacingAfter(4);
        return parrafo;
    }

    private static PdfPTable tabla(float[] anchos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(4);
        tabla.setHeaderRows(1);
        return tabla;
    }

    private static void encabezado(PdfPTable tabla, String... columnas) {
        for (String columna : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(columna, TEXTO_NEGRITA));
            celda.setPadding(3);
            tabla.addCell(celda);
        }
    }

    private static void celda(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setPadding(3);
        tabla.addCell(celda);
    }

    private static void filaEtiquetada(PdfPTable tabla, String etiqueta, String valor) {
        celda(tabla, etiqueta, TEXTO_NEGRITA);
        celda(tabla, valor, TEXTO);
    }

    private static String valorONulo(String valor) {
        return (valor == null || valor.isBlank()) ? "-" : valor;
    }

}