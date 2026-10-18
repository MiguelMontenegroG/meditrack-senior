// Pruebas de la pantalla de tomas: agenda con estados calculados, registro de
// una toma (con motivo obligatorio al omitir) y visibilidad del boton registrar
// segun el rol.

import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { TomasResidente } from '@/features/medicacion/TomasResidente'
import * as tomaService from '@/features/medicacion/services/tomaService'
import type { AgendaEsquema, PaginaTomas } from '@/features/medicacion/types'

let rolActual: string = 'CUIDADOR_ENFERMERO'

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: rolActual } }),
}))

const AGENDA: AgendaEsquema[] = [
  {
    esquemaId: 3,
    medicamentoId: 5,
    medicamentoNombre: 'Losartan',
    dosis: '50 mg',
    slots: [
      { fechaHoraProgramada: '2025-01-01T08:00:00-05:00', estado: 'ADMINISTRADA' },
      { fechaHoraProgramada: '2025-01-01T16:00:00-05:00', estado: 'PENDIENTE' },
      { fechaHoraProgramada: '2025-01-01T00:00:00-05:00', estado: 'ATRASADA' },
    ],
  },
]

const HISTORIAL: PaginaTomas = {
  contenido: [
    {
      id: 1,
      esquemaId: 3,
      registradoPor: 1,
      fechaHoraProgramada: '2025-01-01T08:00:00-05:00',
      fechaHoraRegistro: '2025-01-01T08:05:00-05:00',
      estado: 'ADMINISTRADA',
      observaciones: null,
    },
  ],
  pagina: 0,
  tamano: 10,
  total: 1,
  totalPaginas: 1,
}

function renderizar() {
  return render(
    <MemoryRouter initialEntries={['/residentes/7/tomas']}>
      <Routes>
        <Route path="/residentes/:id/tomas" element={<TomasResidente />} />
        <Route path="/residentes/:id" element={<p>Detalle</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('TomasResidente', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('muestra la agenda con el estado calculado de cada slot', async () => {
    vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)
    vi.spyOn(tomaService, 'listarHistorialTomas').mockResolvedValue(HISTORIAL)
    renderizar()
    expect(await screen.findByText('Administrada')).toBeInTheDocument()
    expect(screen.getByText('Pendiente')).toBeInTheDocument()
    expect(screen.getByText('Atrasada')).toBeInTheDocument()
  })

  it('exige un motivo al registrar una toma como OMITIDA', async () => {
    vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)
    vi.spyOn(tomaService, 'listarHistorialTomas').mockResolvedValue(HISTORIAL)
    const espia = vi.spyOn(tomaService, 'registrarToma')
    const usuario = userEvent.setup()
    renderizar()
    // El primer slot registrable es el PENDIENTE (16:00).
    const pendiente = (await screen.findAllByRole('button', { name: /^registrar$/i }))[0]
    await usuario.click(pendiente)
    await usuario.click(screen.getByRole('button', { name: /omitida/i }))
    await usuario.click(screen.getByRole('button', { name: /registrar toma/i }))
    expect(await screen.findByText(/indica el motivo/i)).toBeInTheDocument()
    expect(espia).not.toHaveBeenCalled()
  })

  it('registra una toma ADMINISTRADA y recarga la agenda', async () => {
    const agendaEspia = vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)
    vi.spyOn(tomaService, 'listarHistorialTomas').mockResolvedValue(HISTORIAL)
    const espia = vi
      .spyOn(tomaService, 'registrarToma')
      .mockResolvedValue({ id: 9 } as never)
    const usuario = userEvent.setup()
    renderizar()
    const pendiente = (await screen.findAllByRole('button', { name: /^registrar$/i }))[0]
    await usuario.click(pendiente)
    await usuario.click(screen.getByRole('button', { name: /registrar toma/i }))
    await waitFor(() => expect(espia).toHaveBeenCalledTimes(1))
    expect(espia).toHaveBeenCalledWith(
      7,
      3,
      expect.objectContaining({ estado: 'ADMINISTRADA' }),
    )
    // La agenda se recarga (una carga inicial + una tras registrar).
    await waitFor(() => expect(agendaEspia.mock.calls.length).toBeGreaterThanOrEqual(2))
  })

  it('no muestra el boton registrar al FAMILIAR_AUTORIZADO', async () => {
    rolActual = 'FAMILIAR_AUTORIZADO'
    vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)
    vi.spyOn(tomaService, 'listarHistorialTomas').mockResolvedValue(HISTORIAL)
    renderizar()
    expect(await screen.findByText('Pendiente')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^registrar$/i })).not.toBeInTheDocument()
  })

  it('muestra el historial al cambiar de pestana', async () => {
    rolActual = 'CUIDADOR_ENFERMERO'
    vi.spyOn(tomaService, 'obtenerAgenda').mockResolvedValue(AGENDA)
    vi.spyOn(tomaService, 'listarHistorialTomas').mockResolvedValue(HISTORIAL)
    const usuario = userEvent.setup()
    renderizar()
    await screen.findByText('Pendiente')
    await usuario.click(screen.getByRole('tab', { name: /historial/i }))
    const tabla = await screen.findByRole('table', { name: /historial de tomas/i })
    expect(within(tabla).getByText(/losartan/i)).toBeInTheDocument()
  })
})
