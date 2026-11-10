import { createBrowserRouter, Navigate } from 'react-router-dom'
import { RutaLogin } from '@/app/RutaLogin'
import { RutaProtegida } from '@/features/auth/guards/RutaProtegida'
import { CambioContrasena } from '@/features/auth/CambioContrasena'
import { PanelAdministrador } from '@/features/dashboard/PanelAdministrador'
import { ListadoAlertas } from '@/features/alertas/ListadoAlertas'
import { GeneradorReportes } from '@/features/reportes/GeneradorReportes'
import { Calendario } from '@/features/citas/Calendario'
import { FormularioCita } from '@/features/citas/FormularioCita'
import { VistaCuidador } from '@/features/cuidador/VistaCuidador'
import { ListadoResidentes } from '@/features/pacientes/ListadoResidentes'
import { DetalleResidente } from '@/features/pacientes/DetalleResidente'
import { FormularioResidente } from '@/features/pacientes/FormularioResidente'
import { ListadoUsuarios } from '@/features/usuarios/ListadoUsuarios'
import { FormularioUsuario } from '@/features/usuarios/FormularioUsuario'
import { ListadoMedicamentosCatalogo } from '@/features/medicacion/ListadoMedicamentos'
import { FormularioMedicamento } from '@/features/medicacion/FormularioMedicamento'
import { FormularioEsquema } from '@/features/medicacion/FormularioEsquema'
import { TomasResidente } from '@/features/medicacion/TomasResidente'
import { BitacoraResidente } from '@/features/bitacora/BitacoraResidente'
import { SelectorBitacora } from '@/features/bitacora/SelectorBitacora'
import { AuthLayout } from '@/layouts/AuthLayout'
import { MainLayout } from '@/layouts/MainLayout'

// Router real de la aplicacion. Rutas publicas para login; rutas protegidas
// por rol. Los residentes son transversales: la escritura (nuevo/editar) queda
// en un bloque solo para ADMINISTRADOR y la lectura segun alcance en otro.
// El calendario de citas, el selector de bitacora, las alertas y los reportes
// son de lectura/generacion para los tres roles (el backend filtra el alcance);
// agendar citas y registrar signos vitales siguen limitados por rol.
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
          { path: '/residentes/:id/esquemas/nuevo', element: <FormularioEsquema /> },
          { path: '/residentes/:id/esquemas/:esquemaId/editar', element: <FormularioEsquema /> },
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
          { path: '/residentes/:id/tomas', element: <TomasResidente /> },
          { path: '/residentes/:id/signos-vitales', element: <BitacoraResidente /> },
        ],
      },
    ],
  },
  // Calendario de citas: lectura para los tres roles. Las acciones de agendar y
  // editar viven en su propio bloque, solo para ADMINISTRADOR.
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <MainLayout titulo="Citas" />,
        children: [{ path: '/calendario', element: <Calendario /> }],
      },
    ],
  },
  // Selector de bitacora: los tres roles eligen un residente de su alcance y
  // abren su bitacora.
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <MainLayout titulo="Bitacora" />,
        children: [{ path: '/bitacora', element: <SelectorBitacora /> }],
      },
    ],
  },
  // Alertas: lectura para los tres roles (atender se valida por rol en la UI).
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <MainLayout titulo="Alertas" />,
        children: [{ path: '/alertas', element: <ListadoAlertas /> }],
      },
    ],
  },
  // Reportes: generacion en PDF para los tres roles (alcance del backend).
  {
    element: <RutaProtegida />,
    children: [
      {
        element: <MainLayout titulo="Reportes" />,
        children: [{ path: '/reportes', element: <GeneradorReportes /> }],
      },
    ],
  },
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR']} />,
    children: [
      {
        element: <MainLayout titulo="Panel" />,
        children: [{ path: '/dashboard', element: <PanelAdministrador /> }],
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
  // Catalogo de medicamentos: lectura ADMINISTRADOR y CUIDADOR_ENFERMERO; la
  // escritura (nuevo/editar) queda restringida al ADMINISTRADOR mas abajo.
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR', 'CUIDADOR_ENFERMERO']} />,
    children: [
      {
        element: <MainLayout titulo="Medicamentos" />,
        children: [{ path: '/medicamentos', element: <ListadoMedicamentosCatalogo /> }],
      },
    ],
  },
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR']} />,
    children: [
      {
        element: <MainLayout titulo="Medicamentos" />,
        children: [
          { path: '/medicamentos/nuevo', element: <FormularioMedicamento /> },
          { path: '/medicamentos/:id/editar', element: <FormularioMedicamento /> },
        ],
      },
    ],
  },
  // Escritura de citas: agendar y editar, solo ADMINISTRADOR.
  {
    element: <RutaProtegida rolesPermitidos={['ADMINISTRADOR']} />,
    children: [
      {
        element: <MainLayout titulo="Citas" />,
        children: [
          { path: '/calendario/nuevo', element: <FormularioCita /> },
          { path: '/calendario/:citaId/editar', element: <FormularioCita /> },
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