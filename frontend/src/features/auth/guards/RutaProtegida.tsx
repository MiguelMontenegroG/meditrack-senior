import { Navigate, Outlet } from 'react-router-dom'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import type { RolUsuario } from '@/features/auth/types'

// Guard de rutas por rol. Si no hay sesion redirige a /login; si el rol no
// esta autorizado redirige a la ruta inicial del rol del usuario.
// Es "simple" a proposito: el backend valida de verdad (Paso 3).

interface PropsRutaProtegida {
  /** Roles con acceso. Si se omite, basta con estar autenticado. */
  rolesPermitidos?: RolUsuario[]
}

const RUTA_INICIAL: Record<RolUsuario, string> = {
  ADMINISTRADOR: '/dashboard',
  CUIDADOR_ENFERMERO: '/cuidador',
  FAMILIAR_AUTORIZADO: '/familiar',
}

/** Envoltura de rutas protegidas por autenticacion y rol. */
export function RutaProtegida({ rolesPermitidos }: PropsRutaProtegida) {
  const { usuario } = useAutenticacion()

  if (!usuario) {
    return <Navigate to="/login" replace />
  }

  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    return <Navigate to={RUTA_INICIAL[usuario.rol]} replace />
  }

  return <Outlet />
}
