import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import ClinicalWorkspace from '@/ClinicalWorkspace'

// Estructura de rutas lista para la futura descomposicion por features.
// Por ahora el workspace clinico completo (login + panel) se monta en la raiz,
// conservando el diseno y el flujo originales.
const router = createBrowserRouter([
  {
    path: '/',
    element: <ClinicalWorkspace />,
  },
  {
    path: '/login',
    element: <ClinicalWorkspace />,
  },
  {
    path: '*',
    element: <Navigate to="/" replace />,
  },
])

export default function App() {
  return <RouterProvider router={router} />
}