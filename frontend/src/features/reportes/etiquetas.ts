// Etiquetas y descripciones de los tipos de reporte en espanol, y las secciones
// que incluye cada uno (coincide con TipoReporte del backend). Se usan en el
// selector del generador y en el historial.

import type { TipoReporte } from '@/features/reportes/types'

/** Texto legible de cada tipo de reporte. */
export const ETIQUETA_TIPO_REPORTE: Record<TipoReporte, string> = {
  HISTORIA_CLINICA: 'Historia clinica',
  BITACORA: 'Bitacora de signos vitales',
  MEDICACION: 'Medicacion',
  GENERAL: 'General',
}

/** Descripcion breve del contenido de cada tipo de reporte. */
export const DESCRIPCION_TIPO_REPORTE: Record<TipoReporte, string> = {
  HISTORIA_CLINICA:
    'Cabecera del residente, esquemas de medicacion, citas y bitacora de signos vitales.',
  BITACORA: 'Cabecera del residente, bitacora de signos vitales y citas.',
  MEDICACION: 'Cabecera del residente y esquemas de medicacion.',
  GENERAL: 'Cabecera y resumen de citas y alertas del periodo.',
}

/** Opciones del selector de tipo de reporte (orden del producto). */
export const TIPOS_REPORTE: { valor: TipoReporte; etiqueta: string }[] = [
  { valor: 'HISTORIA_CLINICA', etiqueta: ETIQUETA_TIPO_REPORTE.HISTORIA_CLINICA },
  { valor: 'BITACORA', etiqueta: ETIQUETA_TIPO_REPORTE.BITACORA },
  { valor: 'MEDICACION', etiqueta: ETIQUETA_TIPO_REPORTE.MEDICACION },
  { valor: 'GENERAL', etiqueta: ETIQUETA_TIPO_REPORTE.GENERAL },
]

/**
 * Advierte que la descarga del PDF puede tardar si hay muchos datos en el
 * periodo: los reportes se generan en el momento, no se almacenan.
 */
export const AVISO_GENERACION =
  'El PDF se genera en el momento a partir de los datos del periodo; puede tardar unos segundos.'
