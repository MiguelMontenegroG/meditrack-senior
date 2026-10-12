// Pruebas del listado de usuarios: estados de carga, error, vacio, datos,
// filtros y el marcador "Tu". Se mockean el servicio y el contexto de auth.

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ListadoUsuarios } from '@/features/usuarios/ListadoUsuarios'

const listarUsuarios = vi.fn()

vi.mock('@/features/usuarios/services/usuarioService', () => ({
  listarUsuarios: (...args: unknown[]) => listarUsuarios(...args),
}))

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: 'ADMINISTRADOR' } }),
}))

/** Renderiza el listado dentro de un router en memoria. */
function renderizar() {
  return render(
    <MemoryRouter>
      <ListadoUsuarios />
    </MemoryRouter>,
  )
}

const PAGINA = {
  contenido: [
    {
      id: 1,
      nombreCompleto: 'Ana Gomez',
      correo: 'ana@ejemplo.com',
      telefono: null,
      rol: 'ADMINISTRADOR',
      activo: true,
      ultimoAccesoEn: null,
      creadoEn: null,
      actualizadoEn: null,
    },
    {
      id: 2,
      nombreCompleto: 'Luis Perez',
      correo: 'luis@ejemplo.com',
      telefono: '3000000000',
      rol: 'CUIDADOR_ENFERMERO',
      activo: true,
      ultimoAccesoEn: null,
      creadoEn: null,
      actualizadoEn: null,
    },
  ],
  pagina: 0,
  tamano: 10,
  total: 2,
  totalPaginas: 1,
}

describe('ListadoUsuarios', () => {
  beforeEach(() => {
    listarUsuarios.mockResolvedValue(PAGINA)
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('muestra el estado de carga y luego los usuarios', async () => {
    renderizar()
    expect(screen.getByText(/cargando usuarios/i)).toBeInTheDocument()
    await waitFor(() => expect(screen.getAllByText('Ana Gomez').length).toBeGreaterThan(0))
    expect(screen.getAllByText('Luis Perez').length).toBeGreaterThan(0)
  })

  it('marca con "Tu" al usuario autenticado', async () => {
    renderizar()
    await waitFor(() => expect(screen.getAllByText('Ana Gomez').length).toBeGreaterThan(0))
    expect(screen.getAllByText('Tú').length).toBeGreaterThan(0)
  })

  it('muestra un estado vacio cuando no hay usuarios', async () => {
    listarUsuarios.mockResolvedValue({ ...PAGINA, contenido: [], total: 0, totalPaginas: 0 })
    renderizar()
    await waitFor(() => expect(screen.getByText(/no hay usuarios/i)).toBeInTheDocument())
  })

  it('muestra un estado de error y permite reintentar', async () => {
    listarUsuarios.mockRejectedValue({
      codigo: 'ERROR_RED',
      status: 0,
      mensajeUsuario: 'Sin conexion.',
    })
    renderizar()
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
    expect(screen.getByRole('button', { name: /reintentar/i })).toBeInTheDocument()
  })

  it('consulta por defecto solo activos', async () => {
    renderizar()
    await waitFor(() => expect(listarUsuarios).toHaveBeenCalled())
    expect(listarUsuarios).toHaveBeenCalledWith(
      expect.objectContaining({ activo: true, pagina: 0 }),
    )
  })

  it('al elegir el filtro "Todos" deja de enviar activo', async () => {
    renderizar()
    await waitFor(() => expect(listarUsuarios).toHaveBeenCalled())
    await userEvent.click(screen.getByRole('button', { name: 'Todos' }))
    await waitFor(() =>
      expect(listarUsuarios).toHaveBeenLastCalledWith(
        expect.objectContaining({ activo: undefined }),
      ),
    )
  })
})
