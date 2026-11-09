package co.edu.uniquindio.meditrack.reporte.infrastructure.pdf;

import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;
import co.edu.uniquindio.meditrack.reporte.domain.SeccionReporte;
import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba del adaptador de PDF: comprueba que OpenPDF produce un documento valido
 * (cabecera %PDF-, catalogo, cierre %%EOF y metadatos de autor/productor) tanto
 * con secciones vacias como con datos en todas las secciones. El contenido de
 * texto va comprimido (FlateDecode), por lo que no se busca texto literal en los
 * bytes: se valida la estructura del documento.
 */
class GeneradorPdfOpenPdfAdapterTest {

    private static final Instant DESDE = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant HASTA = Instant.parse("2026-02-28T23:59:59Z");
    private static final Instant AHORA = Instant.parse("2026-02-10T15:00:00Z");

    private final GeneradorPdfOpenPdfAdapter generador = new GeneradorPdfOpenPdfAdapter();

    private static SeccionReporte.Cabecera cabecera() {
        return new SeccionReporte.Cabecera(
                "Ana Perez", "1020304050", 78, "FEMENINO", "202", "Hipertension controlada",
                LocalDate.of(2020, 3, 1), true);
    }

    private static String texto(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    private static void assertEsPdfValido(byte[] pdf) {
        assertThat(pdf).isNotEmpty();
        assertThat(texto(pdf)).startsWith("%PDF-");
        assertThat(texto(pdf)).endsWith("%%EOF\n");
        assertThat(texto(pdf)).contains("/Type/Catalog");
        assertThat(texto(pdf)).contains("MediTrack Senior");
    }

    @Test
    void generaPdfConSeccionesVacias() {
        ContenidoReporte contenido = new ContenidoReporte(
                TipoReporte.GENERAL, DESDE, HASTA, AHORA, cabecera(),
                List.of(), List.of(), List.of(), List.of());

        assertEsPdfValido(generador.generar(contenido));
    }

    @Test
    void generaPdfConTodasLasSecciones() {
        SeccionReporte.FilaMedicacion medicacion = new SeccionReporte.FilaMedicacion(
                "Losartan", "50 mg", "ORAL", 12, "Con el desayuno",
                LocalDate.of(2026, 1, 1), null, true, "VIGENTE", 20, 2);
        SeccionReporte.FilaCita cita = new SeccionReporte.FilaCita(
                Instant.parse("2026-02-15T14:00:00Z"), "Cardiologia", "Dr. Gomez", "Consultorio 3",
                "Control", "CUMPLIDA");
        SeccionReporte.FilaBitacora bitacora = new SeccionReporte.FilaBitacora(
                Instant.parse("2026-02-09T12:00:00Z"), 120, 80, 95, new BigDecimal("36.5"), "Sin novedad");
        SeccionReporte.FilaAlerta alerta = new SeccionReporte.FilaAlerta(
                Instant.parse("2026-02-09T12:05:00Z"), "SIGNOS_VITALES", "ALTA", "PENDIENTE",
                "Presion elevada");

        ContenidoReporte contenido = new ContenidoReporte(
                TipoReporte.HISTORIA_CLINICA, DESDE, HASTA, AHORA, cabecera(),
                List.of(medicacion), List.of(cita), List.of(bitacora), List.of(alerta));

        assertEsPdfValido(generador.generar(contenido));
    }

    @Test
    void formateaBitacoraConGuionesCuandoFaltanMedidas() {
        SeccionReporte.FilaBitacora sinMedidas = new SeccionReporte.FilaBitacora(
                Instant.parse("2026-02-09T12:00:00Z"), null, null, null, null, null);

        assertThat(sinMedidas.presionFormateada()).isEqualTo("-");
        assertThat(sinMedidas.glucosaFormateada()).isEqualTo("-");
        assertThat(sinMedidas.temperaturaFormateada()).isEqualTo("-");
    }
}