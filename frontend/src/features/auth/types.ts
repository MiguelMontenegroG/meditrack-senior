// Types del contrato REAL de autenticacion (POST /api/auth/login).
// Se declaran aqui para que el Paso 3 solo reemplace la implementacion
// simulada sin tocar las pantallas que los consumen.

/** Roles soportados por el backend. El FAMILIAR_AUTORIZADO es de solo lectura. */
export type RolUsuario = 'ADMINISTRADOR' | 'CUIDADOR_ENFERMERO' | 'FAMILIAR_AUTORIZADO'

/** Usuario tal como lo devuelve el backend dentro de la respuesta de login. */
export interface UsuarioAutenticado {
  id: number
  nombreCompleto: string
  correo: string
  rol: RolUsuario
}

/** Cuerpo que espera POST /api/auth/login. */
export interface CredencialesInicioSesion {
  correo: string
  contrasena: string
}

/** Respuesta 200 de POST /api/auth/login. */
export interface RespuestaInicioSesion {
  token: string
  tipo: 'Bearer'
  expiraEnSegundos: number
  usuario: UsuarioAutenticado
}

/** Forma del error del backend (ApiError). */
export interface ErrorApi {
  timestamp: string
  status: number
  codigo: 'CREDENCIALES_INVALIDAS' | 'NO_AUTENTICADO' | 'ACCESO_DENEGADO' | 'VALIDACION' | 'CUERPO_INVALIDO' | 'CONFLICTO'
  mensaje: string
}

/**
 * API publica del contexto de autenticacion.
 * En el Paso 2 la implementacion es simulada; en el Paso 3 sera real.
 */
export interface ContextoAutenticacion {
  usuario: UsuarioAutenticado | null
  token: string | null
  iniciarSesion: (credenciales: CredencialesInicioSesion, rolSimulado?: RolUsuario) => Promise<void>
  cerrarSesion: () => void
}
