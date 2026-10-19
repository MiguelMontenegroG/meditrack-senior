// Servicio del catalogo de especialidades medicas (solo lectura, los tres
// roles). Ver docs/api/openapi.yaml, tag "Especialidades".

import { apiClient } from '@/lib/api-client'
import type { Especialidad } from '@/features/citas/types'

/** Lista el catalogo completo de especialidades medicas. */
export function listarEspecialidades(): Promise<Especialidad[]> {
  return apiClient.get<Especialidad[]>('/especialidades')
}