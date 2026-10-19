// Pruebas de la logica pura de signos vitales: conversion de numeros con coma
// decimal, validacion de rangos y formateo de los valores para mostrar.

import { describe, expect, it } from 'vitest'
import {
  aEntero,
  aNumero,
  textoGlucosa,
  textoPresion,
  textoTemperatura,
  validarSignosVitales,
  type ValoresSignosVitales,
} from '@/features/bitacora/signosVitales'
import type { SignosVitales } from '@/features/bitacora/types'

/** Valores base validos; cada prueba cambia solo lo que examina. */
function valores(parcial: Partial<ValoresSignosVitales> = {}): ValoresSignosVitales {
  return {
    fecha: '2026-01-20',
    hora: '09:00',
    sistolica: '',
    diastolica: '',
    glucosa: '',
    temperatura: '',
    observaciones: '',
    ...parcial,
  }
}

function registro(parcial: Partial<SignosVitales> = {}): SignosVitales {
  return {
    id: 1,
    pacienteId: 7,
    registradoPor: 2,
    fechaHora: '2026-01-20T14:00:00.000Z',
    presionSistolica: null,
    presionDiastolica: null,
    glucosaMgDl: null,
    temperaturaC: null,
    observaciones: null,
    ...parcial,
  }
}

describe('conversion de numeros', () => {
  it('acepta punto o coma decimal y rechaza textos', () => {
    expect(aNumero('36,5')).toBe(36.5)
    expect(aNumero('36.5')).toBe(36.5)
    expect(aNumero('  ')).toBeNull()
    expect(aNumero('abc')).toBeNull()
    expect(aNumero('3,6,5')).toBeNull()
  })

  it('exige enteros en la presion', () => {
    expect(aEntero('120')).toBe(120)
    expect(aEntero('120.5')).toBeNull()
    expect(aEntero('')).toBeNull()
  })
})

describe('validarSignosVitales', () => {
  it('exige al menos un signo cuando todos estan vacios', () => {
    const errores = validarSignosVitales(valores())
    expect(errores.sistolica).toMatch(/al menos un signo vital/i)
  })

  it('acepta un subconjunto valido de signos', () => {
    expect(validarSignosVitales(valores({ temperatura: '36,5' }))).toEqual({})
  })

  it('rechaza valores fuera de los rangos de plausibilidad', () => {
    expect(validarSignosVitales(valores({ sistolica: '10' })).sistolica).toMatch(/50 y 300/)
    expect(validarSignosVitales(valores({ glucosa: '1000' })).glucosa).toMatch(/20 y 800/)
    expect(validarSignosVitales(valores({ temperatura: '50' })).temperatura).toMatch(/30 y 45/)
  })

  it('exige que la diastolica sea menor que la sistolica', () => {
    const errores = validarSignosVitales(valores({ sistolica: '120', diastolica: '120' }))
    expect(errores.diastolica).toMatch(/menor que la sistolica/i)
  })

  it('exige fecha y hora', () => {
    const errores = validarSignosVitales(valores({ fecha: '', hora: '', temperatura: '36' }))
    expect(errores.fecha).toBeDefined()
    expect(errores.hora).toBeDefined()
  })

  it('limita las observaciones a 500 caracteres', () => {
    const errores = validarSignosVitales(
      valores({ temperatura: '36', observaciones: 'x'.repeat(501) }),
    )
    expect(errores.observaciones).toMatch(/500/)
  })
})

describe('formateo de signos', () => {
  it('muestra la presion completa o con guion en el extremo faltante', () => {
    expect(textoPresion(registro({ presionSistolica: 120, presionDiastolica: 80 }))).toBe(
      '120/80 mmHg',
    )
    expect(textoPresion(registro({ presionSistolica: 120 }))).toBe('120/— mmHg')
    expect(textoPresion(registro())).toBeNull()
  })

  it('formatea glucosa y temperatura con coma decimal', () => {
    expect(textoGlucosa(registro({ glucosaMgDl: 102 }))).toBe('102 mg/dL')
    expect(textoGlucosa(registro())).toBeNull()
    expect(textoTemperatura(registro({ temperaturaC: 36.5 }))).toBe('36,5 C')
    expect(textoTemperatura(registro())).toBeNull()
  })
})
