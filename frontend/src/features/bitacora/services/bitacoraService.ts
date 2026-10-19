// Servicio de la bitacora de signos vitales. Solo HTTP: registra un subconjunto
// de signos (ADMINISTRADOR o CUIDADOR_ENFERMERO), consulta el historial
// paginado con filtro opcional de rango y obtiene el ultimo registro. Los
// registros son inmutables: no hay operaciones de edicion ni borrado. Ver
// docs/api/openapi.yaml, tag "Bitacora de signos vitales".

import { apiClient } from '@/lib/api-client'
import type {
  DatosSignosVitales,
  FiltrosSignosVitales,
  PaginaSignosVitales,
  SignosVitales,
} from '@/features/bitacora/types'

/** Compone el query string del historial omitiendo los filtros no informados. */
function construirQuery(filtros: FiltrosSignosVitales): string {
  const params = new URLSearchParams()
  if (filtros.desde) params.set('desde', filtros.desde)
  if (filtros.hasta) params.set('hasta', filtros.hasta)
  if (filtros.pagina !== undefined) params.set('pagina', String(filtros.pagina))
  if (filtros.tamano !== undefined) params.set('tamano', String(filtros.tamano))
  const query = params.toString()
  return query ? `?${query}` : ''
}

/** Registra signos vitales de un residente (solo ADMINISTRADOR o CUIDADOR). */
export function registrarSignosVitales(
  pacienteId: number,
  datos: DatosSignosVitales,
): Promise<SignosVitales> {
  return apiClient.post<SignosVitales>(`/pacientes/${pacienteId}/signos-vitales`, datos)
}

/**
 * Historial paginado de signos vitales del residente (los tres roles, lectura
 * segun alcance). Orden descendente: el registro mas reciente primero.
 */
export function listarSignosVitales(
  pacienteId: number,
  filtros: FiltrosSignosVitales = {},
): Promise<PaginaSignosVitales> {
  return apiClient.get<PaginaSignosVitales>(
    `/pacientes/${pacienteId}/signos-vitales${construirQuery(filtros)}`,
  )
}

/**
 * Ultimo registro de signos vitales del residente. El backend responde 404 si
 * el residente esta fuera de alcance o si no tiene registros; quien consume la
 * funcion decide como tratarlo (la interfaz lo muestra como "sin registros",
 * no como error).
 */
export function obtenerUltimoRegistro(pacienteId: number): Promise<SignosVitales> {
  return apiClient.get<SignosVitales>(`/pacientes/${pacienteId}/signos-vitales/ultimo`)
}
