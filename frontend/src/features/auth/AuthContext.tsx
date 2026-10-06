import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { ContextoAuth } from '@/features/auth/contextoAuth'
import { registrarManejadorNoAutenticado, registrarToken } from '@/lib/api-client'
import { iniciarSesion as iniciarSesionApi, obtenerUsuarioActual } from '@/features/auth/services/authService'
import { guardarSesion, leerSesion, limpiarSesion } from '@/features/auth/sesion'
import type {
  ContextoAutenticacion,
  CredencialesInicioSesion,
  UsuarioAutenticado,
} from '@/features/auth/types'

// Contexto de autenticacion REAL contra el backend (Paso 3).
//
// - Guarda token y usuario en sessionStorage (se pierde al cerrar la pestana).
// - Al cargar la app restaura la sesion y la valida con GET /api/auth/me.
// - Programa el cierre de sesion al vencer expiraEnSegundos (no hay refresh).
// - Registra en el api-client como leer el token y que hacer ante un 401.

/** Milisegundos por segundo, para el temporizador de expiracion. */
const MS_POR_SEGUNDO = 1000
export function ProveedorAutenticacion({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioAutenticado | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [cargando, setCargando] = useState(true)
  const [errorSesion, setErrorSesion] = useState<string | null>(null)
  // Timer de expiracion de la sesion (se cancela al cerrar sesion o desmontar).
  const temporizador = useRef<ReturnType<typeof setTimeout> | null>(null)

  /** Cancela el temporizador de expiracion pendiente. */
  const cancelarTemporizador = useCallback(() => {
    if (temporizador.current) {
      clearTimeout(temporizador.current)
      temporizador.current = null
    }
  }, [])

  /** Limpia el estado y sessionStorage (no navega; lo hace el guard). */
  const limpiarEstado = useCallback(() => {
    cancelarTemporizador()
    limpiarSesion()
    setUsuario(null)
    setToken(null)
  }, [cancelarTemporizador])

  /**
   * Programa el cierre de sesion cuando vence el token. El JWT no se
   * refresca: al expirar, se limpia la sesion y el guard manda a /login.
   */
  const programarExpiracion = useCallback(
    (expiraEnSegundos: number) => {
      cancelarTemporizador()
      temporizador.current = setTimeout(
        () => limpiarEstado(),
        expiraEnSegundos * MS_POR_SEGUNDO,
      )
    },
    [cancelarTemporizador, limpiarEstado],
  )

  // El api-client consulta el token y notifica los 401 NO_AUTENTICADO.
  useEffect(() => {
    registrarToken(() => token)
  }, [token])

  useEffect(() => {
    registrarManejadorNoAutenticado(() => limpiarEstado())
    return () => registrarManejadorNoAutenticado(() => {})
  }, [limpiarEstado])

  // Restauracion de sesion al cargar la app: si hay token guardado, se
  // valida contra GET /api/auth/me; si falla, se limpia todo.
  useEffect(() => {
    const sesion = leerSesion()
    if (!sesion) {
      setCargando(false)
      return
    }
    obtenerUsuarioActual()
      .then((usuarioActual) => {
        setUsuario(usuarioActual)
        setToken(sesion.token)
        guardarSesion({ token: sesion.token, usuario: usuarioActual })
      })
      .catch(() => limpiarEstado())
      .finally(() => setCargando(false))
  }, [limpiarEstado])

  // Limpieza del temporizador al desmontar el provider.
  useEffect(() => cancelarTemporizador, [cancelarTemporizador])

  const iniciarSesion = useCallback(
    async (credenciales: CredencialesInicioSesion): Promise<UsuarioAutenticado> => {
      setErrorSesion(null)
      const respuesta = await iniciarSesionApi(credenciales)
      guardarSesion({ token: respuesta.token, usuario: respuesta.usuario })
      setUsuario(respuesta.usuario)
      setToken(respuesta.token)
      programarExpiracion(respuesta.expiraEnSegundos)
      return respuesta.usuario
    },
    [programarExpiracion],
  )

  const cerrarSesion = useCallback(() => {
    setErrorSesion(null)
    limpiarEstado()
  }, [limpiarEstado])

  const valor = useMemo<ContextoAutenticacion>(
    () => ({ usuario, token, cargando, errorSesion, iniciarSesion, cerrarSesion }),
    [usuario, token, cargando, errorSesion, iniciarSesion, cerrarSesion],
  )

  return <ContextoAuth.Provider value={valor}>{children}</ContextoAuth.Provider>
}
