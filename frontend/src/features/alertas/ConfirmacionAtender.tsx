// Confirmacion inline para atender una alerta. Se muestra al pulsar "Atender"
// y exige una segunda accion explicita ("Si, atender") para evitar errores.
// No conoce el servicio: recibe los callbacks de confirmar y cancelar.

interface PropsConfirmacionAtender {
  /** true mientras se envia la atencion (bloquea los botones). */
  enviando: boolean
  alConfirmar: () => void
  alCancelar: () => void
}

/** Barra de confirmacion para atender una alerta. */
export function ConfirmacionAtender({
  enviando,
  alConfirmar,
  alCancelar,
}: PropsConfirmacionAtender) {
  return (
    <div className="confirmacion-inline" role="group" aria-label="Confirmar atencion de la alerta">
      <p>¿Confirmas que atendiste esta alerta?</p>
      <button
        type="button"
        className="btn btn-primary"
        onClick={alConfirmar}
        disabled={enviando}
      >
        {enviando ? 'Atendiendo...' : 'Si, atender'}
      </button>
      <button type="button" className="btn btn-outline" onClick={alCancelar} disabled={enviando}>
        Cancelar
      </button>
    </div>
  )
}
