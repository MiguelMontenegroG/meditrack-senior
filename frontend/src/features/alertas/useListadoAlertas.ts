// Hook de carga del listado de alertas. Recibe los filtros (estado, tipo,
// pagina) y consulta GET /api/alertas paginado. Concentra los estados de carga,
// error y reintento para que la pantalla solo presente.

import { useCallback, useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarAlertas } from '@/features/alertas/services/alertaService'
import type { EstadoAlerta, PaginaAlertas, TipoAlerta } from '@/features/alertas/types'

export interface FiltrosListadoAlertas {
  estado?: EstadoAlerta
  tipo?: TipoAlerta
  pagina: number
  tamano: number
}

interface EstadoListadoAlertas {
  datos: PaginaAlertas | null
  cargando: boolean
  error: string | null
  recargar: () => void
}

/** Carga el listado de alertas para los filtros dados. */
export function useListadoAlertas(filtros: FiltrosListadoAlertas): EstadoListadoAlertas {
  const [datos, setDatos] = useState<PaginaAlertas | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  const { estado, tipo, pagina, tamano } = filtros

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    listarAlertas({ estado, tipo, pagina, tamano })
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
  }, [estado, tipo, pagina, tamano, reintento])

  const recargar = useCallback(() => setReintento((n) => n + 1), [])

  return { datos, cargando, error, recargar }
}
