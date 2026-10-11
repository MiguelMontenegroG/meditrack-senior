// Servicio de usuarios para vincular familiares y asignar cuidadores.
// Solo ADMINISTRADOR puede listar usuarios (ver docs/api/openapi.yaml).

import { apiClient } from '@/lib/api-client'
import type { RolUsuario } from '@/features/pacientes/types'

/** Usuario tal como lo devuelve GET /api/usuarios (sin datos sensibles). */
export interface UsuarioListado {
  id: number
  nombreCompleto: string
  correo: string
  telefono: string | null
  rol: RolUsuario
  activo: boolean
}

/** Pagina de usuarios devuelta por el backend. */
export interface PaginaUsuarios {
  contenido: UsuarioListado[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Lista usuarios activos de un rol concreto (para el selector de vinculacion). */
export async function listarUsuariosPorRol(
  rol: RolUsuario,
  activo = true,
): Promise<UsuarioListado[]> {
  const params = new URLSearchParams({ rol, activo: String(activo), tamano: '100' })
  const pagina = await apiClient.get<PaginaUsuarios>(`/usuarios?${params.toString()}`)
  return pagina.contenido
}
