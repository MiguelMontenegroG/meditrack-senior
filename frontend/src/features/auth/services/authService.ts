// Servicio de autenticacion: unico punto que conoce los endpoints de auth.
// Las pantallas y el contexto no llaman a fetch directamente, solo a este
// servicio.

import { apiClient } from '@/lib/api-client'
import type {
  CredencialesInicioSesion,
  RespuestaInicioSesion,
  UsuarioAutenticado,
} from '@/features/auth/types'

/** Inicia sesion contra POST /api/auth/login. */
export function iniciarSesion(credenciales: CredencialesInicioSesion): Promise<RespuestaInicioSesion> {
  // El login no adjunta token (es la ruta publica) y un 401 aqui significa
  // credenciales invalidas, no sesion expirada, por lo que no dispara el
  // manejador global de NO_AUTENTICADO.
  return apiClient.post<RespuestaInicioSesion>('/auth/login', credenciales, {
    conAuth: false,
    notificarNoAutenticado: false,
  })
}

/** Obtiene los datos del usuario autenticado (GET /api/auth/me). */
export function obtenerUsuarioActual(): Promise<UsuarioAutenticado> {
  return apiClient.get<UsuarioAutenticado>('/auth/me')
}
