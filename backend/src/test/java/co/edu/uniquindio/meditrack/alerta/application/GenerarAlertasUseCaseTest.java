package co.edu.uniquindio.meditrack.alerta.application;

import co.edu.uniquindio.meditrack.alerta.application.port.AlertaRepositoryPort;
import co.edu.uniquindio.meditrack.alerta.application.port.FuentesAlertaPort;
import co.edu.uniquindio.meditrack.alerta.domain.Alerta;
import co.edu.uniquindio.meditrack.alerta.domain.EstadoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.TipoAlerta;
import co.edu.uniquindio.meditrack.alerta.domain.UmbralesSignosVitales;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del caso de uso {@link GenerarAlertasUseCase} con reloj fijo: cada fuente
 * genera su alerta, dentro de la tolerancia no se genera nada y dos ejecuciones no
 * duplican (idempotencia, simulando el indice unico parcial de V2).
 */
class GenerarAlertasUseCaseTest {

    private static final Instant AHORA = Instant.parse("2026-03-10T15:00:00Z");
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private static final ConfiguracionAlertas CONFIGURACION = new ConfiguracionAlertas(
            30, 60, 24,
            new ConfiguracionAlertas.Umbrales(90, 180, 50, 110, 70, 250, 35.0, 38.0));

    @Test
    void generaUnaAlertaPorCadaFuenteConMensajeGenerico() {
        FuentesFake fuentes = new FuentesFake();
        fuentes.tomas.add(new FuentesAlertaPort.FuenteToma(501L, 10L, 9001L));
        fuentes.citas.add(new FuentesAlertaPort.FuenteCita(601L, 11L));
        fuentes.signos.add(new FuentesAlertaPort.FuenteSignos(701L, 12L));
        AlmacenFake alertas = new AlmacenFake();

        ResultadoGeneracion resultado = useCase(fuentes, alertas).ejecutar();

        assertThat(resultado.evaluadas()).isEqualTo(3);
        assertThat(resultado.creadas()).isEqualTo(3);
        assertThat(alertas.guardadas).extracting(Alerta::getTipo)
                .containsExactlyInAnyOrder(
                        TipoAlerta.DOSIS_ATRASADA,
                        TipoAlerta.SIGNO_FUERA_DE_RANGO,
                        TipoAlerta.CITA_PROXIMA);
        assertThat(alertas.guardadas).allSatisfy(alerta -> {
            assertThat(alerta.getEstado()).isEqualTo(EstadoAlerta.ACTIVA);
            assertThat(alerta.getMensaje()).isNotBlank();
        });
        // Mensajes genericos: nunca incluyen valores clinicos (cifras).
        assertThat(alertas.guardadas).extracting(Alerta::getMensaje)
                .allSatisfy(mensaje -> assertThat(mensaje).doesNotContainPattern("\\d"));
        // La alerta referencia el registro_toma (FK de V2), no el horario.
        assertThat(alertas.guardadas).extracting(Alerta::getTomaId).contains(9001L);
        assertThat(alertas.guardadas).extracting(Alerta::getCitaId).contains(601L);
        assertThat(alertas.guardadas).extracting(Alerta::getSignosId).contains(701L);
    }

    @Test
    void noGeneraNadaDentroDeLaTolerancia() {
        FuentesFake fuentes = new FuentesFake();
        AlmacenFake alertas = new AlmacenFake();

        ResultadoGeneracion resultado = useCase(fuentes, alertas).ejecutar();

        assertThat(resultado.creadas()).isZero();
        assertThat(alertas.guardadas).isEmpty();
    }

    @Test
    void calculaLosLimitesConLaToleranciaYLaVentanaDeSignos() {
        FuentesFake fuentes = new FuentesFake();
        AlmacenFake alertas = new AlmacenFake();

        useCase(fuentes, alertas).ejecutar();

        // Toma: 30 minutos atras; cita: 60 minutos atras; signos: ultimas 24 h.
        assertThat(fuentes.limiteTomas).isEqualTo(AHORA.minusSeconds(30 * 60));
        assertThat(fuentes.limiteCitas).isEqualTo(AHORA.minusSeconds(60 * 60));
        assertThat(fuentes.desdeSignos).isEqualTo(AHORA.minusSeconds(24 * 60 * 60));
        // La toma programada de hoy se calcula con la fecha del centro (Bogota).
        assertThat(fuentes.fechaCentroTomas).isEqualTo("2026-03-10");
        assertThat(fuentes.zonaTomas).isEqualTo("America/Bogota");
    }

    @Test
    void dosEjecucionesNoDuplicanAlertas() {
        FuentesFake fuentes = new FuentesFake();
        fuentes.tomas.add(new FuentesAlertaPort.FuenteToma(501L, 10L, 9001L));
        fuentes.citas.add(new FuentesAlertaPort.FuenteCita(601L, 11L));
        fuentes.signos.add(new FuentesAlertaPort.FuenteSignos(701L, 12L));
        AlmacenFake alertas = new AlmacenFake();
        GenerarAlertasUseCase casoDeUso = useCase(fuentes, alertas);

        ResultadoGeneracion primera = casoDeUso.ejecutar();
        ResultadoGeneracion segunda = casoDeUso.ejecutar();

        assertThat(primera.creadas()).isEqualTo(3);
        assertThat(segunda.creadas()).isZero();
        assertThat(segunda.duplicadas()).isEqualTo(3);
        assertThat(alertas.guardadas).hasSize(3);
    }

    @Test
    void umbralDeReferenciaSoloAlertaValoresEstrictamenteFuera() {
        UmbralesSignosVitales umbrales = new UmbralesSignosVitales(
                90, 180, 50, 110, 70, 250, 35.0, 38.0);

        assertThat(umbrales.fueraDeRango(180, 110, 250, 38.0)).isFalse();
        assertThat(umbrales.fueraDeRango(181, null, null, null)).isTrue();
        assertThat(umbrales.fueraDeRango(null, null, 69, null)).isTrue();
        assertThat(umbrales.fueraDeRango(null, null, null, 35.0)).isFalse();
        assertThat(umbrales.fueraDeRango(null, null, null, 34.9)).isTrue();
        assertThat(umbrales.fueraDeRango(null, null, null, null)).isFalse();
    }

    private GenerarAlertasUseCase useCase(FuentesAlertaPort fuentes, AlertaRepositoryPort alertas) {
        Clock reloj = Clock.fixed(AHORA, BOGOTA);
        return new GenerarAlertasUseCase(fuentes, alertas, CONFIGURACION, reloj);
    }

    // ------------------------------------------------------------------
    // Dobles de prueba.
    // ------------------------------------------------------------------

    /** Fuente falsa: devuelve las referencias configuradas y captura los limites. */
    private static final class FuentesFake implements FuentesAlertaPort {
        final List<FuenteToma> tomas = new ArrayList<>();
        final List<FuenteCita> citas = new ArrayList<>();
        final List<FuenteSignos> signos = new ArrayList<>();
        Instant limiteTomas;
        Instant limiteCitas;
        Instant desdeSignos;
        String fechaCentroTomas;
        String zonaTomas;

        @Override
        public List<FuenteToma> tomasAtrasadas(Instant limite, String fechaCentro, String zona) {
            limiteTomas = limite;
            fechaCentroTomas = fechaCentro;
            zonaTomas = zona;
            return tomas;
        }

        @Override
        public List<FuenteCita> citasProgramadasVencidas(Instant limite) {
            limiteCitas = limite;
            return citas;
        }

        @Override
        public List<FuenteSignos> signosFueraDeRango(Instant desde, UmbralesConsulta umbrales) {
            desdeSignos = desde;
            return signos;
        }
    }

    /** Almacen falso que simula los indices unicos parciales por fuente. */
    private static final class AlmacenFake implements AlertaRepositoryPort {
        final List<Alerta> guardadas = new ArrayList<>();
        private final Set<String> fuentes = new HashSet<>();

        @Override
        public Optional<Alerta> guardar(Alerta alerta) {
            String clave = alerta.getTomaId() != null ? "toma:" + alerta.getTomaId()
                    : alerta.getCitaId() != null ? "cita:" + alerta.getCitaId()
                    : "signos:" + alerta.getSignosId();
            if (!fuentes.add(clave)) {
                return Optional.empty();
            }
            guardadas.add(alerta);
            return Optional.of(alerta);
        }

        private long proximaTomaOmitida = 9000L;

        @Override
        public TomaLocalizada localizarOMaterializarTomaOmitida(
                Long horarioId, Long pacienteId, String fechaCentro, String zona, java.time.Instant ahora) {
            // El caso de uso solo llega aqui cuando la fuente no trae registro_toma.
            proximaTomaOmitida++;
            return new TomaLocalizada(proximaTomaOmitida, false);
        }

        @Override
        public long contar() {
            return guardadas.size();
        }

        @Override
        public Optional<Alerta> buscarPorId(Long id) {
            return Optional.empty();
        }

        @Override
        public Alerta actualizar(Alerta alerta) {
            return alerta;
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacientes(
                List<Long> pacienteIds, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacientesYEstado(
                List<Long> pacienteIds, EstadoAlerta estado, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacientesYTipo(
                List<Long> pacienteIds, TipoAlerta tipo, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacientesEstadoYTipo(
                List<Long> pacienteIds, EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPaciente(
                Long pacienteId, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacienteYEstado(
                Long pacienteId, EstadoAlerta estado, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacienteYTipo(
                Long pacienteId, TipoAlerta tipo, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }

        @Override
        public co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio listarPorPacienteEstadoYTipo(
                Long pacienteId, EstadoAlerta estado, TipoAlerta tipo, int pagina, int tamano) {
            return new co.edu.uniquindio.meditrack.alerta.domain.PaginaAlertasDominio(List.of(), 0);
        }
    }
}
