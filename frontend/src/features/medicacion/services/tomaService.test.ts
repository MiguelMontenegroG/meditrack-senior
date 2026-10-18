// Pruebas del servicio de tomas con el cliente HTTP simulado: registro, agenda
// del dia (con y sin fecha) e historial paginado con filtros.

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  listarHistorialTomas,
  obtenerAgenda,
  registrarToma,
} from '@/features/medicacion/services/tomaService'

function respuesta(cuerpo: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => cuerpo,
    text: async () => (cuerpo === undefined ? '' : JSON.stringify(cuerpo)),
  } as unknown as Response
}

describe('tomaService', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
  })
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('registra una toma con POST al esquema indicado', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 1 }, 201))
    vi.stubGlobal('fetch', espia)
    await registrarToma(7, 3, {
      fechaHoraProgramada: '2025-01-01T08:00:00-05:00',
      estado: 'ADMINISTRADA',
      observaciones: null,
    })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/7/esquemas/3/tomas')
    expect(opciones.method).toBe('POST')
    expect(JSON.parse(opciones.body as string).estado).toBe('ADMINISTRADA')
  })

  it('obtiene la agenda sin fecha (por defecto hoy en el backend)', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta([]))
    vi.stubGlobal('fetch', espia)
    await obtenerAgenda(7)
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/pacientes/7/tomas/agenda')
  })

  it('obtiene la agenda con fecha en el query string', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta([]))
    vi.stubGlobal('fetch', espia)
    await obtenerAgenda(7, '2025-03-15')
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/pacientes/7/tomas/agenda?fecha=2025-03-15')
  })

  it('lista el historial con todos los filtros en orden', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarHistorialTomas(7, {
      esquemaId: 3,
      desde: '2025-01-01T00:00:00-05:00',
      hasta: '2025-02-01T00:00:00-05:00',
      pagina: 2,
      tamano: 10,
    })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe(
      'http://localhost:8080/api/pacientes/7/tomas/historial' +
        '?esquemaId=3' +
        '&desde=2025-01-01T00%3A00%3A00-05%3A00' +
        '&hasta=2025-02-01T00%3A00%3A00-05%3A00' +
        '&pagina=2&tamano=10',
    )
  })

  it('lista el historial sin filtros (solo query vacia)', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarHistorialTomas(7)
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe('http://localhost:8080/api/pacientes/7/tomas/historial')
  })
})
