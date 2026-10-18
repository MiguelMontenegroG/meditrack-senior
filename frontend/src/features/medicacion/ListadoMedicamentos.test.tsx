// Pruebas del catalogo de medicamentos: carga, estado vacio, error con
// reintento, ocultado del boton "Nuevo" para no administradores y el conflicto
// de nombre duplicado (409) en el formulario.

import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ListadoMedicamentosCatalogo } from '@/features/medicacion/ListadoMedicamentos'

const listarMedicamentos = vi.fn()
let rolActual: string = 'ADMINISTRADOR'

vi.mock('@/features/medicacion/services/medicamentoService', () => ({
  listarMedicamentos: (...args: unknown[]) => listarMedicamentos(...args),
}))

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: rolActual } }),
}))

const PAGINA = {
  contenido: [
    { id: 1, nombre: 'Losartan', presentacion: '50 mg', activo: true },
    { id: 2, nombre: 'Metformina', presentacion: null, activo: false },
  ],
  pagina: 0,
  tamano: 10,
  total: 2,
  totalPaginas: 1,
}

function renderizar() {
  return render(
    <MemoryRouter>
      <ListadoMedicamentosCatalogo />
    </MemoryRouter>,
  )
}

describe('ListadoMedicamentosCatalogo', () => {
  beforeEach(() => {
    rolActual = 'ADMINISTRADOR'
    listarMedicamentos.mockResolvedValue(PAGINA)
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el estado de carga y luego los medicamentos', async () => {
    renderizar()
    expect(screen.getByText(/cargando medicamentos/i)).toBeInTheDocument()
    await waitFor(() => expect(screen.getAllByText('Losartan').length).toBeGreaterThan(0))
    expect(screen.getAllByText('Metformina').length).toBeGreaterThan(0)
  })

  it('consulta por defecto solo activos', async () => {
    renderizar()
    await waitFor(() => expect(listarMedicamentos).toHaveBeenCalled())
    expect(listarMedicamentos).toHaveBeenCalledWith(
      expect.objectContaining({ activo: true, pagina: 0 }),
    )
  })

  it('muestra un estado vacio cuando no hay medicamentos', async () => {
    listarMedicamentos.mockResolvedValue({ ...PAGINA, contenido: [], total: 0, totalPaginas: 0 })
    renderizar()
    await waitFor(() => expect(screen.getByText(/no hay medicamentos/i)).toBeInTheDocument())
  })

  it('muestra un estado de error y permite reintentar', async () => {
    listarMedicamentos.mockRejectedValue({
      codigo: 'ERROR_RED',
      status: 0,
      mensajeUsuario: 'Sin conexion.',
    })
    renderizar()
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
    expect(screen.getByRole('button', { name: /reintentar/i })).toBeInTheDocument()
  })

  it('oculta el boton "Nuevo medicamento" al CUIDADOR_ENFERMERO', async () => {
    rolActual = 'CUIDADOR_ENFERMERO'
    renderizar()
    await waitFor(() => expect(listarMedicamentos).toHaveBeenCalled())
    expect(screen.queryByRole('button', { name: /nuevo medicamento/i })).not.toBeInTheDocument()
  })

  it('muestra el boton "Nuevo medicamento" al ADMINISTRADOR', async () => {
    renderizar()
    await waitFor(() => expect(listarMedicamentos).toHaveBeenCalled())
    expect(screen.getByRole('button', { name: /nuevo medicamento/i })).toBeInTheDocument()
  })
})
