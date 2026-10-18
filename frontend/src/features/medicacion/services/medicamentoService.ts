// Servicio del catalogo de medicamentos. Envuelve el cliente HTTP; no conoce
// el router ni el estado de la interfaz. Escritura solo ADMINISTRADOR; lectura
// tambien CUIDADOR_ENFERMERO (ver docs/api/openapi.yaml, tag "Medicamentos").

import { apiClient } from '@/lib/api-client'
import type {
  DatosMedicamento,
  FiltrosMedicamentos,
  Medicamento,
  PaginaMedicamentos,
} from '@/features/medicacion/types'

/** Compone el query string del listado omitiendo los filtros vacios. */
function construirQuery(filtros: FiltrosMedicamentos): string {
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

/** Lista el catalogo de medicamentos (paginado, con busqueda y estado). */
export function listarMedicamentos(
  filtros: FiltrosMedicamentos = {},
): Promise<PaginaMedicamentos> {
  return apiClient.get<PaginaMedicamentos>(`/medicamentos${construirQuery(filtros)}`)
}

/**
 * Lista medicamentos activos para los selectores del formulario de esquema.
 * Reutiliza el listado paginado pidiendo un tamano amplio.
 */
export async function listarMedicamentosActivos(): Promise<Medicamento[]> {
  const pagina = await listarMedicamentos({ activo: true, tamano: 100 })
  return pagina.contenido
}

/** Crea un medicamento en el catalogo (solo ADMINISTRADOR). */
export function crearMedicamento(datos: DatosMedicamento): Promise<Medicamento> {
  return apiClient.post<Medicamento>('/medicamentos', datos)
}

/** Actualiza un medicamento del catalogo (solo ADMINISTRADOR). */
export function actualizarMedicamento(
  id: number,
  datos: DatosMedicamento,
): Promise<Medicamento> {
  return apiClient.put<Medicamento>(`/medicamentos/${id}`, datos)
}

/** Activa o desactiva un medicamento (solo ADMINISTRADOR; borrado logico). */
export function cambiarEstadoMedicamento(id: number, activo: boolean): Promise<Medicamento> {
  return apiClient.patch<Medicamento>(`/medicamentos/${id}/estado`, { activo })
}
