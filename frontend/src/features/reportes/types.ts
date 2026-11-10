// Tipos del contrato real del modulo de reportes. Espejo de ReporteResponse y
// TipoReporte del backend (ReporteController) y del esquema PaginaReportes de
// docs/api/openapi.yaml. El PDF no se almacena: se entrega en la descarga.

/** Tipo de reporte clinico generable. */
export type TipoReporte = 'HISTORIA_CLINICA' | 'BITACORA' | 'MEDICACION' | 'GENERAL'

/** Metadatos de un reporte generado (sin el PDF). */
export interface ReporteGeneradoResumen {
  id: number
  pacienteId: number
  generadoPor: number
  tipo: TipoReporte
  rangoDesde: string
  rangoHasta: string
  creadoEn: string
}

/** Pagina del historial de reportes generados. */
export interface PaginaReportes {
  contenido: ReporteGeneradoResumen[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros de paginacion para el historial de reportes. */
export interface FiltrosReportes {
  pagina?: number
  tamano?: number
}
