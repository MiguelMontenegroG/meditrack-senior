// Hook de las proximas citas de un residente para la seccion del detalle. Usa
// GET /api/citas con pacienteId y un rango desde hoy hasta +30 dias (hora de
// Bogota convertida a UTC). Se queda con las PROGRAMADAS de las primeras N.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { aInstanteUtc, hoyBogota, sumarDias } from '@/features/citas/fechas'
import { listarCitas } from '@/features/citas/services/citaService'
import type { CitaDetalle } from '@/features/citas/types'

const LIMITE = 5
const TAMANO = 100

interface EstadoProximas {
  citas: CitaDetalle[]
  cargando: boolean
  error: string | null
  recargar: () => void
}

/** Proximas citas programadas del residente (maximo LIMITE). */
export function useProximasCitas(pacienteId: number): EstadoProximas {
  const [citas, setCitas] = useState<CitaDetalle[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    const hoy = hoyBogota()
    const desde = aInstanteUtc(hoy, '00:00')
    const hasta = aInstanteUtc(sumarDias(hoy, 30), '23:59')
    if (!desde || !hasta) {
      setError('No pudimos calcular el rango de fechas.')
      setCargando(false)
      return () => {
        vigente = false
      }
    }
    listarCitas({ desde, hasta, pacienteId, pagina: 0, tamano: TAMANO })
      .then((resultado) => {
        if (!vigente) return
        const programadas = resultado.contenido
          .filter((cita) => cita.estado === 'PROGRAMADA')
          .slice(0, LIMITE)
        setCitas(programadas)
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
  }, [pacienteId, reintento])

  return { citas, cargando, error, recargar: () => setReintento((n) => n + 1) }
}