// Calculo puro del resumen operativo del panel de administrador a partir de
// datos reales del backend. No depende de React ni de la red: recibe las
// agendas del dia de cada residente activo y devuelve los totales y la serie
// del grafico. Al ser funciones puras son faciles de probar.

import type { AgendaEsquema } from '@/features/medicacion/types'
import type { DatosResumenOperativo, PuntoCumplimiento } from '@/features/dashboard/types'

/** Dias abreviados de la semana para el grafico (domingo a sabado). */
const DIAS_SEMANA = ['Dom', 'Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab']

/** Resumen operativo vacio (sin residentes ni agendas). */
export const RESUMEN_VACIO: DatosResumenOperativo = {
  residentes: 0,
  esquemas: 0,
  tomasCerradas: 0,
  tomasTotales: 0,
}

/**
 * Agrega el resumen de un residente. Cada esquema de su agenda cuenta una vez;
 * cada slot es una toma programada, cerrada cuando esta ADMINISTRADA u OMITIDA
 * y abierta cuando esta PENDIENTE o ATRASADA.
 */
export function acumularResumen(
  acumulado: DatosResumenOperativo,
  agenda: AgendaEsquema[],
): DatosResumenOperativo {
  const siguiente: DatosResumenOperativo = { ...acumulado, residentes: acumulado.residentes + 1 }
  for (const esquema of agenda) {
    siguiente.esquemas += 1
    siguiente.tomasTotales += esquema.slots.length
    siguiente.tomasCerradas += esquema.slots.filter(
      (slot) => slot.estado === 'ADMINISTRADA' || slot.estado === 'OMITIDA',
    ).length
  }
  return siguiente
}

/**
 * Porcentaje de avance de la medicacion del dia. Sin tomas programadas se
 * informa 100% (no hay nada pendiente), igual que la vista del cuidador.
 */
export function porcentajeAvance(datos: DatosResumenOperativo): number {
  if (datos.tomasTotales === 0) return 100
  return Math.round((datos.tomasCerradas / datos.tomasTotales) * 100)
}

/**
 * Serie del grafico de cumplimiento. El backend no expone un historico en
 * lote, asi que solo se grafica la medida real disponible: las tomas
 * programadas hoy. Los dias anteriores quedan en cero y el subtitulo del panel
 * lo advierte para no sugerir datos historicos que no existen.
 */
export function serieTomasDeHoy(tomasHoy: number, hoy = new Date()): PuntoCumplimiento[] {
  const serie: PuntoCumplimiento[] = []
  for (let atras = 6; atras >= 0; atras -= 1) {
    const fecha = new Date(hoy)
    fecha.setDate(fecha.getDate() - atras)
    const esHoy = atras === 0
    serie.push({
      dia: esHoy ? 'Hoy' : DIAS_SEMANA[fecha.getDay()],
      tomas: esHoy ? tomasHoy : 0,
    })
  }
  return serie
}
