import { Outlet } from 'react-router-dom'

// Layout de las rutas de autenticacion (login). No envuelve con el chrome
// de la app: la pantalla de inicio de sesion llena el viewport por si misma.
export function AuthLayout() {
  return <Outlet />
}
