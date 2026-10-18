// Hook de la jornada del cuidador: a partir del listado de residentes de su
// alcance, carga la agenda de medicacion del dia de cada uno y agrega las tomas
// pendientes o atrasadas como tareas. Consume endpoints reales
// (GET /pacientes y GET /pacientes/{id}/tomas/agenda); NO usa mocks.

import { useCallback, useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { obtenerAgenda } from '@/features/medicacion/services/tomaService'
import { hoyIso, horaLocal } from '@/features/medicacion/fechas'
import { listarPacientes } from '@/features/pacientes/services/pacienteService'
import type { EstadoSlotToma } from '@/features/medicacion/types'

/** Toma pendiente o atrasada de un residente del alcance del cuidador. */
export interface TareaCuidador {
  pacienteId: number
  pacienteNombre: string
  habitacion: string | null
  esquemaId: number
  medicamentoNombre: string
  dosis: string
  fechaHoraProgramada: string
  hora: string
  estado: Extract<EstadoSlotToma, 'PENDIENTE' | 'ATRASADA'>
}

interface EstadoJornada {
  tareas: TareaCuidador[]
  /** Total de tomas del dia (todas, incluidas administradas/omitidas). */
  total: number
  cargando: boolean
  error: string | null
  recargar: () => Promise<void>
}

const TAMANO_RESIDENTES = 100

/** Carga la agenda del dia de los residentes del alcance del cuidador. */
export function useTareasCuidador(): EstadoJornada {
  const [tareas, setTareas] = useState<TareaCuidador[]>([])
  const [total, setTotal] = useState(0)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const recargar = useCallback(async () => {
    setError(null)
    try {
      const pacientes = await listarPacientes({ activo: true, tamano: TAMANO_RESIDENTES })
      const agendas = await Promise.all(
        pacientes.contenido.map(async (paciente) => ({
          paciente,
          agenda: await obtenerAgenda(paciente.id, hoyIso()),
        })),
      )
      const acumuladas: TareaCuidador[] = []
      let conteoTotal = 0
      for (const { paciente, agenda } of agendas) {
        for (const esquema of agenda) {
          for (const slot of esquema.slots) {
            conteoTotal += 1
            if (slot.estado === 'PENDIENTE' || slot.estado === 'ATRASADA') {
              acumuladas.push({
                pacienteId: paciente.id,
                pacienteNombre: paciente.nombreCompleto,
                habitacion: paciente.habitacion,
                esquemaId: esquema.esquemaId,
                medicamentoNombre:
                  esquema.medicamentoNombre ?? `Medicamento #${esquema.medicamentoId}`,
                dosis: esquema.dosis,
                fechaHoraProgramada: slot.fechaHoraProgramada,
                hora: horaLocal(slot.fechaHoraProgramada),
                estado: slot.estado,
              })
            }
          }
        }
      }
      acumuladas.sort((a, b) => a.fechaHoraProgramada.localeCompare(b.fechaHoraProgramada))
      setTareas(acumuladas)
      setTotal(conteoTotal)
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    } finally {
      setCargando(false)
    }
  }, [])

  useEffect(() => {
    void recargar()
  }, [recargar])

  return { tareas, total, cargando, error, recargar }
}
