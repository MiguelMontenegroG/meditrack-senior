// Servicio de citas medicas. Calendario paginado por rango (lectura segun
// alcance), detalle, agendar/editar (solo ADMINISTRADOR) y cambio de estado
// (ADMINISTRADOR o CUIDADOR_ENFERMERO). Ver docs/api/openapi.yaml, tag "Citas".

import { apiClient } from '@/lib/api-client'
import type {
  CitaDetalle,
  DatosCita,
  DestinoEstadoCita,
  FiltrosCitas,
  PaginaCitas,
} from '@/features/citas/types'

/**
 * Compone el query string del calendario. `desde` y `hasta` son obligatorios
 * (el backend responde 400 si faltan). Se envian siempre ya en ISO-8601 UTC.
 */
function construirQuery(filtros: FiltrosCitas): string {
  const params = new URLSearchParams()
  params.set('desde', filtros.desde)
  params.set('hasta', filtros.hasta)
  if (filtros.estado !== undefined) {
    params.set('estado', filtros.estado)
  }
  if (filtros.pacienteId !== undefined) {
    params.set('pacienteId', String(filtros.pacienteId))
  }
  if (filtros.pagina !== undefined) {
    params.set('pagina', String(filtros.pagina))
  }
  if (filtros.tamano !== undefined) {
    params.set('tamano', String(filtros.tamano))
  }
  return `?${params.toString()}`
}

/** Calendario de citas por rango de fechas, paginado y filtrable. */
export function listarCitas(filtros: FiltrosCitas): Promise<PaginaCitas> {
  return apiClient.get<PaginaCitas>(`/citas${construirQuery(filtros)}`)
}

/** Detalle de una cita de un residente (incluye motivo; lectura segun alcance). */
export function obtenerCita(pacienteId: number, citaId: number): Promise<CitaDetalle> {
  return apiClient.get<CitaDetalle>(`/pacientes/${pacienteId}/citas/${citaId}`)
}

/** Agenda una cita de un residente (solo ADMINISTRADOR). */
export function agendarCita(pacienteId: number, datos: DatosCita): Promise<CitaDetalle> {
  return apiClient.post<CitaDetalle>(`/pacientes/${pacienteId}/citas`, datos)
}

/** Edita una cita en estado PROGRAMADA (solo ADMINISTRADOR). */
export function editarCita(
  pacienteId: number,
  citaId: number,
  datos: DatosCita,
): Promise<CitaDetalle> {
  return apiClient.put<CitaDetalle>(`/pacientes/${pacienteId}/citas/${citaId}`, datos)
}

/**
 * Cambia el estado de una cita PROGRAMADA. CUMPLIDA/INCUMPLIDA (ADMINISTRADOR o
 * CUIDADOR_ENFERMERO, solo si la hora ya llego) o CANCELADA (solo
 * ADMINISTRADOR).
 */
export function cambiarEstadoCita(
  pacienteId: number,
  citaId: number,
  estado: DestinoEstadoCita,
): Promise<CitaDetalle> {
  return apiClient.patch<CitaDetalle>(`/pacientes/${pacienteId}/citas/${citaId}/estado`, {
    estado,
  })
}