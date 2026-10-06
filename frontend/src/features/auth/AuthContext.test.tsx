import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { act, render, screen, waitFor } from '@testing-library/react'
import { ProveedorAutenticacion } from '@/features/auth/AuthContext'
import { useAutenticacion } from '@/features/auth/useAutenticacion'

// Pruebas del AuthContext con fetch simulado (sin backend real).

/** Componente minimo que expone el estado del contexto para verificar. */
function Consumidor() {
  const { usuario, cargando, errorSesion } = useAutenticacion()
  return (
    <div>
      <span data-testid="usuario">{usuario ? usuario.nombreCompleto : 'sin-sesion'}</span>
      <span data-testid="cargando">{String(cargando)}</span>
      <span data-testid="error">{errorSesion ?? ''}</span>
    </div>
  )
}

const USUARIO = {
  id: 1,
  nombreCompleto: 'Mariana Lopez',
  correo: 'mariana@solandes.co',
  rol: 'ADMINISTRADOR',
}

function respuesta(cuerpo: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: new Headers({ 'content-type': 'application/json' }),
    json: async () => cuerpo,
    text: async () => JSON.stringify(cuerpo),
  } as unknown as Response
}

/** Renderiza el provider y expone el contexto via una referencia mutable. */
function renderizarContexto() {
  const ref: { actual: ReturnType<typeof useAutenticacion> | null } = { actual: null }
  function Captura() {
    ref.actual = useAutenticacion()
    return null
  }
  render(
    <ProveedorAutenticacion>
      <Consumidor />
      <Captura />
    </ProveedorAutenticacion>,
  )
  return ref
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_API_URL', 'http://localhost:8080/api')
    sessionStorage.clear()
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    vi.restoreAllMocks()
  })

  it('inicia sesion correctamente y persiste en sessionStorage', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        respuesta({ token: 'jwt-abc', tipo: 'Bearer', expiraEnSegundos: 3600, usuario: USUARIO }),
      ),
    )
    const ref = renderizarContexto()
    await waitFor(() => expect(screen.getByTestId('cargando').textContent).toBe('false'))

    await act(async () => {
      await ref.actual!.iniciarSesion({ correo: USUARIO.correo, contrasena: 'secreto123' })
    })

    await waitFor(() => expect(screen.getByTestId('usuario').textContent).toBe('Mariana Lopez'))
    expect(sessionStorage.getItem('meditrack.token')).toBe('jwt-abc')
  })

  it('propaga el error de login fallido sin crear sesion', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(respuesta({ codigo: 'CREDENCIALES_INVALIDAS' }, 401)),
    )
    const ref = renderizarContexto()
    await waitFor(() => expect(screen.getByTestId('cargando').textContent).toBe('false'))

    await act(async () => {
      await expect(ref.actual!.iniciarSesion({ correo: 'x@x.co', contrasena: 'mala' })).rejects.toThrow()
    })

    expect(screen.getByTestId('usuario').textContent).toBe('sin-sesion')
    expect(sessionStorage.getItem('meditrack.token')).toBeNull()
  })

  it('restaura la sesion guardada validando con GET /api/auth/me', async () => {
    sessionStorage.setItem('meditrack.token', 'jwt-abc')
    sessionStorage.setItem('meditrack.usuario', JSON.stringify(USUARIO))
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(respuesta(USUARIO)))

    renderizarContexto()

    await waitFor(() => expect(screen.getByTestId('usuario').textContent).toBe('Mariana Lopez'))
  })

  it('cierra sesion y limpia el sessionStorage', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        respuesta({ token: 'jwt-abc', tipo: 'Bearer', expiraEnSegundos: 3600, usuario: USUARIO }),
      ),
    )
    const ref = renderizarContexto()
    await waitFor(() => expect(screen.getByTestId('cargando').textContent).toBe('false'))
    await act(async () => {
      await ref.actual!.iniciarSesion({ correo: USUARIO.correo, contrasena: 'secreto123' })
    })
    await waitFor(() => expect(screen.getByTestId('usuario').textContent).toBe('Mariana Lopez'))

    act(() => ref.actual!.cerrarSesion())

    await waitFor(() => expect(screen.getByTestId('usuario').textContent).toBe('sin-sesion'))
    expect(sessionStorage.getItem('meditrack.token')).toBeNull()
    expect(sessionStorage.getItem('meditrack.usuario')).toBeNull()
  })
})