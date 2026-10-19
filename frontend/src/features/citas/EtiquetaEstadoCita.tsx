// Etiqueta del estado de una cita. Centraliza el tono y el texto en espanol
// para el calendario, las tarjetas y la tabla.

import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { ETIQUETA_ESTADO_CITA, TONO_ESTADO_CITA } from '@/features/citas/etiquetas'
import type { EstadoCita } from '@/features/citas/types'

/** Etiqueta visual del estado de una cita. */
export function EtiquetaEstadoCita({ estado }: { estado: EstadoCita }) {
  return <EtiquetaEstado tono={TONO_ESTADO_CITA[estado]}>{ETIQUETA_ESTADO_CITA[estado]}</EtiquetaEstado>
}