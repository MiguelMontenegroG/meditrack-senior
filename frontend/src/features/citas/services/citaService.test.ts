// Pruebas del servicio de citas con el cliente HTTP simulado. Verifican el
// query string del calendario y los cuerpos de POST/PUT/PATCH, sin backend real.

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  agendarCita,
  cambiarEstadoCita,
  editarCita,
  listarCitas,
  obtenerCita,
} from '@/features/citas/services/citaService'

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

describe('citaService', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
  })
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('incluye desde y hasta en el query string y omite los filtros vacios', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarCitas({
      desde: '2026-01-01T05:00:00.000Z',
      hasta: '2026-02-01T05:00:00.000Z',
    })
    const url = espia.mock.calls[0][0] as string
    expect(url).toBe(
      'http://localhost:8080/api/citas?desde=2026-01-01T05%3A00%3A00.000Z&hasta=2026-02-01T05%3A00%3A00.000Z',
    )
  })

  it('agrega estado, pacienteId y paginacion al query string', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ contenido: [] }))
    vi.stubGlobal('fetch', espia)
    await listarCitas({
      desde: '2026-01-01T05:00:00.000Z',
      hasta: '2026-02-01T05:00:00.000Z',
      estado: 'PROGRAMADA',
      pacienteId: 7,
      pagina: 2,
      tamano: 20,
    })
    const url = espia.mock.calls[0][0] as string
    expect(url).toContain('estado=PROGRAMADA')
    expect(url).toContain('pacienteId=7')
    expect(url).toContain('pagina=2')
    expect(url).toContain('tamano=20')
  })

  it('obtiene el detalle de una cita por paciente y cita', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 9 }))
    vi.stubGlobal('fetch', espia)
    await obtenerCita(3, 9)
    expect(espia.mock.calls[0][0]).toBe('http://localhost:8080/api/pacientes/3/citas/9')
  })

  it('agenda con POST y envia el cuerpo de la cita', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 1 }, 201))
    vi.stubGlobal('fetch', espia)
    const datos = {
      especialidadId: 2,
      profesional: 'Dra. Ruiz',
      fechaHora: '2026-01-20T14:00:00.000Z',
      lugar: 'Consultorio 1',
      motivo: 'Control',
    }
    await agendarCita(5, datos)
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/5/citas')
    expect(opciones.method).toBe('POST')
    expect(JSON.parse(opciones.body as string)).toEqual(datos)
  })

  it('edita con PUT', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 9 }))
    vi.stubGlobal('fetch', espia)
    await editarCita(3, 9, {
      especialidadId: null,
      profesional: null,
      fechaHora: '2026-01-20T14:00:00.000Z',
      lugar: null,
      motivo: null,
    })
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/3/citas/9')
    expect(opciones.method).toBe('PUT')
  })

  it('cambia el estado con PATCH y cuerpo { estado }', async () => {
    const espia = vi.fn().mockResolvedValue(respuesta({ id: 9, estado: 'CUMPLIDA' }))
    vi.stubGlobal('fetch', espia)
    await cambiarEstadoCita(3, 9, 'CUMPLIDA')
    const [url, opciones] = espia.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/pacientes/3/citas/9/estado')
    expect(opciones.method).toBe('PATCH')
    expect(JSON.parse(opciones.body as string)).toEqual({ estado: 'CUMPLIDA' })
  })
})