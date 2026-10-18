import { LayoutDashboard, Pill, Users } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import type { RolUsuario } from '@/features/auth/types'

// Navegacion inferior para movil. Muestra solo las secciones principales y
// respeta el area tactil minima de 44px en cada boton.

interface ElementoNavegacion {
  etiqueta: string
  ruta: string
  icono: LucideIcon
}

const NAVEGACION_ADMIN: ElementoNavegacion[] = [
  { etiqueta: 'Panel', ruta: '/dashboard', icono: LayoutDashboard },
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Medicamentos', ruta: '/medicamentos', icono: Pill },
]

const NAVEGACION_CUIDADOR: ElementoNavegacion[] = [
  { etiqueta: 'Hoy', ruta: '/cuidador', icono: LayoutDashboard },
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Medicamentos', ruta: '/medicamentos', icono: Pill },
]

const NAVEGACION_POR_ROL: Record<RolUsuario, ElementoNavegacion[]> = {
  ADMINISTRADOR: NAVEGACION_ADMIN,
  CUIDADOR_ENFERMERO: NAVEGACION_CUIDADOR,
  FAMILIAR_AUTORIZADO: [{ etiqueta: 'Residentes', ruta: '/residentes', icono: Users }],
}

/** Barra de navegacion inferior (solo movil). */
export function NavegacionInferior() {
  const { usuario } = useAutenticacion()
  const elementos = usuario ? NAVEGACION_POR_ROL[usuario.rol] : []
  return (
    <nav className="bottom-nav">
      {elementos.map((item) => {
        const Icono = item.icono
        return (
          <NavLink
            key={item.ruta}
            to={item.ruta}
            className={({ isActive }) => (isActive ? 'active' : '')}
          >
            <Icono size={20} />
            <span>{item.etiqueta}</span>
          </NavLink>
        )
      })}
    </nav>
  )
}
