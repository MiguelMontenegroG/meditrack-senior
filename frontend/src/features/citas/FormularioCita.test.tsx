// Pruebas del formulario de citas: exige residente, rechaza fechas pasadas en
// cliente, agenda con datos validos y traduce el 400/409 del backend.

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { FormularioCita } from '@/features/citas/FormularioCita'
import { ErrorApi } from '@/lib/api-client'
import { sumarDias, hoyBogota } from '@/features/citas/fechas'

const agendarCita = vi.fn()
const editarCita = vi.fn()
const listarPacientes = vi.fn()
const listarEspecialidades = vi.fn()

vi.mock('@/features/citas/services/citaService', () => ({
  agendarCita: (...args: unknown[]) => agendarCita(...args),
  editarCita: (...args: unknown[]) => editarCita(...args),
}))
vi.mock('@/features/pacientes/services/pacienteService', () => ({
  listarPacientes: (...args: unknown[]) => listarPacientes(...args),
}))
vi.mock('@/features/citas/services/especialidadService', () => ({
  listarEspecialidades: (...args: unknown[]) => listarEspecialidades(...args),
}))
vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: 'ADMINISTRADOR' } }),
}))

function renderizar() {
  return render(
    <MemoryRouter initialEntries={['/calendario/nuevo']}>
      <Routes>
        <Route path="/calendario/nuevo" element={<FormularioCita />} />
        <Route path="/calendario" element={<p>Calendario</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('FormularioCita', () => {
  beforeEach(() => {
    listarPacientes.mockResolvedValue({
      contenido: [{ id: 7, nombreCompleto: 'Rosa Gutierrez', edad: 82, habitacion: '204', activo: true }],
      pagina: 0,
      tamano: 100,
      total: 1,
      totalPaginas: 1,
    })
    listarEspecialidades.mockResolvedValue([{ id: 2, nombre: 'Geriatria' }])
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('exige seleccionar un residente antes de enviar', async () => {
    const usuario = userEvent.setup()
    renderizar()
    await screen.findByRole('option', { name: /Rosa Gutierrez/ })
    await usuario.click(screen.getByRole('button', { name: /agendar cita/i }))
    expect(await screen.findByText(/selecciona un residente\./i)).toBeInTheDocument()
    expect(agendarCita).not.toHaveBeenCalled()
  })

  it('rechaza una fecha y hora en el pasado', async () => {
    const usuario = userEvent.setup()
    renderizar()
    await screen.findByRole('option', { name: /Rosa Gutierrez/ })
    await usuario.selectOptions(screen.getByLabelText(/residente/i), '7')
    const fecha = screen.getByLabelText(/fecha/i)
    await usuario.clear(fecha)
    await usuario.type(fecha, '2020-01-01')
    await usuario.click(screen.getByRole('button', { name: /agendar cita/i }))
    expect(await screen.findByText(/no puede estar en el pasado/i)).toBeInTheDocument()
    expect(agendarCita).not.toHaveBeenCalled()
  })

  it('agenda con datos validos y vuelve al calendario', async () => {
    const usuario = userEvent.setup()
    agendarCita.mockResolvedValue({ id: 1 })
    renderizar()
    await screen.findByRole('option', { name: /Rosa Gutierrez/ })
    await usuario.selectOptions(screen.getByLabelText(/residente/i), '7')
    await usuario.selectOptions(screen.getByLabelText(/especialidad/i), '2')
    const fecha = screen.getByLabelText(/fecha/i)
    await usuario.clear(fecha)
    await usuario.type(fecha, sumarDias(hoyBogota(), 1))
    await usuario.click(screen.getByRole('button', { name: /agendar cita/i }))
    await waitFor(() => expect(agendarCita).toHaveBeenCalledTimes(1))
    const [pacienteId, datos] = agendarCita.mock.calls[0]
    expect(pacienteId).toBe(7)
    expect(datos.especialidadId).toBe(2)
    expect(datos.fechaHora).toMatch(/Z$/)
    expect(await screen.findByText('Calendario')).toBeInTheDocument()
  })

  it('muestra el mensaje de conflicto (estado final) ante un 409', async () => {
    const usuario = userEvent.setup()
    agendarCita.mockRejectedValue(new ErrorApi({ status: 409, codigo: 'CONFLICTO' }))
    renderizar()
    await screen.findByRole('option', { name: /Rosa Gutierrez/ })
    await usuario.selectOptions(screen.getByLabelText(/residente/i), '7')
    const fecha = screen.getByLabelText(/fecha/i)
    await usuario.clear(fecha)
    await usuario.type(fecha, sumarDias(hoyBogota(), 1))
    await usuario.click(screen.getByRole('button', { name: /agendar cita/i }))
    expect(await screen.findByText(/no esta en estado inicial/i)).toBeInTheDocument()
  })
})