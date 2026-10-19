// Hook del panel de administrador: proximas citas (de cualquier residente del
// alcance) para hoy y los siguientes dias. Usa GET /api/citas con un rango desde
// hoy hasta +30 dias, se queda con las PROGRAMADAS y las ordena por fecha/hora
// ascendente; toma las primeras N. Los nombres de residente se resuelven con
// GET /pacientes de forma tolerante: si ese listado falla, la cita se muestra
// con un identificador generico en vez de romper el panel.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { aInstanteUtc, hoyBogota, sumarDias } from '@/features/citas/fechas'
import { listarCitas } from '@/features/citas/services/citaService'
import type { CitaDetalle } from '@/features/citas/types'
import { listarPacientes } from '@/features/pacientes/services/pacienteService'

const LIMITE = 5
const TAMANO = 100

interface EstadoProximasPanel {
  citas: CitaDetalle[]
  nombrePorResidente: (pacienteId: number) => string
  cargando: boolean
  error: string | null
  recargar: () => void
}

/** Proximas citas programadas del alcance, con nombres resueltos por mejor esfuerzo. */
export function useProximasCitasPanel(): EstadoProximasPanel {
  const [citas, setCitas] = useState<CitaDetalle[]>([])
  const [nombres, setNombres] = useState<Map<number, string>>(new Map())
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
    // Las citas son la fuente principal; los nombres son best-effort.
    Promise.allSettled([
      listarCitas({ desde, hasta, estado: 'PROGRAMADA', pagina: 0, tamano: TAMANO }),
      listarPacientes({ activo: true, pagina: 0, tamano: TAMANO }),
    ])
      .then(([resultadoCitas, resultadoResidentes]) => {
        if (!vigente) return
        if (resultadoResidentes.status === 'fulfilled') {
          const mapa = new Map<number, string>()
          for (const residente of resultadoResidentes.value.contenido) {
            mapa.set(residente.id, residente.nombreCompleto)
          }
          setNombres(mapa)
        }
        if (resultadoCitas.status === 'fulfilled') {
          const programadas = [...resultadoCitas.value.contenido]
            .sort((a, b) => a.fechaHora.localeCompare(b.fechaHora))
            .slice(0, LIMITE)
          setCitas(programadas)
        } else {
          setError(mensajeDeFallo(resultadoCitas.reason))
        }
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [reintento])

  return {
    citas,
    nombrePorResidente: (id: number) => nombres.get(id) ?? `Residente #${id}`,
    cargando,
    error,
    recargar: () => setReintento((n) => n + 1),
  }
}