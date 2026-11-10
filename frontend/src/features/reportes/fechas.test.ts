// Pruebas de la validacion del rango de reportes: rango completo, ordenado y
// con maximo de 366 dias, y conversion de las fechas locales de Bogota a
// instantes UTC (el inicio del dia siguiente para incluir todo el ultimo dia).

import { describe, expect, it } from 'vitest'
import { DIAS_MAXIMOS_REPORTE, diasEntre, validarRangoReporte } from '@/features/reportes/fechas'

describe('diasEntre', () => {
  it('cuenta los dias de calendario entre dos fechas', () => {
    expect(diasEntre('2026-01-01', '2026-01-31')).toBe(30)
    expect(diasEntre('2026-01-01', '2026-01-01')).toBe(0)
  })
})

describe('validarRangoReporte', () => {
  it('rechaza un rango incompleto', () => {
    const resultado = validarRangoReporte('', '2026-01-01')
    expect(resultado.valido).toBe(false)
    expect(resultado.rangoUtc).toBeNull()
  })

  it('rechaza un rango invertido', () => {
    const resultado = validarRangoReporte('2026-02-01', '2026-01-01')
    expect(resultado.valido).toBe(false)
    expect(resultado.mensaje).toMatch(/posterior/)
  })

  it('rechaza un rango mayor al maximo permitido', () => {
    // Un dia mas que el maximo.
    const desde = '2024-01-01'
    const hasta = '2025-01-02' // 367 dias
    expect(diasEntre(desde, hasta)).toBeGreaterThan(DIAS_MAXIMOS_REPORTE)
    const resultado = validarRangoReporte(desde, hasta)
    expect(resultado.valido).toBe(false)
    expect(resultado.mensaje).toMatch(/366/)
  })

  it('acepta un rango valido y calcula el inicio UTC con hasta exclusivo', () => {
    const resultado = validarRangoReporte('2026-01-01', '2026-01-31')
    expect(resultado.valido).toBe(true)
    expect(resultado.rangoUtc).not.toBeNull()
    // Bogota es UTC-5: medianoche local del 1 de enero = 05:00 UTC.
    expect(resultado.rangoUtc?.desde).toBe('2026-01-01T05:00:00.000Z')
    // Hasta exclusivo: medianoche local del 1 de febrero = 05:00 UTC.
    expect(resultado.rangoUtc?.hasta).toBe('2026-02-01T05:00:00.000Z')
  })
})
