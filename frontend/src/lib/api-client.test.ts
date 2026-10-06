import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  apiClient,
  ErrorApi,
  registrarManejadorNoAutenticado,
  registrarToken,
} from '@/lib/api-client'

// Pruebas del cliente HTTP con fetch simulado. No dependen del backend real.

/** Construye una Response minima para el mock de fetch. */
function respuesta(cuerpo: unknown, opciones: { status?: number } = {}): Response {
  const status = opciones.status ?? 200
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => cuerpo,
    text: async () => (cuerpo === undefined ? '' : JSON.stringify(cuerpo)),
  } as unknown as Response
}

describe('api-client', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
    registrarToken(() => null)
    registrarManejadorNoAutenticado(() => {})
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('devuelve el cuerpo parseado en una respuesta exitosa', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta({ id: 1, rol: 'ADMINISTRADOR' })))
    const datos = await apiClient.get<{ id: number }>('/auth/me')
    expect(datos.id).toBe(1)
  })

  it('adjunta el encabezado Authorization Bearer cuando hay token', async () => {
    registrarToken(() => 'token-de-prueba')
    const espia = vi.fn().mockResolvedValue(respuesta({ ok: true }))
    vi.stubGlobal('fetch', espia)
    await apiClient.get('/auth/me')
    const encabezados = espia.mock.calls[0][1].headers as Record<string, string>
    expect(encabezados.Authorization).toBe('Bearer token-de-prueba')
  })

  it('lanza ErrorApi 401 CREDENCIALES_INVALIDAS con mensaje en espanol', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        respuesta({ codigo: 'CREDENCIALES_INVALIDAS', status: 401 }, { status: 401 }),
      ),
    )
    await expect(
      apiClient.post('/auth/login', {}, { conAuth: false, notificarNoAutenticado: false }),
    ).rejects.toMatchObject({
      status: 401,
      codigo: 'CREDENCIALES_INVALIDAS',
      mensajeUsuario: 'Correo o contrasena incorrectos.',
    })
  })

  it('lanza ErrorApi 400 con el codigo VALIDACION', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(respuesta({ codigo: 'VALIDACION', status: 400 }, { status: 400 })),
    )
    const error = await apiClient.post('/auth/login', {}).catch((e) => e)
    expect(error).toBeInstanceOf(ErrorApi)
    expect((error as ErrorApi).codigo).toBe('VALIDACION')
  })

  it('lanza ErrorApi ERROR_RED cuando falla la red (backend caido)', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))
    const error = await apiClient.get('/auth/me').catch((e) => e)
    expect(error).toBeInstanceOf(ErrorApi)
    expect((error as ErrorApi).codigo).toBe('ERROR_RED')
    expect((error as ErrorApi).status).toBe(0)
  })

  it('invoca el manejador NO_AUTENTICADO ante un 401 de sesion', async () => {
    const manejador = vi.fn()
    registrarManejadorNoAutenticado(manejador)
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(respuesta({ codigo: 'NO_AUTENTICADO' }, { status: 401 })),
    )
    await expect(apiClient.get('/auth/me')).rejects.toBeInstanceOf(ErrorApi)
    expect(manejador).toHaveBeenCalledOnce()
  })
})