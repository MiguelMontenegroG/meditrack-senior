import { CalendarDays, ClipboardList, LayoutDashboard, Users } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'

// Navegacion inferior para movil. Muestra solo las secciones principales
// y respeta el area tactil minima de 44px en cada boton.

interface ElementoNavegacion {
  etiqueta: string
  icono: LucideIcon
}

const NAVEGACION: ElementoNavegacion[] = [
  { etiqueta: 'Hoy', icono: LayoutDashboard },
  { etiqueta: 'Residentes', icono: Users },
  { etiqueta: 'Bitacora', icono: ClipboardList },
  { etiqueta: 'Calendario', icono: CalendarDays },
]

interface PropsNavegacionInferior {
  activo: string
  alSeleccionar: (etiqueta: string) => void
}

/** Barra de navegacion inferior (solo movil). */
export function NavegacionInferior({ activo, alSeleccionar }: PropsNavegacionInferior) {
  return (
    <nav className="bottom-nav">
      {NAVEGACION.map((item) => {
        const Icono = item.icono
        return (
          <button
            key={item.etiqueta}
            className={activo === item.etiqueta ? 'active' : ''}
            onClick={() => alSeleccionar(item.etiqueta)}
          >
            <Icono size={20} />
            <span>{item.etiqueta}</span>
          </button>
        )
      })}
    </nav>
  )
}
