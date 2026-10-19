// Pruebas de las utilidades de fecha del modulo de citas: la conversion entre
// hora local de Bogota (UTC-5) y el instante UTC, y la agrupacion por dia.

import { describe, expect, it } from 'vitest'
import {
  aInstanteUtc,
  agruparPorDia,
  fechaBogota,
  horaBogota,
  sumarDias,
  yaPaso,
} from '@/features/citas/fechas'

describe('fechas de citas', () => {
  it('convierte las 09:00 de Bogota al instante UTC con sufijo Z', () => {
    // 09:00 en Bogota (UTC-5) son las 14:00 UTC.
    expect(aInstanteUtc('2026-01-20', '09:00')).toBe('2026-01-20T14:00:00.000Z')
  })

  it('devuelve null si la fecha o la hora no son validas', () => {
    expect(aInstanteUtc('2026-13-01', '09:00')).toBeNull()
    expect(aInstanteUtc('2026-01-20', '25:00')).toBeNull()
    expect(aInstanteUtc('20/01/2026', '09:00')).toBeNull()
  })

  it('obtiene la hora y el dia de Bogota de un instante UTC', () => {
    // 02:00 UTC del 21 = 21:00 del 20 en Bogota (dia anterior).
    expect(fechaBogota('2026-01-21T02:00:00.000Z')).toBe('2026-01-20')
    expect(horaBogota('2026-01-21T02:00:00.000Z')).toBe('21:00')
  })

  it('suma dias cruzando el fin de mes', () => {
    expect(sumarDias('2026-01-31', 1)).toBe('2026-02-01')
    expect(sumarDias('2026-03-01', -1)).toBe('2026-02-28')
  })

  it('sabe si una fecha/hora de Bogota ya paso respecto a un instante dado', () => {
    const ahora = new Date('2026-01-20T15:00:00.000Z') // 10:00 Bogota
    expect(yaPaso('2026-01-20', '09:00', ahora)).toBe(true)
    expect(yaPaso('2026-01-20', '11:00', ahora)).toBe(false)
  })

  it('agrupa las citas por dia de Bogota en orden ascendente', () => {
    const citas = [
      { fechaHora: '2026-01-21T14:00:00.000Z' },
      { fechaHora: '2026-01-20T14:00:00.000Z' },
      { fechaHora: '2026-01-20T20:00:00.000Z' },
    ]
    const grupos = agruparPorDia(citas)
    expect(grupos.map((grupo) => grupo.fecha)).toEqual(['2026-01-20', '2026-01-21'])
    expect(grupos[0].citas).toHaveLength(2)
  })
})