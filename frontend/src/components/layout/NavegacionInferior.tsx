import { CalendarDays, ClipboardList, LayoutDashboard, Pill, ShieldCheck, Users } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import type { RolUsuario } from '@/features/auth/types'

// Navegacion inferior para movil. Muestra las mismas secciones principales que
// la barra lateral segun el rol (hasta seis en administracion) y respeta el
// area tactil minima de 44px en cada boton.

interface ElementoNavegacion {
  etiqueta: string
  ruta: string
  icono: LucideIcon
}

const NAVEGACION_ADMIN: ElementoNavegacion[] = [
  { etiqueta: 'Panel', ruta: '/dashboard', icono: LayoutDashboard },
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Medicamentos', ruta: '/medicamentos', icono: Pill },
  { etiqueta: 'Calendario', ruta: '/calendario', icono: CalendarDays },
  { etiqueta: 'Bitacora', ruta: '/bitacora', icono: ClipboardList },
  { etiqueta: 'Usuarios', ruta: '/usuarios', icono: ShieldCheck },
]

const NAVEGACION_CUIDADOR: ElementoNavegacion[] = [
  { etiqueta: 'Hoy', ruta: '/cuidador', icono: LayoutDashboard },
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Medicamentos', ruta: '/medicamentos', icono: Pill },
  { etiqueta: 'Calendario', ruta: '/calendario', icono: CalendarDays },
  { etiqueta: 'Bitacora', ruta: '/bitacora', icono: ClipboardList },
]

const NAVEGACION_FAMILIAR: ElementoNavegacion[] = [
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Calendario', ruta: '/calendario', icono: CalendarDays },
  { etiqueta: 'Bitacora', ruta: '/bitacora', icono: ClipboardList },
]

const NAVEGACION_POR_ROL: Record<RolUsuario, ElementoNavegacion[]> = {
  ADMINISTRADOR: NAVEGACION_ADMIN,
  CUIDADOR_ENFERMERO: NAVEGACION_CUIDADOR,
  FAMILIAR_AUTORIZADO: NAVEGACION_FAMILIAR,
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
