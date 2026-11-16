// Hook del historial paginado de reportes de un residente. Consulta
// GET /api/pacientes/{id}/reportes. Concentra carga, error y recarga.

import { useCallback, useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarReportes } from '@/features/reportes/services/reporteService'
import type { PaginaReportes } from '@/features/reportes/types'

interface EstadoHistorialReportes {
  datos: PaginaReportes | null
  cargando: boolean
  error: string | null
  recargar: () => void
}

/**
 * Carga el historial de reportes de un residente.
 * @param pacienteId residente de referencia
 * @param pagina pagina (base 0)
 * @param tamano tamano de pagina
 */
export function useHistorialReportes(
  pacienteId: number,
  pagina: number,
  tamano: number,
): EstadoHistorialReportes {
  const [datos, setDatos] = useState<PaginaReportes | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    listarReportes(pacienteId, { pagina, tamano })
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
  }, [pacienteId, pagina, tamano, reintento])

  const recargar = useCallback(() => setReintento((n) => n + 1), [])

  return { datos, cargando, error, recargar }
}
