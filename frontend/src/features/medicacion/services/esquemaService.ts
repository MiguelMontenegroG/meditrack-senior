// Servicio de esquemas de medicacion de un residente. Lectura segun alcance;
// escritura solo ADMINISTRADOR (ver docs/api/openapi.yaml, tag
// "Esquemas de medicacion").

import { apiClient } from '@/lib/api-client'
import type {
  DatosEsquema,
  EsquemaMedicacion,
  FiltrosEsquemas,
} from '@/features/medicacion/types'

/** Compone el query string del listado de esquemas. */
function construirQuery(filtros: FiltrosEsquemas): string {
  const params = new URLSearchParams()
  if (filtros.activo !== undefined) {
    params.set('activo', String(filtros.activo))
  }
  if (filtros.vigente !== undefined) {
    params.set('vigente', String(filtros.vigente))
  }
  const query = params.toString()
  return query ? `?${query}` : ''
}

/** Lista los esquemas de un residente (lectura segun alcance). */
export function listarEsquemas(
  pacienteId: number,
  filtros: FiltrosEsquemas = {},
): Promise<EsquemaMedicacion[]> {
  return apiClient.get<EsquemaMedicacion[]>(
    `/pacientes/${pacienteId}/esquemas${construirQuery(filtros)}`,
  )
}

/** Detalle de un esquema de un residente. */
export function obtenerEsquema(
  pacienteId: number,
  esquemaId: number,
): Promise<EsquemaMedicacion> {
  return apiClient.get<EsquemaMedicacion>(`/pacientes/${pacienteId}/esquemas/${esquemaId}`)
}

/** Crea un esquema con sus horarios (solo ADMINISTRADOR). */
export function crearEsquema(
  pacienteId: number,
  datos: DatosEsquema,
): Promise<EsquemaMedicacion> {
  return apiClient.post<EsquemaMedicacion>(`/pacientes/${pacienteId}/esquemas`, datos)
}

/** Actualiza un esquema y reemplaza sus horarios (solo ADMINISTRADOR). */
export function actualizarEsquema(
  pacienteId: number,
  esquemaId: number,
  datos: DatosEsquema,
): Promise<EsquemaMedicacion> {
  return apiClient.put<EsquemaMedicacion>(
    `/pacientes/${pacienteId}/esquemas/${esquemaId}`,
    datos,
  )
}

/** Activa o suspende un esquema (solo ADMINISTRADOR; borrado logico). */
export function cambiarEstadoEsquema(
  pacienteId: number,
  esquemaId: number,
  activo: boolean,
): Promise<EsquemaMedicacion> {
  return apiClient.patch<EsquemaMedicacion>(
    `/pacientes/${pacienteId}/esquemas/${esquemaId}/estado`,
    { activo },
  )
}
