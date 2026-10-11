// Pruebas del servicio de residentes con el cliente HTTP simulado. No
// dependen del backend real ni de la interfaz.

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  actualizarPaciente,
  cambiarEstadoPaciente,
  crearPaciente,
  listarPacientes,
  obtenerPaciente,
} from '@/features/pacientes/services/pacienteService'

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

describe('pacienteService', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
  })
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('compone el query string omitiendo los filtros vacios', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarPacientes({ texto: 'ana', activo: true, pagina: 1, tamano: 10 })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/pacientes?texto=ana&activo=true&pagina=1&tamano=10')
  })

  it('no envia texto si esta vacio ni activo si no se define', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarPacientes({ texto: '   ' })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/pacientes')
  })

  it('obtiene el detalle de un residente por id', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta({ id: 7, nombreCompleto: 'Gomez, Ana' })))
    const detalle = await obtenerPaciente(7)
    expect(detalle.id).toBe(7)
  })

  it('crea un residente con POST', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 1 }, 201))
    vi.stubGlobal('fetch', espia)
    await crearPaciente({
      documento: '1',
      nombres: 'Ana',
      apellidos: 'Gomez',
      fechaNacimiento: '1940-01-01',
      sexo: 'FEMENINO',
    })
    expect(espia.mock.calls[0][1].method).toBe('POST')
  })

  it('actualiza un residente con PUT', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 2 }))
    vi.stubGlobal('fetch', espia)
    await actualizarPaciente(2, {
      documento: '1',
      nombres: 'Ana',
      apellidos: 'Gomez',
      fechaNacimiento: '1940-01-01',
      sexo: 'FEMENINO',
    })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/2')
    expect(opciones.method).toBe('PUT')
  })

  it('cambia el estado con PATCH y cuerpo { activo }', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 3, activo: false }))
    vi.stubGlobal('fetch', espia)
    const detalle = await cambiarEstadoPaciente(3, false)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/3/estado')
    expect(opciones.method).toBe('PATCH')
    expect(JSON.parse(opciones.body as string)).toEqual({ activo: false })
    expect(detalle.activo).toBe(false)
  })
})
