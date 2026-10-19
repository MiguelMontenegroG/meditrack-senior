// Etiquetas de dominio de las citas en espanol: estados (texto y tono) y
// opciones de filtro. Se usan en el calendario, tablas y tarjetas.

import type { TonoEstado } from '@/components/ui/EtiquetaEstado'
import type { EstadoCita } from '@/features/citas/types'

/** Texto legible de cada estado de cita. */
export const ETIQUETA_ESTADO_CITA: Record<EstadoCita, string> = {
  PROGRAMADA: 'Programada',
  CUMPLIDA: 'Cumplida',
  INCUMPLIDA: 'Incumplida',
  CANCELADA: 'Cancelada',
  REPROGRAMADA: 'Reprogramada',
}

/** Tono visual de cada estado de cita. */
export const TONO_ESTADO_CITA: Record<EstadoCita, TonoEstado> = {
  PROGRAMADA: 'info',
  CUMPLIDA: 'success',
  INCUMPLIDA: 'warning',
  CANCELADA: 'neutral',
  REPROGRAMADA: 'neutral',
}

/**
 * Estados que se ofrecen en los filtros y en las acciones. REPROGRAMADA no se
 * ofrece (no es una operacion del producto) aunque exista en el esquema.
 */
export const ESTADOS_FILTRABLES: { valor: EstadoCita; etiqueta: string }[] = [
  { valor: 'PROGRAMADA', etiqueta: ETIQUETA_ESTADO_CITA.PROGRAMADA },
  { valor: 'CUMPLIDA', etiqueta: ETIQUETA_ESTADO_CITA.CUMPLIDA },
  { valor: 'INCUMPLIDA', etiqueta: ETIQUETA_ESTADO_CITA.INCUMPLIDA },
  { valor: 'CANCELADA', etiqueta: ETIQUETA_ESTADO_CITA.CANCELADA },
]