// Types del panel de administrador. Todos los datos que muestra el panel salen
// de endpoints reales (residentes, medicamentos y agenda de tomas); lo que
// depende de modulos inexistentes se presenta como estado "en construccion".

/** Punto de la serie del grafico del panel (tomas programadas por dia). */
export interface PuntoCumplimiento {
  dia: string
  tomas: number
}

/** Indicador clave mostrado en la cuadricula superior. */
export interface IndicadorKpiDatos {
  etiqueta: string
  valor: string
  detalle: string
  /** Variacion respecto al periodo anterior; vacia si no hay serie real. */
  tendencia: string
  tono: 'primary' | 'warm' | 'blue' | 'green'
  /** Serie para la mini grafica; vacia = no se dibuja tendencia. */
  serie: number[]
}

/**
 * Datos reales con los que se calcula el resumen del panel. Provienen de
 * GET /pacientes (activos) y de la agenda de tomas del dia de cada residente.
 */
export interface DatosResumenOperativo {
  /** Residentes activos cuyo resumen se agrego. */
  residentes: number
  /** Total de esquemas de medicacion vigentes hoy. */
  esquemas: number
  /** Tomadas mas omitidas (registros ya diligenciados). */
  tomasCerradas: number
  /** Total de tomas programadas para hoy en los residentes activos. */
  tomasTotales: number
}
