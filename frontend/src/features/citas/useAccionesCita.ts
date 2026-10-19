// Hook con las acciones de escritura sobre una cita: marcar CUMPLIDA/INCUMPLIDA
// y cancelar. Cada accion llama al servicio y expone el mensaje en espanol. El
// backend es la fuente de verdad: si la regla falla (400/409) se traduce el
// codigo segun el contexto de citas.

import { useState } from 'react'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { cambiarEstadoCita } from '@/features/citas/services/citaService'
import type { DestinoEstadoCita } from '@/features/citas/types'

interface EstadoAcciones {
  enviando: boolean
  error: string | null
  marcar: (pacienteId: number, citaId: number, estado: DestinoEstadoCita) => Promise<boolean>
  limpiar: () => void
}

/** Mensaje en espanol del fallo de una accion de cambio de estado. */
function mensajeErrorAccion(codigo: string): string {
  if (codigo === 'VALIDACION') {
    return mensajeDeErrorContexto('VALIDACION', 'cita-hora-no-llegada')
  }
  if (codigo === 'CONFLICTO') {
    return mensajeDeErrorContexto('CONFLICTO', 'cita-estado-final')
  }
  return mensajeDeErrorContexto(codigo, 'cita-estado-final')
}

/** Acciones de cambio de estado de una cita. */
export function useAccionesCita(): EstadoAcciones {
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const marcar = async (pacienteId: number, citaId: number, estado: DestinoEstadoCita) => {
    setEnviando(true)
    setError(null)
    try {
      await cambiarEstadoCita(pacienteId, citaId, estado)
      return true
    } catch (fallo) {
      setError(fallo instanceof ErrorApi ? mensajeErrorAccion(fallo.codigo) : mensajeDeFallo(fallo))
      return false
    } finally {
      setEnviando(false)
    }
  }

  return { enviando, error, marcar, limpiar: () => setError(null) }
}