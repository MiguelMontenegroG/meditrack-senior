import { Bell, CalendarDays, ClipboardList, FileBarChart, LayoutDashboard, MoreHorizontal, Settings2, ShieldCheck, Users } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { MarcaMediTrack } from '@/components/layout/MarcaMediTrack'

// Barra lateral de escritorio. Recibe el elemento activo y una funcion para
// seleccionarlo; no guarda estado propio (lo controla MainLayout).

interface ElementoNavegacion {
  etiqueta: string
  icono: LucideIcon
  contador?: number
}

const NAVEGACION: ElementoNavegacion[] = [
  { etiqueta: 'Panel', icono: LayoutDashboard },
  { etiqueta: 'Residentes', icono: Users },
  { etiqueta: 'Calendario', icono: CalendarDays },
  { etiqueta: 'Bitacora', icono: ClipboardList },
  { etiqueta: 'Alertas', icono: Bell, contador: 4 },
  { etiqueta: 'Reportes', icono: FileBarChart },
  { etiqueta: 'Usuarios', icono: ShieldCheck },
]

interface PropsBarraLateral {
  activo: string
  alSeleccionar: (etiqueta: string) => void
}

/** Navegacion principal en escritorio. */
export function BarraLateral({ activo, alSeleccionar }: PropsBarraLateral) {
  return (
    <aside className="sidebar">
      <MarcaMediTrack oscuro />
      <div className="center-name">
        <span>CENTRO DE VIDA</span>
        <strong>Sol de los Andes</strong>
        <small>Armenia, Quindio</small>
      </div>
      <nav aria-label="Navegacion principal">
        {NAVEGACION.map((item) => {
          const Icono = item.icono
          return (
            <button
              key={item.etiqueta}
              className={`nav-item ${activo === item.etiqueta ? 'active' : ''}`}
              onClick={() => alSeleccionar(item.etiqueta)}
            >
              <Icono size={19} />
              <span>{item.etiqueta}</span>
              {item.contador ? <b>{item.contador}</b> : null}
            </button>
          )
        })}
      </nav>
      <div className="sidebar-bottom">
        <button className="nav-item">
          <Settings2 size={19} />
          <span>Configuracion</span>
        </button>
        <div className="side-user">
          <span className="avatar avatar-gold">ML</span>
          <span>
            <strong>Mariana Lopez</strong>
            <small>Administradora</small>
          </span>
          <MoreHorizontal size={18} />
        </div>
      </div>
    </aside>
  )
}
