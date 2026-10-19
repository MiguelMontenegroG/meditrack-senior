// Pruebas del panel de administrador: todos los KPI salen de los servicios
// reales (residentes, medicamentos y agenda) y la fecha del saludo es la del
// dia. Se verifica tambien el estado de error.

import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { PanelAdministrador } from '@/features/dashboard/PanelAdministrador'
import * as pacienteService from '@/features/pacientes/services/pacienteService'
import * as medicamentoService from '@/features/medicacion/services/medicamentoService'
import * as tomaService from '@/features/medicacion/services/tomaService'
import * as citaService from '@/features/citas/services/citaService'
import type { AgendaEsquema } from '@/features/medicacion/types'

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, nombreCompleto: 'Laura Rojas', rol: 'ADMINISTRADOR' } }),
}))

const AGENDA: AgendaEsquema[] = [
  {
    esquemaId: 3,
    medicamentoId: 5,
    medicamentoNombre: 'Losartan',
    dosis: '50 mg',
    slots: [
      { fechaHoraProgramada: '2030-01-01T08:00:00-05:00', estado: 'ADMINISTRADA' },
      { fechaHoraProgramada: '2030-01-01T16:00:00-05:00', estado: 'PENDIENTE' },
    ],
  },
]

function renderPanel() {
  return render(
    <MemoryRouter>
      <PanelAdministrador />
    </MemoryRouter>,
  )
}

const PAGINA_CITAS_VACIA = { contenido: [], pagina: 0, tamano: 100, total: 0, totalPaginas: 0 }

describe('PanelAdministrador', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(citaService, 'listarCitas').mockResolvedValue(PAGINA_CITAS_VACIA)
  })

  it('muestra los contadores reales de residentes y medicamentos', async () => {
    vi.spyOn(pacienteService, 'listarPacientes').mockResolvedValue({
      contenido: [
        { id: 1, nombreCompleto: 'Rosa Gutierrez', edad: 82, habitacion: '204', activo: true },
      ],
      pagina: 0,
      tamano: 100,
      total: 7,
      totalPaginas: 1,
    })
    vi.spyOn(medicamentoService, 'listarMedicamentos').mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 1,
      total: 12,
      totalPaginas: 12,
    })
    vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)

    renderPanel()

    await waitFor(() => expect(screen.getByText('Residentes activos')).toBeInTheDocument())
    expect(await screen.findByText('7')).toBeInTheDocument()
    expect(await screen.findByText('12')).toBeInTheDocument()
    // Una toma cerrada de dos programadas.
    expect(await screen.findByText('50%')).toBeInTheDocument()
  })

  it('saluda con el nombre real y la fecha del dia', async () => {
    vi.spyOn(pacienteService, 'listarPacientes').mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 100,
      total: 0,
      totalPaginas: 0,
    })
    vi.spyOn(medicamentoService, 'listarMedicamentos').mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 1,
      total: 0,
      totalPaginas: 0,
    })

    renderPanel()

    expect(await screen.findByText(/Laura/)).toBeInTheDocument()
    const fechaEsperada = new Date().toLocaleDateString('es-CO', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
    expect(screen.getByText(new RegExp(fechaEsperada))).toBeInTheDocument()
  })

  it('muestra el error propio del servicio y permite reintentar', async () => {
    vi.spyOn(pacienteService, 'listarPacientes').mockRejectedValue({
      codigo: 'ERROR_RED',
      mensajeUsuario: 'No pudimos conectar con el servidor. Verifica tu conexion e intentalo de nuevo.',
    })
    vi.spyOn(medicamentoService, 'listarMedicamentos').mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 1,
      total: 0,
      totalPaginas: 0,
    })

    renderPanel()

    expect(
      await screen.findByText(/No pudimos conectar con el servidor/),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /reintentar/i })).toBeInTheDocument()
  })
})
