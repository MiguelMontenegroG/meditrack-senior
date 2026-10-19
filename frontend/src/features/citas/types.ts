// Tipos del contrato del modulo de citas. Espejo exacto de los DTO de
// docs/api/openapi.yaml (EstadoCita, DestinoEstadoCita, Especialidad,
// CitaRequest, CambiarEstadoCitaRequest, CitaResumen, CitaDetalle y
// PaginaCitas).

/** Estados persistidos de una cita. REPROGRAMADA existe pero no se usa. */
export type EstadoCita = 'PROGRAMADA' | 'CUMPLIDA' | 'INCUMPLIDA' | 'CANCELADA' | 'REPROGRAMADA'

/** Destinos validos de PATCH .../estado (REPROGRAMADA no se ofrece). */
export type DestinoEstadoCita = 'CUMPLIDA' | 'INCUMPLIDA' | 'CANCELADA'

/** Especialidad del catalogo (solo lectura). */
export interface Especialidad {
  id: number
  nombre: string
}

/** Campos comunes de una cita (sin motivo). */
export interface CitaResumen {
  id: number
  pacienteId: number
  especialidadId: number | null
  especialidadNombre: string | null
  registradaPor: number
  profesional: string | null
  /** Instante ISO-8601 UTC (sufijo Z) tal como lo devuelve el backend. */
  fechaHora: string
  lugar: string | null
  estado: EstadoCita
}

/** Detalle de una cita: agrega el motivo (presente tambien en el calendario). */
export interface CitaDetalle extends CitaResumen {
  motivo: string | null
}

/** Cuerpo para agendar o editar una cita (POST/PUT). */
export interface DatosCita {
  especialidadId?: number | null
  profesional?: string | null
  /** Instante ISO-8601 en UTC (Z) derivado de la hora local de Bogota. */
  fechaHora: string
  lugar?: string | null
  motivo?: string | null
}

/** Pagina del calendario de citas. */
export interface PaginaCitas {
  contenido: CitaDetalle[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del calendario (query string). desde/hasta son obligatorios. */
export interface FiltrosCitas {
  desde: string
  hasta: string
  estado?: EstadoCita
  pacienteId?: number
  pagina?: number
  tamano?: number
}