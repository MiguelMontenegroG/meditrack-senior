// Types del contrato REAL de autenticacion (POST /api/auth/login).
// Ver docs/api/openapi.yaml para el detalle de cada esquema.
/** Roles soportados por el backend. El FAMILIAR_AUTORIZADO es de solo lectura. */
export type RolUsuario = 'ADMINISTRADOR' | 'CUIDADOR_ENFERMERO' | 'FAMILIAR_AUTORIZADO'

/** Usuario tal como lo devuelve el backend (login y GET /api/auth/me). */
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
  codigo: string
  mensaje: string
  detalles?: string[] | null
}

/** Roles con derecho a navegar a la ruta inicial de cada panel. */
export const RUTA_INICIAL_ROL: Record<RolUsuario, string> = {
  ADMINISTRADOR: '/dashboard',
  CUIDADOR_ENFERMERO: '/cuidador',
  FAMILIAR_AUTORIZADO: '/familiar',
}

/**
 * API publica del contexto de autenticacion.
 * Implementacion real contra el backend (Paso 3).
 */
export interface ContextoAutenticacion {
  usuario: UsuarioAutenticado | null
  token: string | null
  /** true mientras se valida una sesion guardada al cargar la app. */
  cargando: boolean
  /** Mensaje de error de la ultima operacion de sesion (o null). */
  errorSesion: string | null
  iniciarSesion: (credenciales: CredencialesInicioSesion) => Promise<UsuarioAutenticado>
  cerrarSesion: () => void
}
