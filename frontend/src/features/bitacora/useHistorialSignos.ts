// Hook del historial de signos vitales de un residente, paginado y filtrable
// por rango de fechas (hora de Bogota convertida a instantes UTC). El backend
// devuelve los registros del mas reciente al mas antiguo.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { aInstanteUtc, sumarDias } from '@/lib/fechas-bogota'
import { listarSignosVitales } from '@/features/bitacora/services/bitacoraService'
import type { PaginaSignosVitales } from '@/features/bitacora/types'

/** Filtros elegidos por el usuario; las fechas son locales de Bogota ('' = sin
 *  limite por ese extremo). */
export interface FiltrosHistorialSignos {
  desde: string
  hasta: string
  pagina: number
  tamano: number
}

interface EstadoHistorial {
  datos: PaginaSignosVitales | null
  cargando: boolean
  error: string | null
  recargar: () => void
}

/**
 * Traduce el rango local a instantes UTC. `desde` es el inicio del dia y
 * `hasta` el inicio del dia siguiente (fin exclusivo), para incluir completo el
 * ultimo dia elegido, igual que el calendario de citas.
 */
function rangoUtc(desde: string, hasta: string): { desde?: string; hasta?: string } {
  return {
    desde: aInstanteUtc(desde, '00:00') ?? undefined,
    hasta: aInstanteUtc(sumarDias(hasta, 1), '00:00') ?? undefined,
  }
}

/** Historial paginado de signos vitales del residente. */
export function useHistorialSignos(
  pacienteId: number,
  filtros: FiltrosHistorialSignos,
): EstadoHistorial {
  const [datos, setDatos] = useState<PaginaSignosVitales | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  const { desde, hasta, pagina, tamano } = filtros

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    if (desde && hasta && desde > hasta) {
      setDatos(null)
      setError('La fecha inicial no puede ser posterior a la final.')
      setCargando(false)
      return () => {
        vigente = false
      }
    }
    const rango = rangoUtc(desde, hasta)
    listarSignosVitales(pacienteId, { ...rango, pagina, tamano })
      .then((resultado) => {
        if (vigente) setDatos(resultado)
      })
      .catch((fallo) => {
        if (vigente) setError(mensajeDeFallo(fallo))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [pacienteId, desde, hasta, pagina, tamano, reintento])

  return { datos, cargando, error, recargar: () => setReintento((n) => n + 1) }
}
