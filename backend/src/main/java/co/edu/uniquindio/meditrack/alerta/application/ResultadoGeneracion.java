package co.edu.uniquindio.meditrack.alerta.application;

/**
 * Resultado de una ejecucion de {@link GenerarAlertasUseCase}.
 *
 * @param evaluadas referencias evaluadas en la ejecucion
 * @param creadas   alertas nuevas persistidas
 * @param duplicadas referencias que ya tenian alerta (viole el indice unico)
 */
public record ResultadoGeneracion(int evaluadas, int creadas, int duplicadas) {

    public static ResultadoGeneracion vacio() {
        return new ResultadoGeneracion(0, 0, 0);
    }
}
