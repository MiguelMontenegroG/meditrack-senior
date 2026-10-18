// Pruebas del servicio de medicamentos con el cliente HTTP simulado. No
// dependen del backend real ni de la interfaz.

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  actualizarMedicamento,
  cambiarEstadoMedicamento,
  crearMedicamento,
  listarMedicamentos,
  listarMedicamentosActivos,
} from '@/features/medicacion/services/medicamentoService'

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

describe('medicamentoService', () => {
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
    await listarMedicamentos({ texto: 'losartan', activo: true, pagina: 1, tamano: 10 })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe(
      'http://localhost:8080/api/medicamentos?texto=losartan&activo=true&pagina=1&tamano=10',
    )
  })

  it('no envia filtros cuando no se definen', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarMedicamentos()
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/medicamentos')
  })

  it('crea un medicamento con POST y envia nombre y presentacion', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 1 }, 201))
    vi.stubGlobal('fetch', espia)
    await crearMedicamento({ nombre: 'Losartan', presentacion: '50 mg' })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/medicamentos')
    expect(opciones.method).toBe('POST')
    const cuerpo = JSON.parse(opciones.body as string)
    expect(cuerpo).toEqual({ nombre: 'Losartan', presentacion: '50 mg' })
  })

  it('actualiza un medicamento con PUT', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 2 }))
    vi.stubGlobal('fetch', espia)
    await actualizarMedicamento(2, { nombre: 'Metformina', presentacion: null })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/medicamentos/2')
    expect(opciones.method).toBe('PUT')
  })

  it('cambia el estado con PATCH y cuerpo { activo }', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 3, activo: false }))
    vi.stubGlobal('fetch', espia)
    const medicamento = await cambiarEstadoMedicamento(3, false)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/medicamentos/3/estado')
    expect(opciones.method).toBe('PATCH')
    expect(JSON.parse(opciones.body as string)).toEqual({ activo: false })
    expect(medicamento.activo).toBe(false)
  })

  it('listarMedicamentosActivos pide activos y hasta 100 y devuelve el contenido', async () => {
    const espia = vi
      .fn()
      .mockResolvedValue(respuesta({ contenido: [{ id: 9, nombre: 'Aspirina' }] }))
    vi.stubGlobal('fetch', espia)
    const lista = await listarMedicamentosActivos()
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/medicamentos?activo=true&tamano=100')
    expect(lista).toHaveLength(1)
  })
})
