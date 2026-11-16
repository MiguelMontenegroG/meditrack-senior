// Tipos del contrato real del modulo de alertas. Espejo exacto de AlertaResponse
// del backend (AlertaController) y de los enumerados del dominio. La seccion de
// alertas NO esta documentada en docs/api/openapi.yaml; el contrato se tomo del
// controlador y del DTO del backend.

/** Tipo de alerta generada por el sistema. */
export type TipoAlerta =
  | 'DOSIS_ATRASADA'
  | 'SIGNO_FUERA_DE_RANGO'
  | 'CITA_PROXIMA'
  | 'BITACORA_INCOMPLETA'
  | 'OTRA'

/** Severidad de la alerta (define el tono visual). */
export type SeveridadAlerta = 'ALTA' | 'MEDIA' | 'INFO'

/** Estado de la alerta. REVISADA es el valor persistido de "atendida". */
export type EstadoAlerta = 'ACTIVA' | 'REVISADA' | 'DESCARTADA'

/** Origen de la alerta: las automaticas son SISTEMA. */
export type OrigenAlerta = 'SISTEMA' | 'USUARIO'

/** Vista de una alerta tal como la devuelve el backend. */
export interface Alerta {
  id: number
  pacienteId: number
  tipo: TipoAlerta
  severidad: SeveridadAlerta
  mensaje: string
  origen: OrigenAlerta
  signosId: number | null
  tomaId: number | null
  citaId: number | null
  estado: EstadoAlerta
  creadoEn: string
  revisadaEn: string | null
  revisadaPor: number | null
}

/** Pagina de alertas devuelta por el backend (PageResponse). */
export interface PaginaAlertas {
  contenido: Alerta[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del listado de alertas (se envian como query string). */
export interface FiltrosAlertas {
  estado?: EstadoAlerta
  tipo?: TipoAlerta
  pacienteId?: number
  pagina?: number
  tamano?: number
}
