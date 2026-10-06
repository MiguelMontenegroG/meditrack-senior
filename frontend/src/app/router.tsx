import { createBrowserRouter, Navigate } from 'react-router-dom'
import { EnConstruccion } from '@/components/ui/EnConstruccion'
import { RutaLogin } from '@/app/RutaLogin'
import { RutaProtegida } from '@/features/auth/guards/RutaProtegida'
import { PanelAdministrador } from '@/features/dashboard/PanelAdministrador'
import { VistaCuidador } from '@/features/cuidador/VistaCuidador'
import { AuthLayout } from '@/layouts/AuthLayout'
import { MainLayout } from '@/layouts/MainLayout'

// Router real de la aplicacion. Rutas publicas para login, rutas protegidas
// por rol para panel de administrador, vista de cuidador y la espera del
// familiar autorizado (EnConstruccion).

export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [{ path: '/login', element: <RutaLogin /> }],
  },
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR']} />,
    children: [
      {
        element: <MainLayout titulo="Panel" />,
        children: [
          { path: '/dashboard', element: <PanelAdministrador /> },
          { path: '/residentes', element: <EnConstruccion titulo="Residentes" /> },
          { path: '/calendario', element: <EnConstruccion titulo="Calendario" /> },
          { path: '/bitacora', element: <EnConstruccion titulo="Bitacora" /> },
          { path: '/alertas', element: <EnConstruccion titulo="Alertas" /> },
          { path: '/reportes', element: <EnConstruccion titulo="Reportes" /> },
          { path: '/usuarios', element: <EnConstruccion titulo="Usuarios" /> },
        ],
      },
    ],
  },
  {
    element: <RutaProtegida rolesPermitidos={['CUIDADOR_ENFERMERO']} />,
    children: [
      {
        element: <MainLayout titulo="Hoy" />,
        children: [{ path: '/cuidador', element: <VistaCuidador /> }],
      },
    ],
  },
  {
    element: <RutaProtegida rolesPermitidos={['FAMILIAR_AUTORIZADO']} />,
    children: [
      {
        element: <MainLayout titulo="Seguimiento" />,
        children: [{ path: '/familiar', element: <EnConstruccion titulo="Seguimiento del residente" /> }],
      },
    ],
  },
  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '*', element: <Navigate to="/login" replace /> },
])