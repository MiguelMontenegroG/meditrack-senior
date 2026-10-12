import { createBrowserRouter, Navigate } from 'react-router-dom'
import { EnConstruccion } from '@/components/ui/EnConstruccion'
import { RutaLogin } from '@/app/RutaLogin'
import { RutaProtegida } from '@/features/auth/guards/RutaProtegida'
import { CambioContrasena } from '@/features/auth/CambioContrasena'
import { PanelAdministrador } from '@/features/dashboard/PanelAdministrador'
import { VistaCuidador } from '@/features/cuidador/VistaCuidador'
import { ListadoResidentes } from '@/features/pacientes/ListadoResidentes'
import { DetalleResidente } from '@/features/pacientes/DetalleResidente'
import { FormularioResidente } from '@/features/pacientes/FormularioResidente'
import { ListadoUsuarios } from '@/features/usuarios/ListadoUsuarios'
import { FormularioUsuario } from '@/features/usuarios/FormularioUsuario'
import { AuthLayout } from '@/layouts/AuthLayout'
import { MainLayout } from '@/layouts/MainLayout'

// Router real de la aplicacion. Rutas publicas para login; rutas protegidas
// por rol. Los residentes son transversales: la escritura (nuevo/editar) queda
// en un bloque solo para ADMINISTRADOR y la lectura segun alcance en otro.
export const router = createBrowserRouter([
  {
    element: <AuthLayout />,
    children: [{ path: '/login', element: <RutaLogin /> }],
  },
  // Rutas de escritura de residentes: solo ADMINISTRADOR.
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR']} />,
    children: [
      {
        element: <MainLayout titulo="Residentes" />,
        children: [
          { path: '/residentes/nuevo', element: <FormularioResidente /> },
          { path: '/residentes/:id/editar', element: <FormularioResidente /> },
        ],
      },
    ],
  },
  // Rutas de lectura de residentes: cualquier rol autenticado (alcance del backend).
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <MainLayout titulo="Residentes" />,
        children: [
          { path: '/residentes', element: <ListadoResidentes /> },
          { path: '/residentes/:id', element: <DetalleResidente /> },
        ],
      },
    ],
  },
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR']} />,
    children: [
      {
        element: <MainLayout titulo="Panel" />,
        children: [
          { path: '/dashboard', element: <PanelAdministrador /> },
          { path: '/calendario', element: <EnConstruccion titulo="Calendario" /> },
          { path: '/bitacora', element: <EnConstruccion titulo="Bitacora" /> },
          { path: '/alertas', element: <EnConstruccion titulo="Alertas" /> },
          { path: '/reportes', element: <EnConstruccion titulo="Reportes" /> },
        ],
      },
      {
        element: <MainLayout titulo="Usuarios" />,
    children: [
          { path: '/usuarios', element: <ListadoUsuarios /> },
          { path: '/usuarios/nuevo', element: <FormularioUsuario /> },
          { path: '/usuarios/:id/editar', element: <FormularioUsuario /> },
        ],
      },
    ],
  },
  // Cambio de la propia contrasena: cualquier rol autenticado.
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <MainLayout titulo="Mi cuenta" />,
        children: [{ path: '/cuenta/contrasena', element: <CambioContrasena /> }],
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
    path: '/familiar',
    element: <Navigate to="/residentes" replace />,
  },
  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '*', element: <Navigate to="/login" replace /> },
])