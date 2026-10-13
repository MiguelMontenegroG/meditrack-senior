package co.edu.uniquindio.meditrack.medicacion.application;

import co.edu.uniquindio.meditrack.medicacion.application.port.EsquemaMedicacionRepositoryPort;
import co.edu.uniquindio.meditrack.medicacion.domain.AlcanceMedicacion;
import co.edu.uniquindio.meditrack.medicacion.domain.EsquemaMedicacion;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso: listar los esquemas de un residente segun el alcance del usuario.
 *
 * <p>El filtrado por residente visible se hace primero (404 + READ DENEGADO si
 * esta fuera de alcance) y luego se listan los esquemas. La lectura se audita
 * como EXITOSA.</p>
 */
public class ListarEsquemasUseCase {

    private final EsquemaMedicacionRepositoryPort esquemas;
    private final VerificadorAlcanceResidente alcance;
    private final AuditoriaPort auditoria;

    public ListarEsquemasUseCase(
            EsquemaMedicacionRepositoryPort esquemas,
            VerificadorAlcanceResidente alcance,
            AuditoriaPort auditoria) {
        this.esquemas = esquemas;
        this.alcance = alcance;
        this.auditoria = auditoria;
    }

    public List<EsquemaMedicacion> ejecutar(
            Long pacienteId, Boolean activo, Boolean vigente, LocalDate hoy, AlcanceMedicacion autor) {
        alcance.exigirVisible(pacienteId, autor);

        List<EsquemaMedicacion> lista = esquemas.listarPorPaciente(pacienteId, activo, vigente, hoy);

        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "esquema_medicacion",
                pacienteId, ResultadoAuditoria.EXITOSO, "Listado de esquemas de un residente");

        return lista;
    }
}
