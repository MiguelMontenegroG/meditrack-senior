import { useContext } from 'react'
import { ContextoAuth } from '@/features/auth/contextoAuth'
import type { ContextoAutenticacion } from '@/features/auth/types'

/** Hook para consumir el contexto de autenticacion. */
export function useAutenticacion(): ContextoAutenticacion {
  const contexto = useContext(ContextoAuth)
  if (!contexto) {
    throw new Error('useAutenticacion debe usarse dentro de ProveedorAutenticacion')
  }
  return contexto
}
