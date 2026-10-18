// Pruebas del formulario de esquema: valida frecuencia y cantidad de horarios,
// crea el esquema y muestra el conflicto de solapamiento (409).

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { FormularioEsquema } from '@/features/medicacion/FormularioEsquema'
import { ErrorApi } from '@/lib/api-client'
import * as esquemaService from '@/features/medicacion/services/esquemaService'
import * as medicamentoService from '@/features/medicacion/services/medicamentoService'

const MEDICAMENTOS = [
  { id: 5, nombre: 'Losartan', presentacion: '50 mg', activo: true },
]

function renderizar() {
  return render(
    <MemoryRouter initialEntries={['/residentes/7/esquemas/nuevo']}>
      <Routes>
        <Route path="/residentes/:id/esquemas/nuevo" element={<FormularioEsquema />} />
        <Route path="/residentes/:id" element={<p>Detalle</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

/** Espera a que cargue el selector de medicamentos. */
async function esperarSelector() {
  await waitFor(() =>
    expect(screen.getByRole('option', { name: /losartan/i })).toBeInTheDocument(),
  )
}

describe('FormularioEsquema', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('exige al menos un horario', async () => {
    vi.spyOn(medicamentoService, 'listarMedicamentosActivos').mockResolvedValue(MEDICAMENTOS)
    const espia = vi.spyOn(esquemaService, 'crearEsquema')
    const usuario = userEvent.setup()
    renderizar()
    await esperarSelector()
    await usuario.type(screen.getByLabelText(/dosis/i), '50 mg')
    await usuario.click(screen.getByRole('button', { name: /guardar esquema/i }))
    expect(await screen.findByText(/al menos un horario/i)).toBeInTheDocument()
    expect(espia).not.toHaveBeenCalled()
  })

  it('crea el esquema con medicamento, dosis y horarios validos', async () => {
    vi.spyOn(medicamentoService, 'listarMedicamentosActivos').mockResolvedValue(MEDICAMENTOS)
    const espia = vi
      .spyOn(esquemaService, 'crearEsquema')
      .mockResolvedValue({ id: 1 } as never)
    const usuario = userEvent.setup()
    renderizar()
    await esperarSelector()
    await usuario.selectOptions(screen.getByLabelText(/medicamento/i), '5')
    await usuario.clear(screen.getByLabelText(/dosis/i))
    await usuario.type(screen.getByLabelText(/dosis/i), '50 mg')
    // Frecuencia 12 => 2 horarios esperados.
    await usuario.clear(screen.getByLabelText(/frecuencia/i))
    await usuario.type(screen.getByLabelText(/frecuencia/i), '12')
    await usuario.click(screen.getByRole('button', { name: /agregar horario/i }))
    await usuario.click(screen.getByRole('button', { name: /agregar horario/i }))
    const horas = screen.getAllByLabelText(/hora \d/i)
    await usuario.type(horas[0], '08:00')
    await usuario.type(horas[1], '20:00')
    await usuario.click(screen.getByRole('button', { name: /guardar esquema/i }))
    await waitFor(() => expect(espia).toHaveBeenCalledTimes(1))
    const [, datos] = espia.mock.calls[0]
    expect(datos.medicamentoId).toBe(5)
    expect(datos.horarios).toEqual(['08:00', '20:00'])
    expect(await screen.findByText('Detalle')).toBeInTheDocument()
  })

  it('rechaza una frecuencia fuera de 1 a 24', async () => {
    vi.spyOn(medicamentoService, 'listarMedicamentosActivos').mockResolvedValue(MEDICAMENTOS)
    const espia = vi.spyOn(esquemaService, 'crearEsquema')
    const usuario = userEvent.setup()
    renderizar()
    await esperarSelector()
    await usuario.clear(screen.getByLabelText(/frecuencia/i))
    await usuario.type(screen.getByLabelText(/frecuencia/i), '30')
    await usuario.click(screen.getByRole('button', { name: /guardar esquema/i }))
    expect(await screen.findByText(/debe estar entre 1 y 24 horas/i)).toBeInTheDocument()
    expect(espia).not.toHaveBeenCalled()
  })

  it('muestra el conflicto de solapamiento (409) en el error general', async () => {
    vi.spyOn(medicamentoService, 'listarMedicamentosActivos').mockResolvedValue(MEDICAMENTOS)
    vi.spyOn(esquemaService, 'crearEsquema').mockRejectedValue(
      new ErrorApi({ status: 409, codigo: 'CONFLICTO', mensaje: 'solapa' }),
    )
    const usuario = userEvent.setup()
    renderizar()
    await esperarSelector()
    await usuario.selectOptions(screen.getByLabelText(/medicamento/i), '5')
    await usuario.type(screen.getByLabelText(/dosis/i), '50 mg')
    await usuario.clear(screen.getByLabelText(/frecuencia/i))
    await usuario.type(screen.getByLabelText(/frecuencia/i), '12')
    await usuario.click(screen.getByRole('button', { name: /agregar horario/i }))
    await usuario.click(screen.getByRole('button', { name: /agregar horario/i }))
    const horas = screen.getAllByLabelText(/hora \d/i)
    await usuario.type(horas[0], '08:00')
    await usuario.type(horas[1], '20:00')
    await usuario.click(screen.getByRole('button', { name: /guardar esquema/i }))
    expect(await screen.findByText(/fechas que se solapan/i)).toBeInTheDocument()
  })
})
