// Boton para activar o suspender un esquema de medicacion (solo ADMINISTRADOR).
// Pide confirmacion antes de suspender y evita el doble envio.

import { useState } from 'react'
import { Power } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { cambiarEstadoEsquema } from '@/features/medicacion/services/esquemaService'

interface PropsBotonEstadoEsquema {
  pacienteId: number
  esquemaId: number
  activo: boolean
  alCambiar: () => Promise<void>
}

/** Activa o suspende un esquema con confirmacion. */
export function BotonEstadoEsquema({
  pacienteId,
  esquemaId,
  activo,
  alCambiar,
}: PropsBotonEstadoEsquema) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const aplicar = async () => {
    if (enviando) return
    setEnviando(true)
    setError(null)
    try {
      await cambiarEstadoEsquema(pacienteId, esquemaId, !activo)
      setConfirmando(false)
      await alCambiar()
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    } finally {
      setEnviando(false)
    }
  }

  if (activo && !confirmando) {
    return (
      <BotonAccion variant="outline" onClick={() => setConfirmando(true)}>
        <Power size={16} /> Suspender
      </BotonAccion>
    )
  }

  if (activo && confirmando) {
    return (
      <span
        className="confirmacion-inline"
        role="alertdialog"
        aria-label="Confirmar suspension del esquema"
      >
        <span>¿Suspender este esquema?</span>
        <BotonAccion variant="danger" onClick={aplicar} disabled={enviando}>
          {enviando ? 'Suspendiendo...' : 'Si, suspender'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={() => setConfirmando(false)} disabled={enviando}>
          Cancelar
        </BotonAccion>
        {error && <span className="campo-error" role="alert">{error}</span>}
      </span>
    )
  }

  return (
    <span className="estado-accion">
      <BotonAccion variant="outline" onClick={aplicar} disabled={enviando}>
        <Power size={16} /> {enviando ? 'Activando...' : 'Activar'}
      </BotonAccion>
      {error && <span className="campo-error" role="alert">{error}</span>}
    </span>
  )
}
