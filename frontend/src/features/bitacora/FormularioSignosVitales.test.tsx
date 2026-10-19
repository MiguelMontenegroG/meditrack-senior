// Pruebas del formulario de signos vitales: exige al menos un signo, valida
// rangos y coherencia de la presion, convierte la fecha de Bogota a instante
// UTC y muestra el error contextual del backend (400 VALIDACION).

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ErrorApi } from '@/lib/api-client'
import { FormularioSignosVitales } from '@/features/bitacora/FormularioSignosVitales'
import * as bitacoraService from '@/features/bitacora/services/bitacoraService'

function renderizar(alRegistrar = vi.fn(), alCancelar = vi.fn()) {
  render(
    <FormularioSignosVitales
      pacienteId={7}
      alRegistrar={alRegistrar}
      alCancelar={alCancelar}
    />,
  )
  return { alRegistrar, alCancelar }
}

/** Rellena fecha y hora fijas para que las pruebas sean deterministas. */
async function fijarFechaHora(usuario: ReturnType<typeof userEvent.setup>) {
  const fecha = screen.getByLabelText(/^fecha$/i)
  await usuario.clear(fecha)
  await usuario.type(fecha, '2026-01-20')
  const hora = screen.getByLabelText(/^hora$/i)
  await usuario.clear(hora)
  await usuario.type(hora, '09:00')
}

describe('FormularioSignosVitales', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('exige al menos un signo vital', async () => {
    const espia = vi.spyOn(bitacoraService, 'registrarSignosVitales')
    const usuario = userEvent.setup()
    renderizar()
    await usuario.click(screen.getByRole('button', { name: /registrar signos/i }))
    expect(await screen.findByText(/al menos un signo vital/i)).toBeInTheDocument()
    expect(espia).not.toHaveBeenCalled()
  })

  it('rechaza una diastolica mayor o igual que la sistolica', async () => {
    const espia = vi.spyOn(bitacoraService, 'registrarSignosVitales')
    const usuario = userEvent.setup()
    renderizar()
    await usuario.type(screen.getByLabelText(/sistolica/i), '120')
    await usuario.type(screen.getByLabelText(/diastolica/i), '130')
    await usuario.click(screen.getByRole('button', { name: /registrar signos/i }))
    expect(await screen.findByText(/menor que la sistolica/i)).toBeInTheDocument()
    expect(espia).not.toHaveBeenCalled()
  })

  it('rechaza una temperatura fuera de rango', async () => {
    const espia = vi.spyOn(bitacoraService, 'registrarSignosVitales')
    const usuario = userEvent.setup()
    renderizar()
    await usuario.type(screen.getByLabelText(/temperatura/i), '50')
    await usuario.click(screen.getByRole('button', { name: /registrar signos/i }))
    expect(await screen.findByText(/temperatura debe estar entre 30 y 45/i)).toBeInTheDocument()
    expect(espia).not.toHaveBeenCalled()
  })

  it('registra aceptando coma decimal y convierte la hora de Bogota a UTC', async () => {
    const espia = vi
      .spyOn(bitacoraService, 'registrarSignosVitales')
      .mockResolvedValue({} as never)
    const alRegistrar = vi.fn()
    const usuario = userEvent.setup()
    renderizar(alRegistrar)
    await fijarFechaHora(usuario)
    await usuario.type(screen.getByLabelText(/sistolica/i), '120')
    await usuario.type(screen.getByLabelText(/diastolica/i), '80')
    await usuario.type(screen.getByLabelText(/glucosa/i), '102')
    await usuario.type(screen.getByLabelText(/temperatura/i), '36,5')
    await usuario.type(screen.getByLabelText(/observaciones/i), 'Sin novedad')
    await usuario.click(screen.getByRole('button', { name: /registrar signos/i }))

    await waitFor(() => expect(espia).toHaveBeenCalledTimes(1))
    const [pacienteId, datos] = espia.mock.calls[0]
    expect(pacienteId).toBe(7)
    // 09:00 en Bogota (UTC-5) son las 14:00 UTC.
    expect(datos.fechaHora).toBe('2026-01-20T14:00:00.000Z')
    expect(datos.presionSistolica).toBe(120)
    expect(datos.presionDiastolica).toBe(80)
    expect(datos.glucosaMgDl).toBe(102)
    expect(datos.temperaturaC).toBe(36.5)
    expect(datos.observaciones).toBe('Sin novedad')
    expect(alRegistrar).toHaveBeenCalledTimes(1)
  })

  it('permite registrar solo temperatura (subconjunto de signos)', async () => {
    const espia = vi
      .spyOn(bitacoraService, 'registrarSignosVitales')
      .mockResolvedValue({} as never)
    const usuario = userEvent.setup()
    renderizar()
    await fijarFechaHora(usuario)
    await usuario.type(screen.getByLabelText(/temperatura/i), '37')
    await usuario.click(screen.getByRole('button', { name: /registrar signos/i }))
    await waitFor(() => expect(espia).toHaveBeenCalledTimes(1))
    const [, datos] = espia.mock.calls[0]
    expect(datos.presionSistolica).toBeNull()
    expect(datos.glucosaMgDl).toBeNull()
    expect(datos.temperaturaC).toBe(37)
  })

  it('muestra el error contextual ante un 400 VALIDACION del backend', async () => {
    vi.spyOn(bitacoraService, 'registrarSignosVitales').mockRejectedValue(
      new ErrorApi({ status: 400, codigo: 'VALIDACION', mensaje: 'incoherente' }),
    )
    const usuario = userEvent.setup()
    renderizar()
    await fijarFechaHora(usuario)
    await usuario.type(screen.getByLabelText(/glucosa/i), '100')
    await usuario.click(screen.getByRole('button', { name: /registrar signos/i }))
    expect(
      await screen.findByText(/deben estar dentro del rango y la diastolica ser menor/i),
    ).toBeInTheDocument()
  })
})
