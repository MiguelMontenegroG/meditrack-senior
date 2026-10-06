// Persistencia de la sesion en sessionStorage.
//
// Decision de diseno: se usa sessionStorage (no localStorage) para que la
// sesion se pierda al cerrar la pestana, reduciendo la exposicion del token
// en equipos compartidos de un centro geriatrico. Limitacion conocida: no hay
// refresh tokens; al expirar el JWT hay que volver a iniciar sesion.

import type { UsuarioAutenticado } from '@/features/auth/types'

const CLAVE_TOKEN = 'meditrack.token'
const CLAVE_USUARIO = 'meditrack.usuario'

/** Sesion persistida: token JWT y datos del usuario. */
export interface SesionGuardada {
  token: string
  usuario: UsuarioAutenticado
}

/** Guarda token y usuario en sessionStorage. */
export function guardarSesion(sesion: SesionGuardada): void {
  sessionStorage.setItem(CLAVE_TOKEN, sesion.token)
  sessionStorage.setItem(CLAVE_USUARIO, JSON.stringify(sesion.usuario))
}

/** Lee la sesion guardada. Devuelve null si falta o esta corrupta. */
export function leerSesion(): SesionGuardada | null {
  const token = sessionStorage.getItem(CLAVE_TOKEN)
  const usuarioCrudo = sessionStorage.getItem(CLAVE_USUARIO)
  if (!token || !usuarioCrudo) {
    return null
  }
  try {
    const usuario = JSON.parse(usuarioCrudo) as UsuarioAutenticado
    return { token, usuario }
  } catch {
    // Datos locales corruptos: se descartan para forzar un login limpio.
    limpiarSesion()
    return null
  }
}

/** Elimina la sesion de sessionStorage. */
export function limpiarSesion(): void {
  sessionStorage.removeItem(CLAVE_TOKEN)
  sessionStorage.removeItem(CLAVE_USUARIO)
}
