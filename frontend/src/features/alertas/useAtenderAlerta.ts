// Hook de atencion de una alerta. Encapsula la llamada PATCH, el estado de
// "enviando", el mensaje de error contextual (409 si ya estaba atendida, 404 si
// ya no existe) y la recarga del listado al exito. La pantalla solo presenta.

import { useCallback, useState } from 'react'
import { mensajeDeErrorContexto, mensajeDeFallo, type ContextoError } from '@/lib/mensajes-error'
import { atenderAlerta } from '@/features/alertas/services/alertaService'

interface EstadoAtenderAlerta {
  /** Id de la alerta en proceso (o null). */
  atendiendoId: number | null
  /** Mensaje de error en espanol de la ultima atencion (o null). */
  error: string | null
  /** Atiende la alerta y recarga la lista al exito. */
  atender: (id: number) => Promise<void>
  /** Limpia el error mostrado. */
  limpiarError: () => void
}

/** Contexto de mensaje segun el status del fallo. */
function contextoPorStatus(status: number | undefined): ContextoError {
  if (status === 409) return 'alerta-atendida'
  if (status === 404) return 'alerta-no-encontrada'
  return 'alerta-atendida'
}

/** Devuelve el handler y el estado para atender alertas de la lista. */
export function useAtenderAlerta(alRecargar: () => void): EstadoAtenderAlerta {
  const [atendiendoId, setAtendiendoId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const atender = useCallback(
    async (id: number) => {
      setAtendiendoId(id)
      setError(null)
      try {
        await atenderAlerta(id)
        alRecargar()
      } catch (fallo) {
        const status =
          fallo && typeof fallo === 'object' && 'status' in fallo
            ? (fallo as { status: number }).status
            : undefined
        if (status === 409 || status === 404) {
          setError(mensajeDeErrorContexto(status === 409 ? 'CONFLICTO' : 'ERROR_INESPERADO', contextoPorStatus(status)))
        } else {
          setError(mensajeDeFallo(fallo))
        }
        // Aunque falle, se recarga para reflejar el estado real.
        alRecargar()
      } finally {
        setAtendiendoId(null)
      }
    },
    [alRecargar],
  )

  const limpiarError = useCallback(() => setError(null), [])

  return { atendiendoId, error, atender, limpiarError }
}
