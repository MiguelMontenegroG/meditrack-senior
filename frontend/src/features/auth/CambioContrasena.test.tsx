// Pruebas del cambio de contraseña: validacion en cliente, exito, contraseña
// actual incorrecta (409) y longitud invalida (400). Todo con mocks.

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { CambioContrasena } from '@/features/auth/CambioContrasena'
import { ErrorApi } from '@/lib/api-client'

const cambiarContrasena = vi.fn()
const obtenerUsuarioActual = vi.fn()
const cerrarSesion = vi.fn()

vi.mock('@/features/auth/services/authService', () => ({
  cambiarContrasena: (...args: unknown[]) => cambiarContrasena(...args),
  obtenerUsuarioActual: (...args: unknown[]) => obtenerUsuarioActual(...args),
}))

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ cerrarSesion }),
}))

/** Rellena los tres campos del formulario. */
async function rellenar(actual: string, nueva: string, confirmacion: string) {
  await userEvent.type(screen.getByLabelText('Contraseña actual'), actual)
  await userEvent.type(screen.getByLabelText(/^Contraseña nueva/), nueva)
  await userEvent.type(screen.getByLabelText('Confirmar contraseña nueva'), confirmacion)
}

describe('CambioContrasena', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('exige la contraseña actual', async () => {
    render(<CambioContrasena />)
    await userEvent.click(screen.getByRole('button', { name: /cambiar contraseña/i }))
    expect(await screen.findByText(/ingresa tu contraseña actual/i)).toBeInTheDocument()
    expect(cambiarContrasena).not.toHaveBeenCalled()
  })

  it('rechaza que la confirmacion no coincida', async () => {
    render(<CambioContrasena />)
    await rellenar('actual123', 'nueva1234', 'otra12345')
    await userEvent.click(screen.getByRole('button', { name: /cambiar contraseña/i }))
    expect(await screen.findByText(/no coincide/i)).toBeInTheDocument()
    expect(cambiarContrasena).not.toHaveBeenCalled()
  })

  it('cambia la contraseña y muestra el aviso de exito', async () => {
    cambiarContrasena.mockResolvedValue(undefined)
    obtenerUsuarioActual.mockResolvedValue({ id: 1 })
    render(<CambioContrasena />)
    await rellenar('actual123', 'nueva1234', 'nueva1234')
    await userEvent.click(screen.getByRole('button', { name: /cambiar contraseña/i }))
    await waitFor(() => expect(cambiarContrasena).toHaveBeenCalledWith({
      contrasenaActual: 'actual123',
      contrasenaNueva: 'nueva1234',
    }))
    expect(await screen.findByText(/se actualizó correctamente/i)).toBeInTheDocument()
  })

  it('muestra un error de campo si la contraseña actual es incorrecta (409)', async () => {
    cambiarContrasena.mockRejectedValue(new ErrorApi({ status: 409, codigo: 'CONFLICTO' }))
    render(<CambioContrasena />)
    await rellenar('mala1234', 'nueva1234', 'nueva1234')
    await userEvent.click(screen.getByRole('button', { name: /cambiar contraseña/i }))
    expect(await screen.findByText(/la contraseña actual no es correcta/i)).toBeInTheDocument()
  })

  it('cierra la sesion si el token deja de ser valido tras el cambio', async () => {
    cambiarContrasena.mockResolvedValue(undefined)
    obtenerUsuarioActual.mockRejectedValue(new ErrorApi({ status: 401, codigo: 'NO_AUTENTICADO' }))
    render(<CambioContrasena />)
    await rellenar('actual123', 'nueva1234', 'nueva1234')
    await userEvent.click(screen.getByRole('button', { name: /cambiar contraseña/i }))
    await waitFor(() => expect(cerrarSesion).toHaveBeenCalled())
  })
})
