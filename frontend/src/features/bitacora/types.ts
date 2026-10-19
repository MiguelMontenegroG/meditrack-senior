// Tipos del contrato del modulo de bitacora de signos vitales. Espejo exacto de
// los DTO de docs/api/openapi.yaml (SignosVitalesRequest, SignosVitalesDetalle y
// PaginaSignosVitales, tag "Bitacora de signos vitales").
//
// Los registros son INMUTABLES: no hay edicion ni borrado, por eso no se
// definen cuerpos de PUT/PATCH/DELETE.

/** Cuerpo para registrar signos vitales (POST). Todos los signos son opcionales
 *  por separado, pero al menos uno es obligatorio. El autor (registradoPor) sale
 *  del usuario autenticado, nunca del cuerpo. */
export interface DatosSignosVitales {
  /** Instante ISO-8601 UTC (sufijo Z). Si falta, el servidor usa su reloj. */
  fechaHora?: string | null
  /** Presion sistolica en mmHg (50-300); debe ser mayor que la diastolica. */
  presionSistolica?: number | null
  /** Presion diastolica en mmHg (30-200); debe ser menor que la sistolica. */
  presionDiastolica?: number | null
  /** Glucosa en mg/dL (20-800). */
  glucosaMgDl?: number | null
  /** Temperatura en grados C (30.0-45.0), con un decimal. */
  temperaturaC?: number | null
  observaciones?: string | null
}

/** Vista completa de un registro de signos vitales. Los signos no registrados
 *  vienen en null. */
export interface SignosVitales {
  id: number
  pacienteId: number
  registradoPor: number
  /** Instante ISO-8601 UTC del momento de la medicion. */
  fechaHora: string
  presionSistolica: number | null
  presionDiastolica: number | null
  glucosaMgDl: number | null
  temperaturaC: number | null
  observaciones: string | null
}

/** Pagina del historial de signos vitales (registro mas reciente primero). */
export interface PaginaSignosVitales {
  contenido: SignosVitales[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del historial. `desde`/`hasta` son opcionales y en ISO UTC. */
export interface FiltrosSignosVitales {
  desde?: string
  hasta?: string
  pagina?: number
  tamano?: number
}
