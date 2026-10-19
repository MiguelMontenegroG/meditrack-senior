// Hook del ultimo registro de signos vitales de un residente. El backend
// responde 404 tanto si el residente esta fuera de alcance como si no tiene
// registros; para la seccion "resumen" ambos casos se muestran igual ("sin
// registros"), sin tratarlo como error. Cualquier otro fallo si se informa y
// se puede reintentar.

import { useEffect, useState } from 'react'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { obtenerUltimoRegistro } from '@/features/bitacora/services/bitacoraService'
import type { SignosVitales } from '@/features/bitacora/types'

interface EstadoUltimoRegistro {
  registro: SignosVitales | null
  cargando: boolean
  error: string | null
  recargar: () => void
}

/** Ultimo registro de signos vitales del residente (o null si no hay). */
export function useUltimoRegistro(pacienteId: number): EstadoUltimoRegistro {
  const [registro, setRegistro] = useState<SignosVitales | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    obtenerUltimoRegistro(pacienteId)
      .then((resultado) => {
        if (vigente) setRegistro(resultado)
      })
      .catch((fallo) => {
        if (!vigente) return
        // 404 = sin registros (o fuera de alcance): no es un fallo para mostrar.
        if (fallo instanceof ErrorApi && fallo.status === 404) {
          setRegistro(null)
        } else {
          setError(mensajeDeFallo(fallo))
        }
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [pacienteId, reintento])

  return { registro, cargando, error, recargar: () => setReintento((n) => n + 1) }
}
