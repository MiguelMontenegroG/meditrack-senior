// Servicio de alertas. Solo HTTP: no conoce React ni el router. Los endpoints
// corresponden al AlertaController del backend (no documentados en
// docs/api/openapi.yaml).
//   - GET  /api/alertas                       (los tres roles, paginado y filtrable)
//   - PATCH /api/alertas/{id}/atender         (ADMINISTRADOR o CUIDADOR_ENFERMERO)

import { apiClient } from '@/lib/api-client'
import type { Alerta, FiltrosAlertas, PaginaAlertas } from '@/features/alertas/types'

/** Compone el query string del listado omitiendo los filtros vacios. */
function construirQuery(filtros: FiltrosAlertas): string {
  const params = new URLSearchParams()
  if (filtros.estado !== undefined) params.set('estado', filtros.estado)
  if (filtros.tipo !== undefined) params.set('tipo', filtros.tipo)
  if (filtros.pacienteId !== undefined) params.set('pacienteId', String(filtros.pacienteId))
  if (filtros.pagina !== undefined) params.set('pagina', String(filtros.pagina))
  if (filtros.tamano !== undefined) params.set('tamano', String(filtros.tamano))
  const query = params.toString()
  return query ? `?${query}` : ''
}

/** Listado paginado de alertas del alcance del usuario. */
export function listarAlertas(filtros: FiltrosAlertas = {}): Promise<PaginaAlertas> {
  return apiClient.get<PaginaAlertas>(`/alertas${construirQuery(filtros)}`)
}

/**
 * Atiende una alerta (ADMINISTRADOR o CUIDADOR_ENFERMERO). Ya atendida: 409
 * CONFLICTO; fuera de alcance o inexistente: 404.
 */
export function atenderAlerta(id: number): Promise<Alerta> {
  return apiClient.patch<Alerta>(`/alertas/${id}/atender`)
}
