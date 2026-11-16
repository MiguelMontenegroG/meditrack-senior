package co.edu.uniquindio.meditrack.alerta.application;

import co.edu.uniquindio.meditrack.alerta.application.port.AlertaRepositoryPort;
import co.edu.uniquindio.meditrack.alerta.domain.AlcanceAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import co.edu.uniquindio.meditrack.medicacion.application.port.PacienteAlcancePort;
import co.edu.uniquindio.meditrack.medicacion.domain.RolUsuario;
import co.edu.uniquindio.meditrack.paciente.application.port.PacienteRepositoryPort;
import co.edu.uniquindio.meditrack.paciente.domain.Paciente;
import co.edu.uniquindio.meditrack.shared.application.port.AccionAuditoria;
import co.edu.uniquindio.meditrack.shared.application.port.AuditoriaPort;
import co.edu.uniquindio.meditrack.shared.application.port.ResultadoAuditoria;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: listado paginado de alertas con filtros opcionales {@code estado},
 * {@code tipo} y {@code pacienteId}.
 *
 * <p>Los tres roles pueden leer, siempre segun su alcance:</p>
 * <ul>
 *   <li>Si llega {@code pacienteId} y no es visible (inexistente o fuera de
 *   alcance) se responde una pagina <b>vacia</b> (total 0) sin revelar su
 *   existencia.</li>
 *   <li>Si no llega {@code pacienteId} se listan las alertas de los residentes
 *   visibles (consultas de alcance del modulo paciente, un metodo por rol).</li>
 * </ul>
 *
 * <p>Los filtros se resuelven con metodos de repositorio separados por combinacion
 * (nunca parametros nulos a PostgreSQL). El {@code total} lo calcula el mismo
 * predicado que el contenido.</p>
 */
public class ListarAlertasUseCase {

    /** Tamano de pagina para enumerar los residentes visibles. */
    private static final int TAMANO_PAGINA = 500;

    private final AlertaRepositoryPort alertas;
    private final PacienteRepositoryPort pacientes;
    private final PacienteAlcancePort alcancePaciente;
    private final AuditoriaPort auditoria;

    public ListarAlertasUseCase(
            AlertaRepositoryPort alertas,
            PacienteRepositoryPort pacientes,
            PacienteAlcancePort alcancePaciente,
            AuditoriaPort auditoria) {
        this.alertas = alertas;
        this.pacientes = pacientes;
        this.alcancePaciente = alcancePaciente;
        this.auditoria = auditoria;
    }

    public PaginaAlertasDominio ejecutar(
            EstadoAlerta estado, TipoAlerta tipo, Long pacienteId,
            int pagina, int tamano, AlcanceAlerta autor) {
        if (pacienteId != null) {
            return porResidente(estado, tipo, pacienteId, pagina, tamano, autor);
        }
        return porConjuntoVisible(estado, tipo, pagina, tamano, autor);
    }

    private PaginaAlertasDominio porResidente(
            EstadoAlerta estado, TipoAlerta tipo, Long pacienteId,
            int pagina, int tamano, AlcanceAlerta autor) {
        if (!alcancePaciente.visible(pacienteId, autor.usuarioId(), aRolUsuario(autor))) {
            auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "alerta",
                    pacienteId, ResultadoAuditoria.DENEGADO,
                    "Listado de alertas de residente fuera de alcance o inexistente");
            return new PaginaAlertasDominio(List.of(), 0L);
        }
        PaginaAlertasDominio resultado = listarPorPaciente(estado, tipo, pacienteId, pagina, tamano);
        auditar(autor, "Listado de alertas del residente");
        return resultado;
    }

    private PaginaAlertasDominio porConjuntoVisible(
            EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano, AlcanceAlerta autor) {
        List<Long> visibles = pacientesVisibles(autor);
        if (visibles.isEmpty()) {
            auditar(autor, "Listado de alertas sin residentes visibles");
            return new PaginaAlertasDominio(List.of(), 0L);
        }
        PaginaAlertasDominio resultado = listarPorPacientes(estado, tipo, visibles, pagina, tamano);
        auditar(autor, "Listado de alertas");
        return resultado;
    }

    private PaginaAlertasDominio listarPorPaciente(
            EstadoAlerta estado, TipoAlerta tipo, Long pacienteId, int pagina, int tamano) {
        if (estado != null && tipo != null) {
            return alertas.listarPorPacienteEstadoYTipo(pacienteId, estado, tipo, pagina, tamano);
        }
        if (estado != null) {
            return alertas.listarPorPacienteYEstado(pacienteId, estado, pagina, tamano);
        }
        if (tipo != null) {
            return alertas.listarPorPacienteYTipo(pacienteId, tipo, pagina, tamano);
        }
        return alertas.listarPorPaciente(pacienteId, pagina, tamano);
    }

    private PaginaAlertasDominio listarPorPacientes(
            EstadoAlerta estado, TipoAlerta tipo, List<Long> visibles, int pagina, int tamano) {
        if (estado != null && tipo != null) {
            return alertas.listarPorPacientesEstadoYTipo(visibles, estado, tipo, pagina, tamano);
        }
        if (estado != null) {
            return alertas.listarPorPacientesYEstado(visibles, estado, pagina, tamano);
        }
        if (tipo != null) {
            return alertas.listarPorPacientesYTipo(visibles, tipo, pagina, tamano);
        }
        return alertas.listarPorPacientes(visibles, pagina, tamano);
    }

    private void auditar(AlcanceAlerta autor, String detalle) {
        auditoria.registrar(autor.usuarioId(), AccionAuditoria.READ, "alerta",
                null, ResultadoAuditoria.EXITOSO, detalle);
    }

    private List<Long> pacientesVisibles(AlcanceAlerta autor) {
        List<Long> ids = new ArrayList<>();
        int pagina = 0;
        List<Paciente> lote;
        do {
            lote = listarLote(autor, pagina);
            lote.forEach(p -> ids.add(p.getId()));
            pagina++;
        } while (lote.size() == TAMANO_PAGINA);
        return ids;
    }

    private List<Paciente> listarLote(AlcanceAlerta autor, int pagina) {
        return switch (autor.rol()) {
            case ADMINISTRADOR -> pacientes.listarTodos(null, null, pagina, TAMANO_PAGINA);
            case CUIDADOR_ENFERMERO -> pacientes.listarPorCuidador(
                    null, null, autor.usuarioId(), pagina, TAMANO_PAGINA);
            case FAMILIAR_AUTORIZADO -> pacientes.listarPorFamiliar(
                    null, null, autor.usuarioId(), pagina, TAMANO_PAGINA);
        };
    }

    private static RolUsuario aRolUsuario(AlcanceAlerta alcance) {
        return RolUsuario.valueOf(alcance.rol().name());
    }
}
