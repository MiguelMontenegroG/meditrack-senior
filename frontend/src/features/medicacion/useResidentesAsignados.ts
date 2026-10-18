// Hook de los residentes del alcance del cuidador. Reutiliza la carga de la
// jornada (GET /pacientes + agenda de hoy) y agrupa las tomas abiertas por
// residente para saber cuantas pendientes o atrasadas tiene cada uno.

import { useMemo } from 'react'
import { useTareasCuidador } from '@/features/medicacion/useTareasCuidador'

/** Resumen por residente del alcance del cuidador. */
export interface ResidenteAsignado {
  pacienteId: number
  pacienteNombre: string
  habitacion: string | null
  /** Tomas pendientes o atrasadas de hoy. */
  abiertas: number
  /** Subconjunto de `abiertas` que ya vencieron. */
  atrasadas: number
}

interface EstadoResidentes {
  residentes: ResidenteAsignado[]
  cargando: boolean
  error: string | null
  recargar: () => Promise<void>
}

/**
 * Residentes asignados al cuidador con el conteo de tomas abiertas de hoy.
 * La agenda ya se consulta por residente en useTareasCuidador (una por cada
 * uno del alcance, maximo la primera pagina); aqui solo se agrupa el
 * resultado, sin consultas adicionales.
 */
export function useResidentesAsignados(): EstadoResidentes {
  const { tareas, cargando, error, recargar } = useTareasCuidador()

  const residentes = useMemo(() => {
    const porId = new Map<number, ResidenteAsignado>()
    for (const tarea of tareas) {
      const actual = porId.get(tarea.pacienteId) ?? {
        pacienteId: tarea.pacienteId,
        pacienteNombre: tarea.pacienteNombre,
        habitacion: tarea.habitacion,
        abiertas: 0,
        atrasadas: 0,
      }
      actual.abiertas += 1
      if (tarea.estado === 'ATRASADA') actual.atrasadas += 1
      porId.set(tarea.pacienteId, actual)
    }
    return [...porId.values()].sort((a, b) => a.pacienteNombre.localeCompare(b.pacienteNombre))
  }, [tareas])

  return { residentes, cargando, error, recargar }
}
