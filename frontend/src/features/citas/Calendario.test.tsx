// Pruebas del calendario de citas: carga y agrupa por dia con el nombre del
// residente, oculta/expone acciones segun el rol, y el estado vacio.

import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { Calendario } from '@/features/citas/Calendario'

const listarCitas = vi.fn()
const listarPacientes = vi.fn()
let rolActual: string = 'ADMINISTRADOR'

vi.mock('@/features/citas/services/citaService', () => ({
  listarCitas: (...args: unknown[]) => listarCitas(...args),
  cambiarEstadoCita: vi.fn(),
}))
vi.mock('@/features/pacientes/services/pacienteService', () => ({
  listarPacientes: (...args: unknown[]) => listarPacientes(...args),
}))
vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: rolActual } }),
}))

const RESIDENTES = {
  contenido: [{ id: 7, nombreCompleto: 'Rosa Gutierrez', edad: 82, habitacion: '204', activo: true }],
  pagina: 0,
  tamano: 100,
  total: 1,
  totalPaginas: 1,
}

const CITA_PASADA = {
  id: 1,
  pacienteId: 7,
  especialidadId: 2,
  especialidadNombre: 'Geriatria',
  registradaPor: 1,
  profesional: 'Dra. Ruiz',
  fechaHora: '2020-01-20T14:00:00.000Z', // ya paso: permite marcar estado
  lugar: 'Consultorio 1',
  estado: 'PROGRAMADA',
  motivo: 'Control anual',
}

function pagina(contenido: unknown[]) {
  return { contenido, pagina: 0, tamano: 20, total: contenido.length, totalPaginas: 1 }
}

function renderizar() {
  return render(
    <MemoryRouter>
      <Calendario />
    </MemoryRouter>,
  )
}

describe('Calendario', () => {
  beforeEach(() => {
    rolActual = 'ADMINISTRADOR'
    listarPacientes.mockResolvedValue(RESIDENTES)
    listarCitas.mockResolvedValue(pagina([CITA_PASADA]))
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('carga las citas y muestra residente, especialidad y accion de agendar', async () => {
    renderizar()
    expect(screen.getByText(/cargando citas/i)).toBeInTheDocument()
    expect((await screen.findAllByText('Rosa Gutierrez')).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/Geriatria/).length).toBeGreaterThan(0)
    expect(screen.getAllByRole('button', { name: /agendar cita/i }).length).toBeGreaterThan(0)
  })

  it('muestra un estado vacio cuando no hay citas', async () => {
    listarCitas.mockResolvedValue(pagina([]))
    renderizar()
    await waitFor(() => expect(screen.getByText(/no hay citas/i)).toBeInTheDocument())
  })

  it('ofrece Editar y Cancelar solo al ADMINISTRADOR', async () => {
    renderizar()
    expect((await screen.findAllByRole('button', { name: /editar/i })).length).toBeGreaterThan(0)
    expect(screen.getAllByRole('button', { name: /cancelar/i }).length).toBeGreaterThan(0)
  })

  it('el CUIDADOR no agenda, no edita ni cancela, pero puede marcar estado', async () => {
    rolActual = 'CUIDADOR_ENFERMERO'
    renderizar()
    expect((await screen.findAllByRole('button', { name: /cumplida/i })).length).toBeGreaterThan(0)
    expect(screen.queryByRole('button', { name: /agendar cita/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /editar/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /cancelar/i })).not.toBeInTheDocument()
  })

  it('el FAMILIAR es solo lectura: no ve acciones de escritura', async () => {
    rolActual = 'FAMILIAR_AUTORIZADO'
    renderizar()
    expect((await screen.findAllByText('Rosa Gutierrez')).length).toBeGreaterThan(0)
    expect(screen.queryByRole('button', { name: /agendar cita/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /cumplida/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /editar/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /cancelar/i })).not.toBeInTheDocument()
  })
})