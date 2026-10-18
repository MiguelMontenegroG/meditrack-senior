// Pruebas del servicio de esquemas de medicacion con el cliente HTTP simulado.

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  actualizarEsquema,
  cambiarEstadoEsquema,
  crearEsquema,
  listarEsquemas,
  obtenerEsquema,
} from '@/features/medicacion/services/esquemaService'

function respuesta(cuerpo: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => cuerpo,
    text: async () => (cuerpo === undefined ? '' : JSON.stringify(cuerpo)),
  } as unknown as Response
}

const DATOS = {
  medicamentoId: 5,
  dosis: '50 mg',
  frecuenciaHoras: 8,
  via: 'ORAL' as const,
  indicaciones: 'Con alimentos',
  fechaInicio: '2025-01-01',
  fechaFin: null,
  horarios: ['08:00', '16:00', '00:00'],
}

describe('esquemaService', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
  })
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('lista los esquemas de un residente sin filtros', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta([]))
    vi.stubGlobal('fetch', espia)
    await listarEsquemas(7)
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/pacientes/7/esquemas')
  })

  it('lista los esquemas aplicando filtros de estado y vigencia', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta([]))
    vi.stubGlobal('fetch', espia)
    await listarEsquemas(7, { activo: true, vigente: false })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe(
      'http://localhost:8080/api/pacientes/7/esquemas?activo=true&vigente=false',
    )
  })

  it('obtiene el detalle de un esquema', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta({ id: 3 })))
    const esquema = await obtenerEsquema(7, 3)
    expect(esquema.id).toBe(3)
  })

  it('crea un esquema con POST y envia sus horarios', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 1 }, 201))
    vi.stubGlobal('fetch', espia)
    await crearEsquema(7, DATOS)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/7/esquemas')
    expect(opciones.method).toBe('POST')
    expect(JSON.parse(opciones.body as string).horarios).toEqual(DATOS.horarios)
  })

  it('actualiza un esquema con PUT', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 3 }))
    vi.stubGlobal('fetch', espia)
    await actualizarEsquema(7, 3, DATOS)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/7/esquemas/3')
    expect(opciones.method).toBe('PUT')
  })

  it('cambia el estado del esquema con PATCH y cuerpo { activo }', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 3, activo: false }))
    vi.stubGlobal('fetch', espia)
    const esquema = await cambiarEstadoEsquema(7, 3, false)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/7/esquemas/3/estado')
    expect(opciones.method).toBe('PATCH')
    expect(JSON.parse(opciones.body as string)).toEqual({ activo: false })
    expect(esquema.activo).toBe(false)
  })
})
