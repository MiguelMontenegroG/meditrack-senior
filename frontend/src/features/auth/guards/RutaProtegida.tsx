import { Navigate, Outlet } from 'react-router-dom'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { RUTA_INICIAL_ROL, type RolUsuario } from '@/features/auth/types'

// Guard de rutas por rol. Mientras se restaura la sesion muestra un estado de
// carga; si no hay sesion redirige a /login; si el rol no esta autorizado
// redirige a la ruta inicial del rol del usuario (nunca pantalla en blanco).
// El backend valida de verdad; este guard es solo de navegacion.

interface PropsRutaProtegida {
  /** Roles con acceso. Si se omite, basta con estar autenticado. */
  rolesPermitidos?: RolUsuario[]
}

/** Envoltura de rutas protegidas por autenticacion y rol. */
export function RutaProtegida({ rolesPermitidos }: PropsRutaProtegida) {
  const { usuario, cargando } = useAutenticacion()

  if (cargando) {
    return <EstadoCarga mensaje="Verificando tu sesion..." />
  }

  if (!usuario) {
    return <Navigate to="/login" replace />
  }

  if (rolesPermitidos && !rolesPermitidos.includes(usuario.rol)) {
    return <Navigate to={RUTA_INICIAL_ROL[usuario.rol]} replace />
  }

  return <Outlet />
}