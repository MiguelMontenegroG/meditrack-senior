// Hook de carga del historial de tomas de un residente, paginado y filtrable
// por esquema y rango de fechas.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarHistorialTomas } from '@/features/medicacion/services/tomaService'
import type { FiltrosHistorialTomas, PaginaTomas } from '@/features/medicacion/types'

interface EstadoHistorial {
  datos: PaginaTomas | null
  cargando: boolean
  error: string | null
  recargar: () => void
}

/**
 * Carga el historial paginado. Los filtros ya vienen normalizados por la
 * pantalla; `pagina` se controla aqui para la navegacion.
 */
export function useHistorialTomas(
  pacienteId: number,
  filtros: Omit<FiltrosHistorialTomas, 'pagina' | 'tamano'>,
  pagina: number,
  tamano: number,
): EstadoHistorial {
  const [datos, setDatos] = useState<PaginaTomas | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  const { esquemaId, desde, hasta } = filtros

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    listarHistorialTomas(pacienteId, { esquemaId, desde, hasta, pagina, tamano })
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
  }, [pacienteId, esquemaId, desde, hasta, pagina, tamano, reintento])

  return { datos, cargando, error, recargar: () => setReintento((n) => n + 1) }
}
