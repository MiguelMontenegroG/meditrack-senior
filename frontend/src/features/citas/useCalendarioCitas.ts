// Hook de carga del calendario de citas. Recibe el rango (fechas locales de
// Bogota), el estado y el residente seleccionados, convierte el rango a
// instantes UTC (desde el inicio del dia `desde` hasta el final del dia
// `hasta`) y consulta GET /api/citas paginado. Concentra los estados de carga
// y error para que la pantalla solo presente.

import { useCallback, useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { aInstanteUtc, sumarDias } from '@/features/citas/fechas'
import { listarCitas } from '@/features/citas/services/citaService'
import type { EstadoCita, PaginaCitas } from '@/features/citas/types'

/** Filtros elegidos por el usuario (fechas locales de Bogota). */
export interface FiltrosCalendario {
  desde: string
  hasta: string
  estado?: EstadoCita
  pacienteId?: number
  pagina: number
  tamano: number
}

interface EstadoCalendario {
  datos: PaginaCitas | null
  cargando: boolean
  error: string | null
  recargar: () => void
}

/**
 * Calcula los instantes UTC del rango. `hasta` se envia como el inicio del dia
 * siguiente para que el rango incluya todo el ultimo dia elegido.
 */
function rangoUtc(desde: string, hasta: string): { desde: string; hasta: string } | null {
  const inicio = aInstanteUtc(desde, '00:00')
  const finExclusivo = aInstanteUtc(sumarDias(hasta, 1), '00:00')
  if (!inicio || !finExclusivo) return null
  return { desde: inicio, hasta: finExclusivo }
}

/** Carga el calendario de citas para los filtros dados. */
export function useCalendarioCitas(filtros: FiltrosCalendario): EstadoCalendario {
  const [datos, setDatos] = useState<PaginaCitas | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  const { desde, hasta, estado, pacienteId, pagina, tamano } = filtros

  useEffect(() => {
    let vigente = true
    const rango = rangoUtc(desde, hasta)
    if (!rango) {
      setError('El rango de fechas no es valido.')
      setCargando(false)
      return () => {
        vigente = false
      }
    }
    setCargando(true)
    setError(null)
    listarCitas({
      desde: rango.desde,
      hasta: rango.hasta,
      estado,
      pacienteId,
      pagina,
      tamano,
    })
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
  }, [desde, hasta, estado, pacienteId, pagina, tamano, reintento])

  const recargar = useCallback(() => setReintento((n) => n + 1), [])

  return { datos, cargando, error, recargar }
}