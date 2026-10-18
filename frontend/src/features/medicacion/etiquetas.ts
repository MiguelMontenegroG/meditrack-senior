// Etiquetas de dominio de medicacion en espanol: vias de administracion y
// estados de toma. Se usan en tablas, tarjetas, agenda e historial.

import type { ViaAdministracion } from '@/features/medicacion/types'

/** Etiqueta legible de cada via de administracion. */
export const ETIQUETA_VIA: Record<ViaAdministracion, string> = {
  ORAL: 'Oral',
  SUBCUTANEA: 'Subcutánea',
  INTRAMUSCULAR: 'Intramuscular',
  TOPICA: 'Tópica',
  OTRA: 'Otra',
}

/** Opciones de via para los desplegables (orden de presentacion). */
export const OPCIONES_VIA: { valor: ViaAdministracion; etiqueta: string }[] = (
  Object.keys(ETIQUETA_VIA) as ViaAdministracion[]
).map((valor) => ({ valor, etiqueta: ETIQUETA_VIA[valor] }))
