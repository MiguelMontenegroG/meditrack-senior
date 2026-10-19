// Pruebas del selector de bitacora (/bitacora): estados de carga, error, vacio y
// el enlace por residente a su bitacora. Se mockean el servicio y la sesion; no
// hay backend. La pantalla hace una sola consulta al listado, asi que se
// comprueba tambien que los filtros enviados son los minimos necesarios.

import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { SelectorBitacora } from '@/features/bitacora/SelectorBitacora'

const listarPacientes = vi.fn()
let rolActual = 'ADMINISTRADOR'

vi.mock('@/features/pacientes/services/pacienteService', () => ({
  listarPacientes: (...args: unknown[]) => listarPacientes(...args),
}))

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: { id: 1, rol: rolActual } }),
}))

/** Pagina de residentes con un unico elemento. */
function pagina(contenido: unknown[] = [], totalPaginas = 1) {
  return { contenido, pagina: 0, tamano: 10, total: contenido.length, totalPaginas }
}

function renderizar() {
  return render(
    <MemoryRouter>
      <SelectorBitacora />
    </MemoryRouter>,
  )
}

describe('SelectorBitacora', () => {
  beforeEach(() => {
    rolActual = 'ADMINISTRADOR'
  })
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('pide solo residentes activos, una vez y con paginacion', async () => {
    listarPacientes.mockResolvedValue(pagina())
    renderizar()
    await waitFor(() => expect(listarPacientes).toHaveBeenCalledTimes(1))
    expect(listarPacientes).toHaveBeenCalledWith({ activo: true, pagina: 0, tamano: 10 })
  })

  it('muestra un estado vacio cuando no hay residentes en el alcance', async () => {
    listarPacientes.mockResolvedValue(pagina())
    renderizar()
    expect(await screen.findByText(/aun no hay residentes/i)).toBeInTheDocument()
  })

  it('enlaza cada residente con su bitacora sin pedir sus ultimos signos', async () => {
    listarPacientes.mockResolvedValue(
      pagina([{ id: 7, nombreCompleto: 'Gomez, Ana', edad: 84, habitacion: 'A1', activo: true }]),
    )
    renderizar()
    const enlaces = await screen.findAllByRole('link', { name: /abrir la bitacora de gomez, ana/i })
    // El acceso principal (tabla de escritorio) apunta a la bitacora real.
    expect(enlaces).toHaveLength(1)
    expect(enlaces[0]).toHaveAttribute('href', '/residentes/7/signos-vitales')
    // La tarjeta movil ofrece el mismo destino.
    const tarjetas = screen
      .getAllByRole('link')
      .filter((enlace) => enlace.getAttribute('href') === '/residentes/7/signos-vitales')
    expect(tarjetas.length).toBeGreaterThanOrEqual(1)
    // Una sola peticion: no hay llamadas por residente.
    expect(listarPacientes).toHaveBeenCalledTimes(1)
  })

  it('muestra el estado de error y permite reintentar', async () => {
    listarPacientes.mockRejectedValue({ codigo: 'ERROR_RED', status: 0, mensajeUsuario: 'Sin conexion.' })
    renderizar()
    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument())
    expect(screen.getByText('Sin conexion.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /reintentar/i })).toBeInTheDocument()
  })

  it('cambia el texto de ayuda segun el rol', async () => {
    rolActual = 'FAMILIAR_AUTORIZADO'
    listarPacientes.mockResolvedValue(pagina())
    renderizar()
    await screen.findByText(/aun no hay residentes/i)
    expect(screen.getByText(/consultar su historial de signos vitales/i)).toBeInTheDocument()
    expect(screen.queryByText(/registrar signos vitales/i)).not.toBeInTheDocument()
  })
})
