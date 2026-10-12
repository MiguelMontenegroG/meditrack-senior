// Pruebas del servicio de usuarios con el cliente HTTP simulado. No dependen
// del backend real ni de la interfaz.

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  actualizarUsuario,
  cambiarEstadoUsuario,
  crearUsuario,
  listarUsuarios,
  listarUsuariosPorRol,
  obtenerUsuario,
} from '@/features/usuarios/services/usuarioService'

/** Respuesta minima compatible con lo que espera el api-client. */
function respuesta(cuerpo: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => cuerpo,
    text: async () => (cuerpo === undefined ? '' : JSON.stringify(cuerpo)),
  } as unknown as Response
}

describe('usuarioService', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
  })
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('compone el query string del listado omitiendo los filtros vacios', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarUsuarios({ rol: 'CUIDADOR_ENFERMERO', activo: true, pagina: 2, tamano: 10 })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe(
      'http://localhost:8080/api/usuarios?rol=CUIDADOR_ENFERMERO&activo=true&pagina=2&tamano=10',
    )
  })

  it('no envia filtros cuando no se definen', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarUsuarios()
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/usuarios')
  })

  it('obtiene un usuario por id', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta({ id: 4, nombreCompleto: 'Ana' })))
    const usuario = await obtenerUsuario(4)
    expect(usuario.id).toBe(4)
  })

  it('crea un usuario con POST y envia la contrasena', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 1 }, 201))
    vi.stubGlobal('fetch', espia)
    await crearUsuario({
      nombreCompleto: 'Ana Gomez',
      correo: 'ana@ejemplo.com',
      contrasena: 'secreta123',
      rol: 'CUIDADOR_ENFERMERO',
    })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/usuarios')
    expect(opciones.method).toBe('POST')
    expect(JSON.parse(opciones.body as string).contrasena).toBe('secreta123')
  })

  it('actualiza un usuario con PUT (nombre, telefono y rol)', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 2 }))
    vi.stubGlobal('fetch', espia)
    await actualizarUsuario(2, { nombreCompleto: 'Ana', telefono: null, rol: 'ADMINISTRADOR' })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/usuarios/2')
    expect(opciones.method).toBe('PUT')
  })

  it('cambia el estado con PATCH y cuerpo { activo }', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 3, activo: false }))
    vi.stubGlobal('fetch', espia)
    const usuario = await cambiarEstadoUsuario(3, false)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/usuarios/3/estado')
    expect(opciones.method).toBe('PATCH')
    expect(JSON.parse(opciones.body as string)).toEqual({ activo: false })
    expect(usuario.activo).toBe(false)
  })

  it('listarUsuariosPorRol filtra activos y pide hasta 100 y devuelve el contenido', async () => {
    const espia = vi
      .fn()
      .mockResolvedValue(respuesta({ contenido: [{ id: 9, nombreCompleto: 'Luis' }] }))
    vi.stubGlobal('fetch', espia)
    const lista = await listarUsuariosPorRol('FAMILIAR_AUTORIZADO')
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe(
      'http://localhost:8080/api/usuarios?rol=FAMILIAR_AUTORIZADO&activo=true&tamano=100',
    )
    expect(lista).toHaveLength(1)
  })
})
