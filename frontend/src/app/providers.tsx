import type { ReactNode } from 'react'
import { ProveedorAutenticacion } from '@/features/auth/AuthContext'

// Providers de la aplicacion. Hoy solo monta el contexto de autenticacion
// simulado; el Paso 3 sustituye su implementacion sin tocar el router.
export function ProveedoresApp({ children }: { children: ReactNode }) {
  return <ProveedorAutenticacion>{children}</ProveedorAutenticacion>
}
