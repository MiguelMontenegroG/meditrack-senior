import { CheckCircle2, CircleAlert, Clock3, ArrowUpRight } from 'lucide-react'
import type { ReactNode } from 'react'

// Etiqueta de estado con icono y color segun el tono. Generaliza los
// indicadores de severidad usados en alertas, tabla de residentes y vitales.

export type TonoEstado = 'success' | 'warning' | 'danger' | 'info' | 'neutral'

const ICONOS: Record<TonoEstado, typeof Clock3> = {
  success: CheckCircle2,
  warning: Clock3,
  danger: CircleAlert,
  info: ArrowUpRight,
  neutral: Clock3,
}

interface PropsEtiquetaEstado {
  tono: TonoEstado
  children: ReactNode
}

/** Indicador visual de estado (p. ej. "Dosis atrasada"). */
export function EtiquetaEstado({ tono, children }: PropsEtiquetaEstado) {
  const Icono = ICONOS[tono]
  return (
    <span className={`status status-${tono}`}>
      <Icono size={14} />
      {children}
    </span>
  )
}
