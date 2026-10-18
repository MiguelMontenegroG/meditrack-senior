// Hook del panel de administrador: carga los residentes activos de su alcance,
// la agenda de medicacion de hoy de cada uno y los totales del catalogo, y
// agrega los contadores del panel. Consume endpoints reales (GET /pacientes,
// GET /medicamentos y GET /pacientes/{id}/tomas/agenda); NO usa mocks. Si una
// agenda falla, el resumen se calcula con las que si respondieron y se informa
// el error para que el panel no quede completamente vacio.

import { useCallback, useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { horaLocal, hoyIso } from '@/features/medicacion/fechas'
import { obtenerAgenda } from '@/features/medicacion/services/tomaService'
import { listarMedicamentos } from '@/features/medicacion/services/medicamentoService'
import { listarPacientes } from '@/features/pacientes/services/pacienteService'
import {
  RESUMEN_VACIO,
  acumularResumen,
  porcentajeAvance,
  serieTomasDeHoy,
} from '@/features/dashboard/resumenOperativo'
import type { DatosResumenOperativo, PuntoCumplimiento } from '@/features/dashboard/types'

const TAMANO_RESIDENTES = 100

interface EstadoResumenOperativo {
  resumen: DatosResumenOperativo
  /** Total de residentes activos segun el campo `total` de GET /pacientes. */
  totalResidentes: number
  /** Total de medicamentos activos segun el campo `total` de GET /medicamentos. */
  totalMedicamentos: number
  /** Porcentaje de tomas cerradas hoy (administradas u omitidas). */
  avance: number
  /** Serie del grafico (tomas programadas hoy). */
  serie: PuntoCumplimiento[]
  /** Tomas de hoy que siguen pendientes o atrasadas (alertas de medicacion). */
  tomasAbiertas: TomaAbierta[]
  cargando: boolean
  error: string | null
  recargar: () => Promise<void>
}

/** Toma de hoy que aun no se registra, con el residente al que pertenece. */
export interface TomaAbierta {
  pacienteId: number
  pacienteNombre: string
  medicamentoNombre: string
  dosis: string
  hora: string
  estado: 'PENDIENTE' | 'ATRASADA'
}

/** Resumen operativo del panel con datos reales de medicacion. */
export function useResumenOperativo(): EstadoResumenOperativo {
  const [resumen, setResumen] = useState<DatosResumenOperativo>(RESUMEN_VACIO)
  const [totalResidentes, setTotalResidentes] = useState(0)
  const [totalMedicamentos, setTotalMedicamentos] = useState(0)
  const [tomasAbiertas, setTomasAbiertas] = useState<TomaAbierta[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const recargar = useCallback(async () => {
    setError(null)
    try {
      // Una sola pagina alcanza para el resumen: los conteos usan `total`
      // (que cuenta todas las filas del servidor, no solo esta pagina).
      const [pacientes, medicamentos] = await Promise.all([
        listarPacientes({ activo: true, tamano: TAMANO_RESIDENTES }),
        listarMedicamentos({ activo: true, tamano: 1 }),
      ])
      setTotalResidentes(pacientes.total)
      setTotalMedicamentos(medicamentos.total)

      const agendas = await Promise.allSettled(
        pacientes.contenido.map(async (paciente) => ({
          paciente,
          agenda: await obtenerAgenda(paciente.id, hoyIso()),
        })),
      )
      const resumenAgregado = agendas.reduce<DatosResumenOperativo>((acumulado, resultado) => {
        if (resultado.status !== 'fulfilled') return acumulado
        return acumularResumen(acumulado, resultado.value.agenda)
      }, RESUMEN_VACIO)

      const abiertas: TomaAbierta[] = []
      for (const resultado of agendas) {
        if (resultado.status !== 'fulfilled') continue
        const { paciente, agenda } = resultado.value
        for (const esquema of agenda) {
          for (const slot of esquema.slots) {
            if (slot.estado !== 'PENDIENTE' && slot.estado !== 'ATRASADA') continue
            abiertas.push({
              pacienteId: paciente.id,
              pacienteNombre: paciente.nombreCompleto,
              medicamentoNombre:
                esquema.medicamentoNombre ?? `Medicamento #${esquema.medicamentoId}`,
              dosis: esquema.dosis,
              hora: horaLocal(slot.fechaHoraProgramada),
              estado: slot.estado,
            })
          }
        }
      }
      // Las atrasadas van primero: son las que exigen accion inmediata.
      abiertas.sort((a, b) => {
        if (a.estado !== b.estado) return a.estado === 'ATRASADA' ? -1 : 1
        return a.hora.localeCompare(b.hora)
      })

      setResumen(resumenAgregado)
      setTomasAbiertas(abiertas)
      if (agendas.some((resultado) => resultado.status === 'rejected')) {
        setError('Parte de la agenda de hoy no se pudo cargar; las cifras pueden estar incompletas.')
      }
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
      setResumen(RESUMEN_VACIO)
      setTomasAbiertas([])
      setTotalResidentes(0)
      setTotalMedicamentos(0)
    } finally {
      setCargando(false)
    }
  }, [])

  useEffect(() => {
    void recargar()
  }, [recargar])

  return {
    resumen,
    totalResidentes,
    totalMedicamentos,
    avance: porcentajeAvance(resumen),
    serie: serieTomasDeHoy(resumen.tomasTotales),
    tomasAbiertas,
    cargando,
    error,
    recargar,
  }
}
