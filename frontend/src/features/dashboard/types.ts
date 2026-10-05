import type { TonoEstado } from '@/components/ui/EtiquetaEstado'

// Types del panel de administrador. Los datos de ejemplo viven en mock-data.ts
// y se reemplazaran por datos del backend en el Paso 3.

/** Punto de la serie de cumplimiento diario (citas y bitacoras). */
export interface PuntoCumplimiento {
  dia: string
  citas: number
  bitacoras: number
}

/** Punto de la mini grafica de tendencia de un KPI. */
export interface PuntoMiniGrafico {
  valor: number
}

/** Indicador clave mostrado en la cuadricula superior. */
export interface IndicadorKpiDatos {
  etiqueta: string
  valor: string
  detalle: string
  tendencia: string
  tono: 'primary' | 'warm' | 'blue' | 'green'
}

/** Alerta activa que requiere seguimiento del equipo. */
export interface AlertaActiva {
  tipo: string
  residente: string
  momento: string
  severidad: string
  tono: TonoEstado
}

/** Fila de residente que requiere atencion hoy. */
export interface ResidenteAtencion {
  nombre: string
  edad: string
  habitacion: string
  situacion: string
  tono: TonoEstado
  estado: string
}
