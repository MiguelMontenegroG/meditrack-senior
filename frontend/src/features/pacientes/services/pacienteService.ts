// Servicio de residentes (pacientes). Envuelve el cliente HTTP; no conoce el
// router ni el estado de la interfaz. Los endpoints corresponden al modulo
// paciente del backend (ver docs/api/openapi.yaml).

import { apiClient } from '@/lib/api-client'
import type {
  DatosPaciente,
  FiltrosPacientes,
  PacienteDetalle,
  PaginaPacientes,
} from '@/features/pacientes/types'

/** Compone el query string del listado omitiendo los filtros vacios. */
function construirQuery(filtros: FiltrosPacientes): string {
  const params = new URLSearchParams()
  if (filtros.texto && filtros.texto.trim()) {
    params.set('texto', filtros.texto.trim())
  }
  if (filtros.activo !== undefined) {
    params.set('activo', String(filtros.activo))
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

/** Lista residentes segun el alcance del usuario autenticado (paginado). */
export function listarPacientes(filtros: FiltrosPacientes = {}): Promise<PaginaPacientes> {
  return apiClient.get<PaginaPacientes>(`/pacientes${construirQuery(filtros)}`)
}

/** Detalle de un residente. Fuera de alcance o inexistente lanza 404. */
export function obtenerPaciente(id: number): Promise<PacienteDetalle> {
  return apiClient.get<PacienteDetalle>(`/pacientes/${id}`)
}

/** Crea un residente (solo ADMINISTRADOR). */
export function crearPaciente(datos: DatosPaciente): Promise<PacienteDetalle> {
  return apiClient.post<PacienteDetalle>('/pacientes', datos)
}

/** Actualiza los datos de un residente (solo ADMINISTRADOR). */
export function actualizarPaciente(id: number, datos: DatosPaciente): Promise<PacienteDetalle> {
  return apiClient.put<PacienteDetalle>(`/pacientes/${id}`, datos)
}

/** Activa o desactiva un residente (solo ADMINISTRADOR; borrado logico). */
export function cambiarEstadoPaciente(id: number, activo: boolean): Promise<PacienteDetalle> {
  return apiClient.patch<PacienteDetalle>(`/pacientes/${id}/estado`, { activo })
}
