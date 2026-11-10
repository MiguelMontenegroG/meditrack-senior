// Servicio de reportes. Solo HTTP: no conoce React ni el router. Los endpoints
// corresponden a ReporteController (ver docs/api/openapi.yaml, tag "Reportes").
//   - GET /api/pacientes/{id}/reportes          (historial paginado, lectura)
//   - GET /api/pacientes/{id}/reportes/{tipo}.pdf (genera y descarga el PDF)

import { apiClient, type RespuestaBinaria } from '@/lib/api-client'
import type { FiltrosReportes, PaginaReportes } from '@/features/reportes/types'

/** Compone el query string del historial omitiendo los parametros vacios. */
function construirQuery(filtros: FiltrosReportes): string {
  const params = new URLSearchParams()
  if (filtros.pagina !== undefined) params.set('pagina', String(filtros.pagina))
  if (filtros.tamano !== undefined) params.set('tamano', String(filtros.tamano))
  const query = params.toString()
  return query ? `?${query}` : ''
}

/** Historial paginado de reportes generados de un residente (lectura). */
export function listarReportes(
  pacienteId: number,
  filtros: FiltrosReportes = {},
): Promise<PaginaReportes> {
  return apiClient.get<PaginaReportes>(
    `/pacientes/${pacienteId}/reportes${construirQuery(filtros)}`,
  )
}

/**
 * Genera y descarga el PDF de un reporte. El rango se envia en ISO-8601 UTC.
 * Devuelve el Blob y el nombre de archivo sugerido por el backend.
 */
export function descargarReportePdf(
  pacienteId: number,
  tipo: string,
  rango: { desde: string; hasta: string },
): Promise<RespuestaBinaria> {
  const params = new URLSearchParams({ desde: rango.desde, hasta: rango.hasta })
  return apiClient.descargarBinario(
    `/pacientes/${pacienteId}/reportes/${tipo}.pdf?${params.toString()}`,
  )
}
