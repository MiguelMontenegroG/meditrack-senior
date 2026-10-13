package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.RegistroTomaRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RegistroToma;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;

import java.time.Instant;
import java.util.List;

/**
 * Caso de uso: historial de tomas de un residente (solo lectura), con filtros
 * opcionales por esquema y rango de fechas. Lectura paginada segun el alcance.
 */
public class ListarHistorialTomasUseCase {

    private final RegistroTomaRepositoryPort tomas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ListarHistorialTomasUseCase(
            RegistroTomaRepositoryPort tomas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.tomas = tomas;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public List<RegistroToma> ejecutar(
            Long pacienteId, Long esquemaId, Instant desde, Instant hasta,
            int pagina, int tamano, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);
        List<RegistroToma> lista = tomas.listarHistorial(pacienteId, esquemaId, desde, hasta, pagina, tamano);
        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "registro_toma",
                pacienteId, ResultadoAuditoria.EXITOSO, "Historial de tomas de un residente");
        return lista;
    }

    public long contar(
            Long pacienteId, Long esquemaId, Instant desde, Instant hasta, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);
        return tomas.contarHistorial(pacienteId, esquemaId, desde, hasta);
    }
}
