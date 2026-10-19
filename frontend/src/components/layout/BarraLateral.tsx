import { Bell, CalendarDays, ClipboardList, FileBarChart, LayoutDashboard, MoreHorizontal, Pill, Settings2, ShieldCheck, Users } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import { MarcaMediTrack } from '@/components/layout/MarcaMediTrack'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import type { RolUsuario } from '@/features/auth/types'

// Barra lateral de escritorio. Muestra "Residentes" a los tres roles; las
// secciones de administracion (Panel, Calendario, etc.) solo al ADMINISTRADOR.

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
  { etiqueta: 'Alertas', ruta: '/alertas', icono: Bell },
  { etiqueta: 'Reportes', ruta: '/reportes', icono: FileBarChart },
  { etiqueta: 'Usuarios', ruta: '/usuarios', icono: ShieldCheck },
]

const NAVEGACION_CUIDADOR: ElementoNavegacion[] = [
  { etiqueta: 'Hoy', ruta: '/cuidador', icono: LayoutDashboard },
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Medicamentos', ruta: '/medicamentos', icono: Pill },
  { etiqueta: 'Calendario', ruta: '/calendario', icono: CalendarDays },
]

const NAVEGACION_FAMILIAR: ElementoNavegacion[] = [
  { etiqueta: 'Residentes', ruta: '/residentes', icono: Users },
  { etiqueta: 'Calendario', ruta: '/calendario', icono: CalendarDays },
]

const NAVEGACION_POR_ROL: Record<RolUsuario, ElementoNavegacion[]> = {
  ADMINISTRADOR: NAVEGACION_ADMIN,
  CUIDADOR_ENFERMERO: NAVEGACION_CUIDADOR,
  FAMILIAR_AUTORIZADO: NAVEGACION_FAMILIAR,
}

/** Etiqueta legible del rol para la tarjeta de usuario. */
const NOMBRE_ROL: Record<RolUsuario, string> = {
  ADMINISTRADOR: 'Administrador',
  CUIDADOR_ENFERMERO: 'Cuidador enfermero',
  FAMILIAR_AUTORIZADO: 'Familiar autorizado',
}

/** Iniciales para el avatar a partir del nombre completo. */
function iniciales(nombreCompleto: string | undefined): string {
  if (!nombreCompleto) return '--'
  return nombreCompleto
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((parte) => parte[0]?.toUpperCase() ?? '')
    .join('')
}

/** Navegacion principal en escritorio. */
export function BarraLateral() {
  const { usuario } = useAutenticacion()
  const elementos = usuario ? NAVEGACION_POR_ROL[usuario.rol] : []
  return (
    <aside className="sidebar">
      <MarcaMediTrack oscuro />
      <div className="center-name">
        <span>CENTRO DE VIDA</span>
        <strong>Sol de los Andes</strong>
        <small>Armenia, Quindio</small>
      </div>
      <nav aria-label="Navegacion principal">
        {elementos.map((item) => {
          const Icono = item.icono
          return (
            <NavLink
              key={item.ruta}
              to={item.ruta}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Icono size={19} />
              <span>{item.etiqueta}</span>
            </NavLink>
          )
        })}
      </nav>
      <div className="sidebar-bottom">
        <NavLink to="/login" className="nav-item">
          <Settings2 size={19} />
          <span>Configuracion</span>
        </NavLink>
        <div className="side-user">
          <span className="avatar avatar-gold">{iniciales(usuario?.nombreCompleto)}</span>
          <span>
            <strong>{usuario?.nombreCompleto ?? 'Sin sesion'}</strong>
            <small>{usuario ? NOMBRE_ROL[usuario.rol] : '--'}</small>
          </span>
          <MoreHorizontal size={18} />
        </div>
      </div>
    </aside>
  )
}
