// Servicio de tomas de medicacion. Registro (ADMINISTRADOR o
// CUIDADOR_ENFERMERO con alcance), agenda del dia e historial paginado
// (lectura segun alcance). Ver docs/api/openapi.yaml, tag "Tomas".

import { apiClient } from '@/lib/api-client'
import type {
  AgendaEsquema,
  DatosRegistroToma,
  FiltrosHistorialTomas,
  PaginaTomas,
  RegistroToma,
} from '@/features/medicacion/types'

/**
 * Registra la toma de un esquema. Lanza 409 si esa hora ya tiene una toma o
 * si el esquema no esta vigente; 400 si la hora no corresponde a un horario.
 */
export function registrarToma(
  pacienteId: number,
  esquemaId: number,
  datos: DatosRegistroToma,
): Promise<RegistroToma> {
  return apiClient.post<RegistroToma>(
    `/pacientes/${pacienteId}/esquemas/${esquemaId}/tomas`,
    datos,
  )
}

/** Agenda de medicacion del dia del residente (estados calculados). */
export function obtenerAgenda(pacienteId: number, fecha?: string): Promise<AgendaEsquema[]> {
  const query = fecha ? `?fecha=${encodeURIComponent(fecha)}` : ''
  return apiClient.get<AgendaEsquema[]>(`/pacientes/${pacienteId}/tomas/agenda${query}`)
}

/** Compone el query string del historial omitiendo los filtros vacios. */
function construirQuery(filtros: FiltrosHistorialTomas): string {
  const params = new URLSearchParams()
  if (filtros.esquemaId !== undefined) {
    params.set('esquemaId', String(filtros.esquemaId))
  }
  if (filtros.desde) {
    params.set('desde', filtros.desde)
  }
  if (filtros.hasta) {
    params.set('hasta', filtros.hasta)
  }
  if (filtros.pagina !== undefined) {
    params.set('pagina', String(filtros.pagina))
  }
  if (filtros.tamano !== undefined) {
    params.set('tamano', String(filtros.tamano))
  }
  const query = params.toString()
  return query ? `?${query}` : ''
}

/** Historial de tomas de un residente, paginado y filtrable. */
export function listarHistorialTomas(
  pacienteId: number,
  filtros: FiltrosHistorialTomas = {},
): Promise<PaginaTomas> {
  return apiClient.get<PaginaTomas>(
    `/pacientes/${pacienteId}/tomas/historial${construirQuery(filtros)}`,
  )
}
