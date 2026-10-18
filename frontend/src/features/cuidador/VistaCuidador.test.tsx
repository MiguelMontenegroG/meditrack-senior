// Pruebas de la vista del cuidador: tomas reales de la agenda, residentes a
// cargo con su conteo de tomas abiertas y ausencia de cifras de bitacora de
// ejemplo (solo el aviso "proximamente").

import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { VistaCuidador } from '@/features/cuidador/VistaCuidador'
import * as pacienteService from '@/features/pacientes/services/pacienteService'
import * as tomaService from '@/features/medicacion/services/tomaService'
import type { AgendaEsquema } from '@/features/medicacion/types'

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 9, nombreCompleto: 'Carlos Mejia', rol: 'CUIDADOR_ENFERMERO' } }),
}))

const AGENDA: AgendaEsquema[] = [
  {
    esquemaId: 3,
    medicamentoId: 5,
    medicamentoNombre: 'Losartan',
    dosis: '50 mg',
    slots: [
      { fechaHoraProgramada: '2030-01-01T08:00:00-05:00', estado: 'PENDIENTE' },
      { fechaHoraProgramada: '2030-01-01T00:00:00-05:00', estado: 'ATRASADA' },
      { fechaHoraProgramada: '2030-01-01T12:00:00-05:00', estado: 'ADMINISTRADA' },
    ],
  },
]

function renderVista() {
  return render(
    <MemoryRouter>
      <VistaCuidador />
    </MemoryRouter>,
  )
}

describe('VistaCuidador', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(pacienteService, 'listarPacientes').mockResolvedValue({
      contenido: [
        { id: 1, nombreCompleto: 'Rosa Gutierrez', edad: 82, habitacion: '204', activo: true },
      ],
      pagina: 0,
      tamano: 100,
      total: 1,
      totalPaginas: 1,
    })
    vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)
  })

  it('lista las tomas abiertas reales del residente', async () => {
    renderVista()
    expect((await screen.findAllByText('Losartan')).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/Rosa Gutierrez/).length).toBeGreaterThan(0)
  })

  it('muestra los residentes a cargo con su conteo de tomas abiertas', async () => {
    renderVista()
    expect(await screen.findByText('Residentes a tu cargo')).toBeInTheDocument()
    expect(await screen.findByText(/2 tomas abiertas/)).toBeInTheDocument()
    expect(await screen.findByText(/1 atrasadas/)).toBeInTheDocument()
  })

  it('no muestra signos vitales ni bitacoras de ejemplo', async () => {
    renderVista()
    await screen.findByText('Residentes a tu cargo')
    expect(screen.queryByText('128/78')).not.toBeInTheDocument()
    expect(screen.queryByText('102')).not.toBeInTheDocument()
    expect(screen.getByText(/Proximamente/)).toBeInTheDocument()
  })
})
