import { createContext } from 'react'
import type { ContextoAutenticacion } from '@/features/auth/types'

// Contexto de autenticacion (separado del provider y del hook para que cada
// archivo tenga una unica responsabilidad y el fast refresh funcione).
export const ContextoAuth = createContext<ContextoAutenticacion | null>(null)
