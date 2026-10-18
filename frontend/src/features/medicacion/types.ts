// Tipos del contrato del modulo medicacion. Espejo exacto de los DTO de
// docs/api/openapi.yaml (Medicamento, EsquemaMedicacion, RegistroToma,
// AgendaEsquema, SlotToma y sus paginas).

/** Via de administracion de un medicamento. */
export type ViaAdministracion = 'ORAL' | 'SUBCUTANEA' | 'INTRAMUSCULAR' | 'TOPICA' | 'OTRA'

/** Estado persistido de una toma registrada (no se persiste lo pendiente). */
export type EstadoToma = 'ADMINISTRADA' | 'OMITIDA'

/** Estado calculado de un slot de la agenda del dia. */
export type EstadoSlotToma = 'PENDIENTE' | 'ATRASADA' | 'ADMINISTRADA' | 'OMITIDA'

/** Medicamento del catalogo. */
export interface Medicamento {
  id: number
  nombre: string
  presentacion: string | null
  activo: boolean
}

/** Cuerpo para crear o actualizar un medicamento. */
export interface DatosMedicamento {
  nombre: string
  presentacion?: string | null
}

/** Pagina del catalogo de medicamentos. */
export interface PaginaMedicamentos {
  contenido: Medicamento[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del listado de medicamentos (query string). */
export interface FiltrosMedicamentos {
  texto?: string
  activo?: boolean
  pagina?: number
  tamano?: number
}

/** Esquema de medicacion de un residente, con sus horarios (HH:mm). */
export interface EsquemaMedicacion {
  id: number
  pacienteId: number
  medicamentoId: number
  medicamentoNombre: string | null
  dosis: string
  frecuenciaHoras: number
  via: ViaAdministracion
  indicaciones: string | null
  fechaInicio: string
  fechaFin: string | null
  activo: boolean
  vigente: boolean
  horarios: string[]
}

/** Cuerpo para crear o actualizar un esquema (incluye sus horarios). */
export interface DatosEsquema {
  medicamentoId: number
  dosis: string
  frecuenciaHoras: number
  via: ViaAdministracion
  indicaciones?: string | null
  fechaInicio: string
  fechaFin?: string | null
  horarios: string[]
}

/** Filtros del listado de esquemas de un residente. */
export interface FiltrosEsquemas {
  activo?: boolean
  vigente?: boolean
}

/** Toma registrada (inmutable: no hay editar ni borrar). */
export interface RegistroToma {
  id: number
  esquemaId: number
  registradoPor: number
  fechaHoraProgramada: string
  fechaHoraRegistro: string | null
  estado: EstadoToma
  observaciones: string | null
}

/** Cuerpo para registrar una toma. */
export interface DatosRegistroToma {
  fechaHoraProgramada: string
  estado: EstadoToma
  observaciones?: string | null
}

/** Pagina del historial de tomas. */
export interface PaginaTomas {
  contenido: RegistroToma[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del historial de tomas (query string). */
export interface FiltrosHistorialTomas {
  esquemaId?: number
  desde?: string
  hasta?: string
  pagina?: number
  tamano?: number
}

/** Slot de la agenda del dia para un esquema. */
export interface SlotToma {
  fechaHoraProgramada: string
  estado: EstadoSlotToma
}

/** Agenda de un esquema con el estado calculado de cada slot del dia. */
export interface AgendaEsquema {
  esquemaId: number
  medicamentoId: number
  medicamentoNombre: string | null
  dosis: string
  slots: SlotToma[]
}
