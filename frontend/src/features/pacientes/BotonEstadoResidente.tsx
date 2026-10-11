// Boton para activar/desactivar un residente (solo ADMINISTRADOR). Pide
// confirmacion antes de desactivar (borrado logico) y muestra el estado del
// envio para evitar doble clic.

import { useState } from 'react'
import { Power } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { cambiarEstadoPaciente } from '@/features/pacientes/services/pacienteService'

interface PropsBotonEstadoResidente {
  pacienteId: number
  activo: boolean
  alCambiar: () => Promise<void>
}

/** Activa o desactiva un residente con confirmacion. */
export function BotonEstadoResidente({
  pacienteId,
  activo,
  alCambiar,
}: PropsBotonEstadoResidente) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const aplicar = async () => {
    if (enviando) return
    setEnviando(true)
    setError(null)
    try {
      await cambiarEstadoPaciente(pacienteId, !activo)
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
      <span className="confirmacion-inline" role="alertdialog" aria-label="Confirmar desactivación">
        <span>¿Desactivar este residente?</span>
        <BotonAccion variant="danger" onClick={aplicar} disabled={enviando}>
          {enviando ? 'Desactivando...' : 'Sí, desactivar'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={() => setConfirmando(false)} disabled={enviando}>
          Cancelar
        </BotonAccion>
        {error && <span className="campo-error" role="alert">{error}</span>}
      </span>
    )
  }

  return (
    <BotonAccion variant="outline" onClick={aplicar} disabled={enviando}>
      <Power size={16} /> {enviando ? 'Activando...' : 'Activar'}
    </BotonAccion>
  )
}
