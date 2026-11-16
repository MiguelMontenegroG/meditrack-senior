// Etiquetas de dominio de las alertas en espanol: tipo, estado y severidad en
// texto y tono visual. Se usan en las tarjetas, la tabla y los filtros.

import type { TonoEstado } from '@/components/ui/EtiquetaEstado'
import type { EstadoAlerta, SeveridadAlerta, TipoAlerta } from '@/features/alertas/types'

/** Texto legible de cada tipo de alerta. */
export const ETIQUETA_TIPO_ALERTA: Record<TipoAlerta, string> = {
  DOSIS_ATRASADA: 'Dosis atrasada',
  SIGNO_FUERA_DE_RANGO: 'Signo fuera de rango',
  CITA_PROXIMA: 'Cita vencida',
  BITACORA_INCOMPLETA: 'Bitacora incompleta',
  OTRA: 'Otra',
}

/** Texto legible de cada estado. REVISADA se muestra como "Atendida". */
export const ETIQUETA_ESTADO_ALERTA: Record<EstadoAlerta, string> = {
  ACTIVA: 'Activa',
  REVISADA: 'Atendida',
  DESCARTADA: 'Descartada',
}

/** Tono visual de cada estado. */
export const TONO_ESTADO_ALERTA: Record<EstadoAlerta, TonoEstado> = {
  ACTIVA: 'danger',
  REVISADA: 'success',
  DESCARTADA: 'neutral',
}

/** Tono visual de cada severidad (usado como indicador en la etiqueta). */
export const TONO_SEVERIDAD: Record<SeveridadAlerta, TonoEstado> = {
  ALTA: 'danger',
  MEDIA: 'warning',
  INFO: 'info',
}

/** Texto legible de la severidad. */
export const ETIQUETA_SEVERIDAD: Record<SeveridadAlerta, string> = {
  ALTA: 'Alta',
  MEDIA: 'Media',
  INFO: 'Informativa',
}

/**
 * Estados ofrecidos en el filtro. Se ofrecen los tres del esquema; el flujo
 * normal produce ACTIVA y REVISADA.
 */
export const ESTADOS_ALERTA_FILTRABLES: { valor: EstadoAlerta; etiqueta: string }[] = [
  { valor: 'ACTIVA', etiqueta: ETIQUETA_ESTADO_ALERTA.ACTIVA },
  { valor: 'REVISADA', etiqueta: ETIQUETA_ESTADO_ALERTA.REVISADA },
  { valor: 'DESCARTADA', etiqueta: ETIQUETA_ESTADO_ALERTA.DESCARTADA },
]

/** Tipos ofrecidos en el filtro (el catalogo completo del esquema). */
export const TIPOS_ALERTA_FILTRABLES: { valor: TipoAlerta; etiqueta: string }[] = (
  Object.keys(ETIQUETA_TIPO_ALERTA) as TipoAlerta[]
).map((valor) => ({ valor, etiqueta: ETIQUETA_TIPO_ALERTA[valor] }))
