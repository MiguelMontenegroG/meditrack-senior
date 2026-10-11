// Servicio de asignacion de cuidadores a residentes (solo ADMINISTRADOR).
// Ver docs/api/openapi.yaml.

import { apiClient } from '@/lib/api-client'
import type { AsignacionCuidador } from '@/features/pacientes/types'

/** Lista las asignaciones vigentes de cuidadores de un residente. */
export function listarCuidadores(pacienteId: number): Promise<AsignacionCuidador[]> {
  return apiClient.get<AsignacionCuidador[]>(`/pacientes/${pacienteId}/cuidadores`)
}

/** Asigna un cuidador (usuario con rol CUIDADOR_ENFERMERO) a un residente. */
export function asignarCuidador(
  pacienteId: number,
  usuarioId: number,
): Promise<AsignacionCuidador> {
  return apiClient.post<AsignacionCuidador>(`/pacientes/${pacienteId}/cuidadores`, {
    usuarioId,
  })
}

/** Desasigna un cuidador (hasta = hoy, no borra la asignacion). */
export function desasignarCuidador(pacienteId: number, usuarioId: number): Promise<void> {
  return apiClient.delete<void>(`/pacientes/${pacienteId}/cuidadores/${usuarioId}`)
}
