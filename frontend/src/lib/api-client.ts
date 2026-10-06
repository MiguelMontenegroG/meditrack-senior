// Cliente HTTP unico de la aplicacion. Envuelve fetch nativo: no se usan
// librerias de datos (axios, react-query, etc.).
//
// Responsabilidades:
// - Resolver la URL base desde import.meta.env.VITE_API_URL (nunca hardcodeada).
// - Adjuntar el encabezado Authorization: Bearer <token> cuando hay sesion.
// - Parsear el error del backend (ApiError) y lanzar ErrorApi con status/codigo.
// - Manejar respuestas sin cuerpo y errores de red.
// - Ante 401 NO_AUTENTICADO, avisar mediante un callback (registrado por el
//   AuthContext) para limpiar la sesion y redirigir a /login, sin acoplar el
//   cliente al router.

import { mensajeDeError } from '@/lib/mensajes-error'

/** Configuracion de la API. La URL base se resuelve una sola vez. */
function resolverBaseUrl(): string {
  const base = import.meta.env.VITE_API_URL
  if (!base) {
    // Falla temprano y con un mensaje claro en desarrollo: mejor que hacer
    // peticiones a una URL relativa equivocada.
    throw new ErrorApi({
      status: 0,
      codigo: 'ERROR_INESPERADO',
      mensaje: 'Falta configurar VITE_API_URL (ver frontend/.env.example).',
    })
  }
  // Normaliza la barra final para componer rutas sin duplicarla.
  return base.replace(/\/+$/, '')
}

/** Error de dominio del cliente HTTP con el status y el codigo del backend. */
export class ErrorApi extends Error {
  readonly status: number
  readonly codigo: string

  constructor(opciones: { status: number; codigo: string; mensaje?: string }) {
    super(opciones.mensaje ?? mensajeDeError(opciones.codigo))
    this.name = 'ErrorApi'
    this.status = opciones.status
    this.codigo = opciones.codigo
  }

  /** Mensaje en espanol listo para mostrar al usuario. */
  get mensajeUsuario(): string {
    return mensajeDeError(this.codigo)
  }
}

/** Obtiene el token de sesion actual. Devuelve null si no hay sesion. */
type TokenGetter = () => string | null

/** Callback invocado cuando el backend responde 401 NO_AUTENTICADO. */
type NoAutenticadoHandler = () => void

let obtenerToken: TokenGetter = () => null
let alNoAutenticado: NoAutenticadoHandler = () => {}

/** Registra de donde leer el token. Lo llama el AuthContext al montarse. */
export function registrarToken(getter: TokenGetter): void {
  obtenerToken = getter
}

/** Registra que hacer ante un 401 NO_AUTENTICADO (limpiar sesion + /login). */
export function registrarManejadorNoAutenticado(handler: NoAutenticadoHandler): void {
  alNoAutenticado = handler
}

interface OpcionesPeticion {
  method?: string
  body?: unknown
  /** Si es false, no adjunta el token aunque exista (p. ej. login). */
  conAuth?: boolean
  /** Fuerza el disparo del callback 401 aunque la ruta no sea protegida. */
  notificarNoAutenticado?: boolean
}

/** Interpreta el cuerpo de una respuesta de error del backend (ApiError). */
async function leerError(respuesta: Response): Promise<ErrorApi> {
  let codigo = 'ERROR_INESPERADO'
  try {
    const datos = (await respuesta.json()) as { codigo?: string }
    if (datos && typeof datos.codigo === 'string') {
      codigo = datos.codigo
    }
  } catch {
    // Respuesta sin cuerpo o no JSON: se mantiene el codigo generico.
  }
  return new ErrorApi({ status: respuesta.status, codigo })
}

/**
 * Ejecuta una peticion al backend y devuelve el cuerpo ya parseado.
 * Lanza ErrorApi ante cualquier fallo (HTTP, red o respuesta invalida).
 */
async function peticion<T>(ruta: string, opciones: OpcionesPeticion = {}): Promise<T> {
  const { method = 'GET', body, conAuth = true, notificarNoAutenticado = true } = opciones
  const url = `${resolverBaseUrl()}${ruta.startsWith('/') ? ruta : `/${ruta}`}`

  const encabezados: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) {
    encabezados['Content-Type'] = 'application/json'
  }
  const token = conAuth ? obtenerToken() : null
  if (token) {
    encabezados.Authorization = `Bearer ${token}`
  }

  let respuesta: Response
  try {
    respuesta = await fetch(url, {
      method,
      headers: encabezados,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    // fetch solo rechaza por fallo de red (backend caido, sin conexion).
    throw new ErrorApi({ status: 0, codigo: 'ERROR_RED' })
  }

  if (!respuesta.ok) {
    const error = await leerError(respuesta)
    // 401 de sesion: el token falta o es invalido/expirado. Se limpia la
    // sesion mediante el callback registrado por el AuthContext.
    if (respuesta.status === 401 && error.codigo === 'NO_AUTENTICADO' && notificarNoAutenticado) {
      alNoAutenticado()
    }
    throw error
  }

  // Respuestas 204 o sin cuerpo: se devuelve undefined sin intentar parsear.
  if (respuesta.status === 204 || respuesta.headers.get('content-length') === '0') {
    return undefined as T
  }

  const texto = await respuesta.text()
  if (!texto) {
    return undefined as T
  }
  try {
    return JSON.parse(texto) as T
  } catch {
    throw new ErrorApi({ status: respuesta.status, codigo: 'ERROR_INESPERADO' })
  }
}

/** Cliente HTTP con los verbos que usa la aplicacion. */
export const apiClient = {
  get: <T>(ruta: string, opciones?: OpcionesPeticion) =>
    peticion<T>(ruta, { ...opciones, method: 'GET' }),
  post: <T>(ruta: string, body?: unknown, opciones?: OpcionesPeticion) =>
    peticion<T>(ruta, { ...opciones, method: 'POST', body }),
  put: <T>(ruta: string, body?: unknown, opciones?: OpcionesPeticion) =>
    peticion<T>(ruta, { ...opciones, method: 'PUT', body }),
  delete: <T>(ruta: string, opciones?: OpcionesPeticion) =>
    peticion<T>(ruta, { ...opciones, method: 'DELETE' }),
}
