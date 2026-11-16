// Pruebas del servicio de alertas: construccion de query string (omite filtros
// vacios) y verbo/ruta de la atencion. Se mockea el cliente HTTP para no tocar
// la red.

import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiClient } from '@/lib/api-client'
import { atenderAlerta, listarAlertas } from '@/features/alertas/services/alertaService'

describe('alertaService', () => {
  afterEach(() => vi.restoreAllMocks())

  it('pide el listado sin query cuando no hay filtros', async () => {
    const get = vi.spyOn(apiClient, 'get').mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 10,
      total: 0,
      totalPaginas: 0,
    })
    await listarAlertas()
    expect(get).toHaveBeenCalledWith('/alertas')
  })

  it('incluye estado, tipo, paciente y paginacion en el query', async () => {
    const get = vi.spyOn(apiClient, 'get').mockResolvedValue({
      contenido: [],
      pagina: 1,
      tamano: 10,
      total: 0,
      totalPaginas: 0,
    })
    await listarAlertas({ estado: 'ACTIVA', tipo: 'DOSIS_ATRASADA', pacienteId: 7, pagina: 1, tamano: 10 })
    const ruta = get.mock.calls[0][0] as string
    expect(ruta).toContain('estado=ACTIVA')
    expect(ruta).toContain('tipo=DOSIS_ATRASADA')
    expect(ruta).toContain('pacienteId=7')
    expect(ruta).toContain('pagina=1')
    expect(ruta).toContain('tamano=10')
  })

  it('atiende con PATCH a /alertas/{id}/atender', async () => {
    const patch = vi.spyOn(apiClient, 'patch').mockResolvedValue({})
    await atenderAlerta(42)
    expect(patch).toHaveBeenCalledWith('/alertas/42/atender')
  })
})
