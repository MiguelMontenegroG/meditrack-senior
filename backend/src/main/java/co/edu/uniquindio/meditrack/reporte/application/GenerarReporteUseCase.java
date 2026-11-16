package co.edu.uniquindio.meditrack.reporte.application;

import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.reporte.application.port.DatosReportePort;
import co.edu.uniquindio.meditrack.reporte.application.port.GeneradorPdfPort;
import co.edu.uniquindio.meditrack.reporte.application.port.ReporteGeneradoRepositoryPort;
import co.edu.uniquindio.meditrack.reporte.domain.AlcanceReporte;
import co.edu.uniquindio.meditrack.reporte.domain.ContenidoReporte;
import co.edu.uniquindio.meditrack.reporte.domain.ReporteGenerado;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.usuario.application.port.UsuarioRepositoryPort;

import java.time.Clock;
import java.time.Instant;

/**
 * Caso de uso: generar un reporte clinico en PDF de un residente (Proceso 3).
 *
 * <p>La generacion es una <b>lectura agregada</b>: valida el alcance del residente
 * con {@link VerificadorAlcanceResidente} (404 si no es visible, auditado como
 * READ DENEGADO), recolecta el contenido del periodo con {@link DatosReportePort},
 * registra los metadatos del reporte (reporte_generado) para el historial y
 * devuelve el PDF generado por {@link GeneradorPdfPort}.</p>
 *
 * <p>El PDF no se guarda como binario en la base de datos: se produce en el
 * momento y se entrega al cliente. El historial (reporte_generado) solo guarda
 * metadatos y una ruta referencial, tal como define la migracion V2.</p>
 *
 * <p>El autor (generadoPor) sale siempre del usuario autenticado, nunca del
 * cuerpo de la peticion.</p>
 */
public class GenerarReporteUseCase {

    private final VerificadorAlcanceResidente alcance;
    private final DatosReportePort datos;
    private final GeneradorPdfPort generador;
    private final ReporteGeneradoRepositoryPort reportes;
    private final UsuarioRepositoryPort usuarios;
    private final AuditoriaPort auditoria;
    private final Clock reloj;

    public GenerarReporteUseCase(
            VerificadorAlcanceResidente alcance,
            DatosReportePort datos,
            GeneradorPdfPort generador,
            ReporteGeneradoRepositoryPort reportes,
            UsuarioRepositoryPort usuarios,
            AuditoriaPort auditoria,
            Clock reloj) {
        this.alcance = alcance;
        this.datos = datos;
        this.generador = generador;
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    /**
     * Genera el reporte y devuelve el PDF junto con sus metadatos. El residente
     * debe existir y estar dentro del alcance del usuario (si no, 404). El rango
     * es obligatorio y no puede superar el maximo (400/409).
     */
    public Resultado generar(Long pacienteId, ComandoReporte comando, AlcanceReporte autor) {
        ReporteGenerado.validarRango(comando.desde(), comando.hasta());

        // Alcance: residente visible o 404 (y READ DENEGADO auditado). La generacion
        // de reportes no es escritura clinica, por eso no exige residente activo: se
        // puede emitir el cierre de historia de un residente dado de baja.
        alcance.exigirVisible(pacienteId, aAlcanceMedicacion(autor));

        Instant ahora = reloj.instant();
        String nombreAutor = usuarios.buscarPorId(autor.usuarioId())
                .map(u -> u.getNombreCompleto())
                .orElse("Usuario " + autor.usuarioId());

        ContenidoReporte contenido = datos.recolectar(
                pacienteId, comando.tipo(), comando.desde(), comando.hasta(), ahora, nombreAutor);
        byte[] pdf = generador.generar(contenido);

        ReporteGenerado metadatos = ReporteGenerado.nuevo(
                pacienteId, autor.usuarioId(), comando.tipo(), comando.desde(), comando.hasta(), ahora);
        ReporteGenerado persistido = reportes.guardar(metadatos);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.CREATE, "reporte",
                persistido.getId(), ResultadoAuditoria.EXITOSO,
                "Generacion de reporte " + comando.tipo().name());

        return new Resultado(persistido, pdf);
    }

    /**
     * Resultado de la generacion: metadatos persistidos (para auditar y trazar) y
     * los bytes del PDF (para la descarga).
     */
    public record Resultado(ReporteGenerado reporte, byte[] pdf) {
    }

    /** Adapta el alcance del modulo de reportes al que consume el verificador. */
    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceReporte alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}