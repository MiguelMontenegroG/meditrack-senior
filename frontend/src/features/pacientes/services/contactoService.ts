// Servicio de contactos de emergencia de un residente. Lectura segun alcance;
// escritura solo ADMINISTRADOR (ver docs/api/openapi.yaml).

import { apiClient } from '@/lib/api-client'
import type { ContactoEmergencia, DatosContacto } from '@/features/pacientes/types'

/** Lista los contactos de emergencia de un residente. */
export function listarContactos(pacienteId: number): Promise<ContactoEmergencia[]> {
  return apiClient.get<ContactoEmergencia[]>(`/pacientes/${pacienteId}/contactos`)
}

/** Crea un contacto de emergencia (solo ADMINISTRADOR). */
export function crearContacto(
  pacienteId: number,
  datos: DatosContacto,
): Promise<ContactoEmergencia> {
  return apiClient.post<ContactoEmergencia>(`/pacientes/${pacienteId}/contactos`, datos)
}

/** Actualiza un contacto de emergencia (solo ADMINISTRADOR). */
export function actualizarContacto(
  pacienteId: number,
  contactoId: number,
  datos: DatosContacto,
): Promise<ContactoEmergencia> {
  return apiClient.put<ContactoEmergencia>(
    `/pacientes/${pacienteId}/contactos/${contactoId}`,
    datos,
  )
}

/** Elimina un contacto de emergencia (solo ADMINISTRADOR). */
export function eliminarContacto(pacienteId: number, contactoId: number): Promise<void> {
  return apiClient.delete<void>(`/pacientes/${pacienteId}/contactos/${contactoId}`)
}
