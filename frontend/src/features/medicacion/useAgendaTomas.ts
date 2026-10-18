// Hook de carga de la agenda del dia de un residente. Concentra los estados
// (carga, no encontrado, error) y la recarga tras registrar una toma.

import { useCallback, useEffect, useState } from 'react'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { obtenerAgenda } from '@/features/medicacion/services/tomaService'
import type { AgendaEsquema } from '@/features/medicacion/types'

interface EstadoAgenda {
  agenda: AgendaEsquema[]
  cargando: boolean
  noEncontrado: boolean
  error: string | null
  recargar: () => Promise<void>
}

/** Carga la agenda de medicacion del dia de un residente. */
export function useAgendaTomas(pacienteId: number, fecha: string): EstadoAgenda {
  const [agenda, setAgenda] = useState<AgendaEsquema[]>([])
  const [cargando, setCargando] = useState(true)
  const [noEncontrado, setNoEncontrado] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const recargar = useCallback(async () => {
    setError(null)
    try {
      const resultado = await obtenerAgenda(pacienteId, fecha)
      setAgenda(resultado)
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.status === 404) {
        setNoEncontrado(true)
      } else {
        setError(mensajeDeFallo(fallo))
      }
    } finally {
      setCargando(false)
    }
  }, [pacienteId, fecha])

  useEffect(() => {
    setCargando(true)
    setNoEncontrado(false)
    void recargar()
  }, [recargar])

  return { agenda, cargando, noEncontrado, error, recargar }
}
