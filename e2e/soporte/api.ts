// Utilidades HTTP para las pruebas end-to-end: llamadas directas a la API con
// el token del administrador. Se usan para preparar datos que no forman parte
// del flujo que se quiere demostrar y para la limpieza final.
//
// Nunca se imprimen tokens ni contrasenas: los mensajes de error solo incluyen
// el metodo, la ruta y el status.

import { expect, request, type APIRequestContext, type APIResponse } from '@playwright/test'
import { ENTORNO } from './entorno'

/** Cuerpo de error del backend (ApiError). */
export interface ErrorApi {
  status: number
  codigo: string
  mensaje: string
  detalles?: string[] | null
}

/** Cliente HTTP autenticado con el token del administrador. */
export interface ClienteApi {
  obtener: <T>(ruta: string) => Promise<T>
  post: <T>(ruta: string, cuerpo?: unknown) => Promise<T>
  patch: <T>(ruta: string, cuerpo: unknown) => Promise<T>
  /** DELETE: solo se usa para deshacer vinculos (nunca borra residentes). */
  eliminar: (ruta: string) => Promise<void>
  /** Ejecuta la peticion y devuelve status + cuerpo sin lanzar por 4xx. */
  crudo: (
    metodo: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE',
    ruta: string,
    cuerpo?: unknown,
  ) => Promise<{ respuesta: APIResponse; cuerpo: unknown }>
  cerrar: () => Promise<void>
}

/**
 * Comprueba que el baseURL incluya el prefijo /api. Playwright usa
 * `new URL(ruta, baseURL)`: con rutas absolutas ('/auth/login') descarta la
 * parte del path de baseURL, asi que la ruta debe construirse completa.
 */
function urlCompleta(base: string, ruta: string): string {
  return `${base.replace(/\/+$/, '')}${ruta.startsWith('/') ? ruta : `/${ruta}`}`
}

/** Crea un cliente HTTP con el token indicado (o sin el si es null). */
export async function crearCliente(token: string | null): Promise<ClienteApi> {
  const contexto: APIRequestContext = await request.newContext({
    baseURL: ENTORNO.apiUrl,
    extraHTTPHeaders: token ? { Authorization: `Bearer ${token}` } : {},
  })

  const crudo: ClienteApi['crudo'] = async (metodo, ruta, cuerpo) => {
    const respuesta = await contexto.fetch(ruta, {
      method: metodo,
      data: cuerpo,
      headers: { Accept: 'application/json' },
    })
    const tipo = respuesta.headers()['content-type'] ?? ''
    const cuerpoLeido = tipo.includes('application/json') ? await respuesta.json() : null
    return { respuesta, cuerpo: cuerpoLeido }
  }

  const exigirOk = async <T>(metodo: string, ruta: string, respuesta: APIResponse): Promise<T> => {
    if (!respuesta.ok()) {
      const detalle = (await respuesta.text()).slice(0, 300)
      throw new Error(`${metodo} ${ruta} respondio ${respuesta.status()}: ${detalle}`)
    }
    const tipo = respuesta.headers()['content-type'] ?? ''
    if (!tipo.includes('application/json')) return null as T
    return (await respuesta.json()) as T
  }

  return {
    obtener: async <T>(ruta: string) => {
      const respuesta = await contexto.get(urlCompleta(ENTORNO.apiUrl, ruta))
      return exigirOk<T>('GET', ruta, respuesta)
    },
    post: async <T>(ruta: string, cuerpo?: unknown) => {
      const respuesta = await contexto.post(urlCompleta(ENTORNO.apiUrl, ruta), { data: cuerpo })
      return exigirOk<T>('POST', ruta, respuesta)
    },
    patch: async <T>(ruta: string, cuerpo: unknown) => {
      const respuesta = await contexto.patch(urlCompleta(ENTORNO.apiUrl, ruta), { data: cuerpo })
      return exigirOk<T>('PATCH', ruta, respuesta)
    },
    eliminar: async (ruta: string) => {
      const respuesta = await contexto.delete(urlCompleta(ENTORNO.apiUrl, ruta))
      await exigirOk<null>('DELETE', ruta, respuesta)
    },
    crudo,
    cerrar: () => contexto.dispose(),
  }
}

/** Inicia sesion por API y devuelve el token Bearer. */
export async function iniciarSesionApi(correo: string, contrasena: string): Promise<string> {
  const contexto = await request.newContext({ baseURL: ENTORNO.apiUrl })
  try {
    const respuesta = await contexto.post(urlCompleta(ENTORNO.apiUrl, '/auth/login'), {
      data: { correo, contrasena },
    })
    if (!respuesta.ok()) {
      throw new Error(`No se pudo iniciar sesion por API (status ${respuesta.status()}).`)
    }
    const cuerpo = (await respuesta.json()) as { token?: string }
    if (!cuerpo.token) {
      throw new Error('La respuesta de login no incluye token.')
    }
    return cuerpo.token
  } finally {
    await contexto.dispose()
  }
}

/** Verifica que una respuesta cruda sea de error con el status y codigo dados. */
export function esperarError(
  respuesta: APIResponse,
  cuerpo: unknown,
  status: number,
  codigo?: string,
): void {
  const error = cuerpo as ErrorApi | null
  expect(
    respuesta.status(),
    `se esperaba status ${status} y llego ${respuesta.status()} (${error?.codigo ?? 'sin codigo'})`,
  ).toBe(status)
  if (codigo) {
    expect(error?.codigo, 'codigo de error del backend').toBe(codigo)
  }
}

/** Credenciales del administrador necesarias para preparar datos por API. */
export interface Credenciales {
  correo: string
  contrasena: string
}

/** Cliente del administrador ya autenticado (centraliza el login por API). */
export async function clienteAdmin(admin: Credenciales): Promise<ClienteApi> {
  const token = await iniciarSesionApi(admin.correo, admin.contrasena)
  return crearCliente(token)
}
