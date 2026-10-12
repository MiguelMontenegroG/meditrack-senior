// Servicio de usuarios. Envuelve el cliente HTTP; no conoce el router ni el
// estado de la interfaz. Todos los endpoints requieren rol ADMINISTRADOR
// (ver docs/api/openapi.yaml, tag "Usuarios").

import { apiClient } from '@/lib/api-client'
import type {
  DatosActualizarUsuario,
  DatosCrearUsuario,
  FiltrosUsuarios,
  PaginaUsuarios,
  RolUsuario,
  Usuario,
} from '@/features/usuarios/types'

/** Compone el query string del listado omitiendo los filtros vacios. */
function construirQuery(filtros: FiltrosUsuarios): string {
  const params = new URLSearchParams()
  if (filtros.rol) {
    params.set('rol', filtros.rol)
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

/** Lista usuarios con filtros por rol y estado (paginado). */
export function listarUsuarios(filtros: FiltrosUsuarios = {}): Promise<PaginaUsuarios> {
  return apiClient.get<PaginaUsuarios>(`/usuarios${construirQuery(filtros)}`)
}

/** Detalle de un usuario por id. */
export function obtenerUsuario(id: number): Promise<Usuario> {
  return apiClient.get<Usuario>(`/usuarios/${id}`)
}

/** Crea un usuario (solo ADMINISTRADOR). */
export function crearUsuario(datos: DatosCrearUsuario): Promise<Usuario> {
  return apiClient.post<Usuario>('/usuarios', datos)
}

/** Actualiza nombre, telefono y rol de un usuario (solo ADMINISTRADOR). */
export function actualizarUsuario(id: number, datos: DatosActualizarUsuario): Promise<Usuario> {
  return apiClient.put<Usuario>(`/usuarios/${id}`, datos)
}

/** Activa o desactiva un usuario (solo ADMINISTRADOR; borrado logico). */
export function cambiarEstadoUsuario(id: number, activo: boolean): Promise<Usuario> {
  return apiClient.patch<Usuario>(`/usuarios/${id}/estado`, { activo })
}

/**
 * Lista usuarios activos de un rol concreto (para los selectores de
 * vinculacion de familiares y asignacion de cuidadores del detalle).
 */
export async function listarUsuariosPorRol(
  rol: RolUsuario,
  activo = true,
): Promise<Usuario[]> {
  const pagina = await listarUsuarios({ rol, activo, tamano: 100 })
  return pagina.contenido
}
