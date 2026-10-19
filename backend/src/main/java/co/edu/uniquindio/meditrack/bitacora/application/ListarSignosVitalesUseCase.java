package co.edu.uniquindio.meditrack.bitacora.application;

import co.edu.uniquindio.meditrack.bitacora.application.port.SignosVitalesRepositoryPort;
import co.edu.uniquindio.meditrack.bitacora.domain.AlcanceBitacora;
import co.edu.uniquindio.meditrack.medicacion.application.VerificadorAlcanceResidente;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;
import co.edu.uniquindio.meditrack.shared.domain.ValidacionDeNegocioException;

import java.time.Instant;

/**
 * Caso de uso: historial paginado de signos vitales de un residente.
 *
 * <p>Los tres roles pueden leer, segun su alcance. Los filtros opcionales
 * {@code desde}/{@code hasta} se resuelven con metodos de repositorio separados
 * por combinacion (nunca con parametros nulos). El orden es por fecha descendente
 * (registro mas reciente primero).</p>
 *
 * <p>Si el residente esta fuera de alcance o es inexistente, el verificador
 * responde 404 y audita READ DENEGADO sin revelar su existencia. Al final se
 * audita la lectura exitosa del historial.</p>
 */
public class ListarSignosVitalesUseCase {

    private final SignosVitalesRepositoryPort signos;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ListarSignosVitalesUseCase(
            SignosVitalesRepositoryPort signos,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.signos = signos;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public PaginaSignosVitalesDominio ejecutar(
            Long pacienteId, Instant desde, Instant hasta, int pagina, int tamano, AlcanceBitacora autor) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionDeNegocioException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        alcance.exigirVisible(pacienteId, aAlcanceMedicacion(autor));

        PaginaSignosVitalesDominio resultado = consultar(pacienteId, desde, hasta, pagina, tamano);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "signos_vitales",
                pacienteId, ResultadoAuditoria.EXITOSO, "Historial de signos vitales");

        return resultado;
    }

    /** Elige el metodo de repositorio segun que extremos del rango llegan informados. */
    private PaginaSignosVitalesDominio consultar(
            Long pacienteId, Instant desde, Instant hasta, int pagina, int tamano) {
        if (desde == null && hasta == null) {
            return signos.listarPorPaciente(pacienteId, pagina, tamano);
        }
        if (hasta == null) {
            return signos.listarPorPacienteDesde(pacienteId, desde, pagina, tamano);
        }
        if (desde == null) {
            return signos.listarPorPacienteHasta(pacienteId, hasta, pagina, tamano);
        }
        return signos.listarPorPacienteYRango(pacienteId, desde, hasta, pagina, tamano);
    }

    private static AlcanceMedicacion aAlcanceMedicacion(AlcanceBitacora alcance) {
        return AlcanceMedicacion.de(alcance.usuarioId(), RolUsuario.valueOf(alcance.rol().name()));
    }
}
