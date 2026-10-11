// Servicio de vinculacion de familiares con residentes (solo ADMINISTRADOR).
// Ver docs/api/openapi.yaml.

import { apiClient } from '@/lib/api-client'
import type { VinculacionFamiliar } from '@/features/pacientes/types'

/** Lista los familiares vinculados a un residente. */
export function listarFamiliares(pacienteId: number): Promise<VinculacionFamiliar[]> {
  return apiClient.get<VinculacionFamiliar[]>(`/pacientes/${pacienteId}/familiares`)
}

/** Vincula un familiar (usuario con rol FAMILIAR_AUTORIZADO) a un residente. */
export function vincularFamiliar(
  pacienteId: number,
  usuarioId: number,
  parentesco: string | null,
): Promise<VinculacionFamiliar> {
  return apiClient.post<VinculacionFamiliar>(`/pacientes/${pacienteId}/familiares`, {
    usuarioId,
    parentesco,
  })
}

/** Revoca la vinculacion de un familiar (autorizado = false, no borra). */
export function revocarFamiliar(pacienteId: number, usuarioId: number): Promise<void> {
  return apiClient.delete<void>(`/pacientes/${pacienteId}/familiares/${usuarioId}`)
}
