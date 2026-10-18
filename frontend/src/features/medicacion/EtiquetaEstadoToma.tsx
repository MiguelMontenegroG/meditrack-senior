// Etiqueta de estado de una toma (calculado o persistido). Centraliza el tono
// y el texto en espanol para la agenda y el historial.

import { EtiquetaEstado, type TonoEstado } from '@/components/ui/EtiquetaEstado'
import type { EstadoSlotToma, EstadoToma } from '@/features/medicacion/types'

const TONO_SLOT: Record<EstadoSlotToma, TonoEstado> = {
  PENDIENTE: 'info',
  ATRASADA: 'warning',
  ADMINISTRADA: 'success',
  OMITIDA: 'neutral',
}

const TEXTO_SLOT: Record<EstadoSlotToma, string> = {
  PENDIENTE: 'Pendiente',
  ATRASADA: 'Atrasada',
  ADMINISTRADA: 'Administrada',
  OMITIDA: 'Omitida',
}

const TONO_TOMA: Record<EstadoToma, TonoEstado> = {
  ADMINISTRADA: 'success',
  OMITIDA: 'neutral',
}

const TEXTO_TOMA: Record<EstadoToma, string> = {
  ADMINISTRADA: 'Administrada',
  OMITIDA: 'Omitida',
}

/** Etiqueta del estado calculado de un slot de la agenda. */
export function EtiquetaEstadoSlot({ estado }: { estado: EstadoSlotToma }) {
  return <EtiquetaEstado tono={TONO_SLOT[estado]}>{TEXTO_SLOT[estado]}</EtiquetaEstado>
}

/** Etiqueta del estado persistido de una toma del historial. */
export function EtiquetaEstadoToma({ estado }: { estado: EstadoToma }) {
  return <EtiquetaEstado tono={TONO_TOMA[estado]}>{TEXTO_TOMA[estado]}</EtiquetaEstado>
}
