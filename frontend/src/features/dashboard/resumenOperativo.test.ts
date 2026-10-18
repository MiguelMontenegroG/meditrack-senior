// Pruebas del calculo puro del resumen operativo del panel de administrador:
// agregacion de agendas, porcentaje de avance y serie del grafico. Son pruebas
// de funciones puras, sin React ni red.

import { describe, expect, it } from 'vitest'
import {
  RESUMEN_VACIO,
  acumularResumen,
  porcentajeAvance,
  serieTomasDeHoy,
} from '@/features/dashboard/resumenOperativo'
import type { AgendaEsquema, EstadoSlotToma } from '@/features/medicacion/types'

/** Construye una agenda con la cantidad de slots indicada por estado. */
function agendaConSlots(esquemaId: number, estados: EstadoSlotToma[]): AgendaEsquema {
  return {
    esquemaId,
    medicamentoId: 1,
    medicamentoNombre: 'Losartan',
    dosis: '50 mg',
    slots: estados.map((estado, indice) => ({
      fechaHoraProgramada: `2026-10-17T0${indice}:00:00Z`,
      estado,
    })),
  }
}

describe('resumenOperativo', () => {
  it('cuenta residentes, esquemas y tomas cerradas de una agenda', () => {
    const resumen = acumularResumen(RESUMEN_VACIO, [
      agendaConSlots(1, ['ADMINISTRADA', 'PENDIENTE', 'OMITIDA', 'ATRASADA']),
      agendaConSlots(2, ['ADMINISTRADA', 'ADMINISTRADA']),
    ])

    expect(resumen.residentes).toBe(1)
    expect(resumen.esquemas).toBe(2)
    expect(resumen.tomasTotales).toBe(6)
    expect(resumen.tomasCerradas).toBe(4)
  })

  it('acumula varios residentes sin mezclar sus agendas', () => {
    const uno = acumularResumen(RESUMEN_VACIO, [agendaConSlots(1, ['ADMINISTRADA'])])
    const dos = acumularResumen(uno, [agendaConSlots(2, ['PENDIENTE', 'ATRASADA'])])

    expect(dos.residentes).toBe(2)
    expect(dos.esquemas).toBe(2)
    expect(dos.tomasTotales).toBe(3)
    expect(dos.tomasCerradas).toBe(1)
  })

  it('informa 100% de avance cuando no hay tomas programadas', () => {
    expect(porcentajeAvance(RESUMEN_VACIO)).toBe(100)
  })

  it('calcula el avance redondeado a partir de las tomas cerradas', () => {
    expect(
      porcentajeAvance({ residentes: 1, esquemas: 1, tomasCerradas: 1, tomasTotales: 3 }),
    ).toBe(33)
    expect(
      porcentajeAvance({ residentes: 1, esquemas: 1, tomasCerradas: 3, tomasTotales: 3 }),
    ).toBe(100)
  })

  it('genera siete puntos y solo hoy tiene las tomas reales', () => {
    const serie = serieTomasDeHoy(12, new Date(2026, 9, 17))

    expect(serie).toHaveLength(7)
    expect(serie[serie.length - 1]).toEqual({ dia: 'Hoy', tomas: 12 })
    expect(serie.slice(0, 6).every((punto) => punto.tomas === 0)).toBe(true)
  })
})
