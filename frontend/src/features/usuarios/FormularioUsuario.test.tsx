// Pruebas del formulario de usuarios: alta, validacion en cliente, correo
// duplicado (409) y edicion con carga de datos. Todo con mocks.

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { FormularioUsuario } from '@/features/usuarios/FormularioUsuario'
import { ErrorApi } from '@/lib/api-client'

const crearUsuario = vi.fn()
const actualizarUsuario = vi.fn()
const obtenerUsuario = vi.fn()

vi.mock('@/features/usuarios/services/usuarioService', () => ({
  crearUsuario: (...args: unknown[]) => crearUsuario(...args),
  actualizarUsuario: (...args: unknown[]) => actualizarUsuario(...args),
  obtenerUsuario: (...args: unknown[]) => obtenerUsuario(...args),
}))

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: 'ADMINISTRADOR' } }),
}))

/** Renderiza el formulario en la ruta indicada. */
function renderizar(ruta = '/usuarios/nuevo') {
  return render(
    <MemoryRouter initialEntries={[ruta]}>
      <Routes>
        <Route path="/usuarios/nuevo" element={<FormularioUsuario />} />
        <Route path="/usuarios/:id/editar" element={<FormularioUsuario />} />
        <Route path="/usuarios" element={<div>Listado de usuarios</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('FormularioUsuario', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('valida los campos obligatorios antes de enviar', async () => {
    renderizar()
    await userEvent.click(screen.getByRole('button', { name: /guardar usuario/i }))
    expect(await screen.findByText(/el nombre es obligatorio/i)).toBeInTheDocument()
    expect(screen.getByText(/el correo es obligatorio/i)).toBeInTheDocument()
    expect(screen.getByText(/la contraseña es obligatoria/i)).toBeInTheDocument()
    expect(crearUsuario).not.toHaveBeenCalled()
  })

  it('rechaza una contrasena menor a 8 caracteres', async () => {
    renderizar()
    await userEvent.type(screen.getByLabelText(/nombre completo/i), 'Ana Gomez')
    await userEvent.type(screen.getByLabelText(/correo/i), 'ana@ejemplo.com')
    await userEvent.type(screen.getByLabelText(/contraseña/i), '123')
    await userEvent.click(screen.getByRole('button', { name: /guardar usuario/i }))
    expect(await screen.findByText(/al menos 8 caracteres/i)).toBeInTheDocument()
    expect(crearUsuario).not.toHaveBeenCalled()
  })

  it('crea el usuario con datos validos', async () => {
    crearUsuario.mockResolvedValue({ id: 5 })
    renderizar()
    await userEvent.type(screen.getByLabelText(/nombre completo/i), 'Ana Gomez')
    await userEvent.type(screen.getByLabelText(/correo/i), 'ana@ejemplo.com')
    await userEvent.type(screen.getByLabelText(/contraseña/i), 'secreta123')
    await userEvent.click(screen.getByRole('button', { name: /guardar usuario/i }))
    await waitFor(() => expect(crearUsuario).toHaveBeenCalled())
    expect(crearUsuario).toHaveBeenCalledWith(
      expect.objectContaining({ correo: 'ana@ejemplo.com', rol: 'CUIDADOR_ENFERMERO' }),
    )
  })

  it('muestra el conflicto de correo duplicado (409) sobre el campo correo', async () => {
    crearUsuario.mockRejectedValue(new ErrorApi({ status: 409, codigo: 'CONFLICTO' }))
    renderizar()
    await userEvent.type(screen.getByLabelText(/nombre completo/i), 'Ana Gomez')
    await userEvent.type(screen.getByLabelText(/correo/i), 'ana@ejemplo.com')
    await userEvent.type(screen.getByLabelText(/contraseña/i), 'secreta123')
    await userEvent.click(screen.getByRole('button', { name: /guardar usuario/i }))
    expect(await screen.findByText(/ya existe un usuario con ese correo/i)).toBeInTheDocument()
  })

  it('en edicion carga los datos y no muestra el campo contrasena', async () => {
    obtenerUsuario.mockResolvedValue({
      id: 2,
      nombreCompleto: 'Luis Perez',
      correo: 'luis@ejemplo.com',
      telefono: '3000000000',
      rol: 'CUIDADOR_ENFERMERO',
      activo: true,
      ultimoAccesoEn: null,
      creadoEn: null,
      actualizadoEn: null,
    })
    renderizar('/usuarios/2/editar')
    await waitFor(() =>
      expect(screen.getByLabelText(/nombre completo/i)).toHaveValue('Luis Perez'),
    )
    expect(screen.queryByLabelText(/^contraseña$/i)).not.toBeInTheDocument()
    expect(screen.getByLabelText(/correo/i)).toBeDisabled()
  })
})
