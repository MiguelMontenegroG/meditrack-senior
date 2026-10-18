// Boton para activar/desactivar un medicamento (solo ADMINISTRADOR). Pide
// confirmacion antes de desactivar y evita el doble envio.

import { useState } from 'react'
import { Power } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { cambiarEstadoMedicamento } from '@/features/medicacion/services/medicamentoService'

interface PropsBotonEstadoMedicamento {
  medicamentoId: number
  activo: boolean
  alCambiar: () => Promise<void>
}

/** Activa o desactiva un medicamento con confirmacion. */
export function BotonEstadoMedicamento({
  medicamentoId,
  activo,
  alCambiar,
}: PropsBotonEstadoMedicamento) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const aplicar = async () => {
    if (enviando) return
    setEnviando(true)
    setError(null)
    try {
      await cambiarEstadoMedicamento(medicamentoId, !activo)
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
        <Power size={16} /> Desactivar
      </BotonAccion>
    )
  }

  if (activo && confirmando) {
    return (
      <span
        className="confirmacion-inline"
        role="alertdialog"
        aria-label="Confirmar desactivacion del medicamento"
      >
        <span>¿Desactivar este medicamento?</span>
        <BotonAccion variant="danger" onClick={aplicar} disabled={enviando}>
          {enviando ? 'Desactivando...' : 'Si, desactivar'}
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
