// Pruebas del formulario de medicamento: validacion, alta correcta y conflicto
// de nombre duplicado (409) sobre el campo nombre.

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { FormularioMedicamento } from '@/features/medicacion/FormularioMedicamento'
import { ErrorApi } from '@/lib/api-client'

const crearMedicamento = vi.fn()
const actualizarMedicamento = vi.fn()

vi.mock('@/features/medicacion/services/medicamentoService', () => ({
  crearMedicamento: (...args: unknown[]) => crearMedicamento(...args),
  actualizarMedicamento: (...args: unknown[]) => actualizarMedicamento(...args),
}))

function renderizar(ruta = '/medicamentos/nuevo') {
  return render(
    <MemoryRouter initialEntries={[ruta]}>
      <Routes>
        <Route path="/medicamentos/nuevo" element={<FormularioMedicamento />} />
        <Route path="/medicamentos/:id/editar" element={<FormularioMedicamento />} />
        <Route path="/medicamentos" element={<p>Catalogo</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('FormularioMedicamento', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('exige el nombre antes de enviar', async () => {
    const usuario = userEvent.setup()
    renderizar()
    await usuario.click(screen.getByRole('button', { name: /guardar medicamento/i }))
    expect(await screen.findByRole('alert')).toHaveTextContent(/nombre es obligatorio/i)
    expect(crearMedicamento).not.toHaveBeenCalled()
  })

  it('crea el medicamento con datos validos', async () => {
    const usuario = userEvent.setup()
    crearMedicamento.mockResolvedValue({ id: 1 })
    renderizar()
    await usuario.type(screen.getByLabelText(/^nombre/i), 'Losartan')
    await usuario.click(screen.getByRole('button', { name: /guardar medicamento/i }))
    await waitFor(() => expect(crearMedicamento).toHaveBeenCalledTimes(1))
    expect(crearMedicamento).toHaveBeenCalledWith({ nombre: 'Losartan', presentacion: null })
    expect(await screen.findByText('Catalogo')).toBeInTheDocument()
  })

  it('muestra el conflicto de nombre duplicado (409) sobre el campo nombre', async () => {
    const usuario = userEvent.setup()
    crearMedicamento.mockRejectedValue(
      new ErrorApi({ status: 409, codigo: 'CONFLICTO', mensaje: 'duplicado' }),
    )
    renderizar()
    await usuario.type(screen.getByLabelText(/^nombre/i), 'Losartan')
    await usuario.click(screen.getByRole('button', { name: /guardar medicamento/i }))
    expect(await screen.findByRole('alert')).toHaveTextContent(/ya existe un medicamento/i)
  })

  it('precarga los datos al editar desde el estado de navegacion', async () => {
    renderizarInicial()
    expect(await screen.findByDisplayValue('Metformina')).toBeInTheDocument()
  })
})

/** Render de edicion pasando el medicamento por el estado de navegacion. */
function renderizarInicial() {
  return render(
    <MemoryRouter
      initialEntries={[
        { pathname: '/medicamentos/2/editar', state: { medicamento: MEDICAMENTO } },
      ]}
    >
      <Routes>
        <Route path="/medicamentos/:id/editar" element={<FormularioMedicamento />} />
        <Route path="/medicamentos" element={<p>Catalogo</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

const MEDICAMENTO = { id: 2, nombre: 'Metformina', presentacion: '850 mg', activo: true }
