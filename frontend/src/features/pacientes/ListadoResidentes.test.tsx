// Pruebas del listado de residentes: estados de carga, error, vacio y datos.
// Se mockean el servicio y el contexto de autenticacion; no hay backend.

import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ListadoResidentes } from '@/features/pacientes/ListadoResidentes'

const listarPacientes = vi.fn()
const puedeEscribir = vi.fn()

vi.mock('@/features/pacientes/services/pacienteService', () => ({
  listarPacientes: (...args: unknown[]) => listarPacientes(...args),
}))

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: 'ADMINISTRADOR' } }),
}))

vi.mock('@/features/pacientes/permisos', () => ({
  puedeEscribir: (...args: unknown[]) => puedeEscribir(...args),
}))

/** Renderiza el listado dentro de un router en memoria. */
function renderizar() {
  return render(
    <MemoryRouter>
      <ListadoResidentes />
    </MemoryRouter>,
  )
}

describe('ListadoResidentes', () => {
  beforeEach(() => {
    puedeEscribir.mockReturnValue(true)
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el estado de carga y luego los residentes', async () => {
    listarPacientes.mockResolvedValue({
      contenido: [{ id: 1, nombreCompleto: 'Gomez, Ana', edad: 84, habitacion: 'A1', activo: true }],
      pagina: 0,
      tamano: 10,
      total: 1,
      totalPaginas: 1,
    })
    renderizar()
    expect(screen.getByText(/cargando residentes/i)).toBeInTheDocument()
    await waitFor(() => expect(screen.getAllByText('Gomez, Ana').length).toBeGreaterThan(0))
  })

  it('muestra un estado vacio cuando no hay residentes', async () => {
    listarPacientes.mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 10,
      total: 0,
      totalPaginas: 0,
    })
    renderizar()
    await waitFor(() => expect(screen.getByText(/no hay residentes/i)).toBeInTheDocument())
  })

  it('muestra un estado de error y permite reintentar', async () => {
    listarPacientes.mockRejectedValue({ codigo: 'ERROR_RED', status: 0, mensajeUsuario: 'Sin conexion.' })
    renderizar()
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
    expect(screen.getByRole('button', { name: /reintentar/i })).toBeInTheDocument()
  })

  it('oculta el boton "Nuevo residente" cuando el rol no puede escribir', async () => {
    puedeEscribir.mockReturnValue(false)
    listarPacientes.mockResolvedValue({
      contenido: [],
      pagina: 0,
      tamano: 10,
      total: 0,
      totalPaginas: 0,
    })
    renderizar()
    await waitFor(() => expect(screen.getByText(/no hay residentes/i)).toBeInTheDocument())
    expect(screen.queryByRole('button', { name: /nuevo residente/i })).not.toBeInTheDocument()
  })
})
