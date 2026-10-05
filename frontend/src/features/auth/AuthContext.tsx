import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { ContextoAuth } from '@/features/auth/contextoAuth'
import type { ContextoAutenticacion, CredencialesInicioSesion, RolUsuario, UsuarioAutenticado } from '@/features/auth/types'

// Contexto de autenticacion SIMULADO.
//
// IMPORTANTE (Paso 2): no se conecta a la API real, no valida credenciales
// reales y NO persiste nada en localStorage. La forma del estado imita el
// contrato real para que el Paso 3 reemplace unicamente iniciarSesion/cerrarSesion
// contra POST /api/auth/login sin tocar las pantallas.

// Directorio de usuarios de prueba usado solo por la simulacion del Paso 2.
const USUARIOS_SIMULADOS: Record<RolUsuario, Omit<UsuarioAutenticado, 'correo'>> = {
  ADMINISTRADOR: { id: 1, nombreCompleto: 'Mariana Lopez', rol: 'ADMINISTRADOR' },
  CUIDADOR_ENFERMERO: { id: 2, nombreCompleto: 'Andres Cuellar', rol: 'CUIDADOR_ENFERMERO' },
  FAMILIAR_AUTORIZADO: { id: 3, nombreCompleto: 'Camila Restrepo', rol: 'FAMILIAR_AUTORIZADO' },
}

export function ProveedorAutenticacion({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<UsuarioAutenticado | null>(null)
  const [token, setToken] = useState<string | null>(null)

  const iniciarSesion = useCallback(
    async (credenciales: CredencialesInicioSesion, rolSimulado: RolUsuario = 'ADMINISTRADOR') => {
      // Simulacion: acepta cualquier credencial de prueba y construye el usuario
      // a partir del rol elegido en la pantalla de inicio de sesion.
      const base = USUARIOS_SIMULADOS[rolSimulado]
      await Promise.resolve()
      setUsuario({ ...base, correo: credenciales.correo })
      setToken('token-simulado-paso-2')
    },
    [],
  )

  const cerrarSesion = useCallback(() => {
    setUsuario(null)
    setToken(null)
  }, [])

  const valor = useMemo<ContextoAutenticacion>(
    () => ({ usuario, token, iniciarSesion, cerrarSesion }),
    [usuario, token, iniciarSesion, cerrarSesion],
  )

  return <ContextoAuth.Provider value={valor}>{children}</ContextoAuth.Provider>
}