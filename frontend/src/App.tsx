import { RouterProvider } from 'react-router-dom'
import { ProveedoresApp } from '@/app/providers'
import { router } from '@/app/router'
export default function App() {
  return (
    <ProveedoresApp>
      <RouterProvider router={router} />
    </ProveedoresApp>
  )
}