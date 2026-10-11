// Pruebas del detalle de residente: estado 404 (no encontrado), datos
// cargados y visibilidad de secciones segun el rol. Todo con mocks.

import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DetalleResidente } from '@/features/pacientes/DetalleResidente'
import { ErrorApi } from '@/lib/api-client'

const obtenerPaciente = vi.fn()
const listarFamiliares = vi.fn()
const listarCuidadores = vi.fn()
let rolActual = 'ADMINISTRADOR'

vi.mock('@/features/pacientes/services/pacienteService', () => ({
  obtenerPaciente: (...args: unknown[]) => obtenerPaciente(...args),
}))
vi.mock('@/features/pacientes/services/familiarService', () => ({
  listarFamiliares: (...args: unknown[]) => listarFamiliares(...args),
}))
vi.mock('@/features/pacientes/services/cuidadorService', () => ({
  listarCuidadores: (...args: unknown[]) => listarCuidadores(...args),
}))
vi.mock('@/features/pacientes/services/usuarioService', () => ({
  listarUsuariosPorRol: vi.fn().mockResolvedValue([]),
}))
vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: rolActual } }),
}))

/** Renderiza el detalle con la ruta /residentes/:id. */
function renderizar(id = '7') {
  return render(
    <MemoryRouter initialEntries={[`/residentes/${id}`]}>
      <Routes>
        <Route path="/residentes/:id" element={<DetalleResidente />} />
      </Routes>
    </MemoryRouter>,
  )
}

const DETALLE = {
  id: 7,
  documento: '123',
  nombres: 'Ana',
  apellidos: 'Gomez',
  nombreCompleto: 'Gomez, Ana',
  fechaNacimiento: '1940-01-01',
  edad: 85,
  sexo: 'FEMENINO',
  habitacion: 'A1',
  situacionClinica: null,
  fechaIngreso: null,
  activo: true,
  contactos: [],
}

describe('DetalleResidente', () => {
  beforeEach(() => {
    rolActual = 'ADMINISTRADOR'
    listarFamiliares.mockResolvedValue([])
    listarCuidadores.mockResolvedValue([])
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('muestra "Residente no encontrado" ante un 404', async () => {
    obtenerPaciente.mockRejectedValue(new ErrorApi({ status: 404, codigo: 'ERROR_INESPERADO' }))
    renderizar()
    await waitFor(() => expect(screen.getByText(/residente no encontrado/i)).toBeInTheDocument())
  })

  it('muestra los datos del residente y las secciones de gestion para ADMIN', async () => {
    obtenerPaciente.mockResolvedValue(DETALLE)
    renderizar()
    await waitFor(() => expect(screen.getByText('Gomez, Ana')).toBeInTheDocument())
    expect(screen.getAllByText(/contactos de emergencia/i).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/familiares autorizados/i).length).toBeGreaterThan(0)
    expect(screen.getAllByText(/cuidadores asignados/i).length).toBeGreaterThan(0)
  })

  it('oculta familiares y cuidadores cuando el rol de solo lectura', async () => {
    rolActual = 'FAMILIAR_AUTORIZADO'
    obtenerPaciente.mockResolvedValue(DETALLE)
    renderizar()
    await waitFor(() => expect(screen.getByText('Gomez, Ana')).toBeInTheDocument())
    expect(screen.queryAllByText(/familiares autorizados/i)).toHaveLength(0)
    expect(screen.queryAllByText(/cuidadores asignados/i)).toHaveLength(0)
  })
})
