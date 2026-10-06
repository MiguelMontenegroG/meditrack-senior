import type { AlertaActiva, IndicadorKpiDatos, PuntoCumplimiento, PuntoMiniGrafico, ResidenteAtencion } from '@/features/dashboard/types'

// DATOS DE EJEMPLO (pendientes de su endpoint).
// Estas cifras son ilustrativas para maquetar el panel de administrador; NO
// provienen del backend. Cuando exista su endpoint real, este archivo se
// reemplaza por una llamada a la capa de servicios.

export const datosCumplimiento: PuntoCumplimiento[] = [
  { dia: 'Lun', citas: 18, bitacoras: 35 },
  { dia: 'Mar', citas: 21, bitacoras: 39 },
  { dia: 'Mie', citas: 16, bitacoras: 37 },
  { dia: 'Jue', citas: 24, bitacoras: 41 },
  { dia: 'Vie', citas: 20, bitacoras: 38 },
  { dia: 'Sab', citas: 14, bitacoras: 34 },
  { dia: 'Hoy', citas: 22, bitacoras: 38 },
]

export const serieMiniGrafico: PuntoMiniGrafico[] = [
  { valor: 44 },
  { valor: 49 },
  { valor: 45 },
  { valor: 57 },
  { valor: 54 },
  { valor: 62 },
  { valor: 68 },
]

export const indicadoresKpi: IndicadorKpiDatos[] = [
  { etiqueta: 'Citas cumplidas', valor: '94%', detalle: 'de 86 citas', tendencia: '+6.2%', tono: 'primary' },
  { etiqueta: 'Bitacoras hoy', valor: '90,5%', detalle: '38 de 42 residentes', tendencia: '+3,1%', tono: 'green' },
  { etiqueta: 'Tiempo de alerta', valor: '8 min', detalle: 'promedio de atencion', tendencia: 'Ã¢Ë†â€™2 min', tono: 'warm' },
  { etiqueta: 'Reportes generados', valor: '27', detalle: 'en este periodo', tendencia: '+12,5%', tono: 'blue' },
]

export const alertasActivas: AlertaActiva[] = [
  { tipo: 'Dosis atrasada', residente: 'Rosa Elena Gutierrez', momento: 'Hace 24 min', severidad: 'Alta', tono: 'danger' },
  { tipo: 'Bitacora incompleta', residente: 'Luis Alberto Cardenas', momento: 'Desde las 10:00 a. m.', severidad: 'Media', tono: 'warning' },
  { tipo: 'Cita proxima en 24 h', residente: 'Beatriz Amparo Rios', momento: 'Manana, 8:30 a. m.', severidad: 'Info', tono: 'info' },
]

export const residentesAtencion: ResidenteAtencion[] = [
  { nombre: 'Rosa Elena Gutierrez', edad: '82 anos', habitacion: 'Hab. 204', situacion: 'Presion elevada', tono: 'danger', estado: 'Revision requerida' },
  { nombre: 'Luis Alberto Cardenas', edad: '76 anos', habitacion: 'Hab. 112', situacion: 'Bitacora pendiente', tono: 'warning', estado: 'Pendiente' },
  { nombre: 'Beatriz Amparo Rios', edad: '88 anos', habitacion: 'Hab. 308', situacion: 'Cita manana', tono: 'info', estado: 'Proxima' },
  { nombre: 'Hector Julio Molina', edad: '79 anos', habitacion: 'Hab. 105', situacion: 'Sin novedades', tono: 'success', estado: 'Sin novedades' },
]
