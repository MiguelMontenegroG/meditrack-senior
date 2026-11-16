// Utilidades de fecha y validacion del rango de reportes. El usuario elige dos
// fechas locales (Bogota, inputs type="date"); aqui se convierten a instantes
// UTC para el backend y se valida la regla del servidor: rango completo,
// ordenado y de maximo 366 dias. Funciones puras, faciles de probar.

import { aInstanteUtc, sumarDias } from '@/lib/fechas-bogota'

/** Dias maximos que admite un reporte (coincide con ReporteGenerado del backend). */
export const DIAS_MAXIMOS_REPORTE = 366

/** Resultado de validar el rango de fechas del formulario. */
export interface ResultadoRango {
  /** true si el rango es valido y se puede enviar. */
  valido: boolean
  /** Mensaje de error en espanol (solo si valido es false). */
  mensaje: string | null
  /** Instantes UTC del rango, solo si valido es true. */
  rangoUtc: { desde: string; hasta: string } | null
}

/**
 * Diferencia en dias (calendario de Bogota) entre dos fechas YYYY-MM-DD. Se
 * calcula con Date.UTC para no depender de la zona del navegador.
 */
export function diasEntre(desde: string, hasta: string): number {
  const a = Date.UTC(
    Number(desde.slice(0, 4)),
    Number(desde.slice(5, 7)) - 1,
    Number(desde.slice(8, 10)),
  )
  const b = Date.UTC(
    Number(hasta.slice(0, 4)),
    Number(hasta.slice(5, 7)) - 1,
    Number(hasta.slice(8, 10)),
  )
  return Math.round((b - a) / (24 * 60 * 60 * 1000))
}

/**
 * Valida el rango elegido y calcula los instantes UTC. `hasta` se envia como el
 * inicio del dia siguiente para que el rango incluya todo el ultimo dia (igual
 * que el calendario de citas).
 */
export function validarRangoReporte(desde: string, hasta: string): ResultadoRango {
  if (!desde || !hasta) {
    return {
      valido: false,
      mensaje: 'Indica ambas fechas del rango del reporte.',
      rangoUtc: null,
    }
  }
  const dias = diasEntre(desde, hasta)
  if (dias < 0) {
    return {
      valido: false,
      mensaje: 'La fecha "desde" no puede ser posterior a "hasta".',
      rangoUtc: null,
    }
  }
  if (dias > DIAS_MAXIMOS_REPORTE) {
    return {
      valido: false,
      mensaje: `El rango del reporte no puede superar los ${DIAS_MAXIMOS_REPORTE} dias.`,
      rangoUtc: null,
    }
  }
  const inicio = aInstanteUtc(desde, '00:00')
  const finExclusivo = aInstanteUtc(sumarDias(hasta, 1), '00:00')
  if (!inicio || !finExclusivo) {
    return {
      valido: false,
      mensaje: 'El rango de fechas no es valido.',
      rangoUtc: null,
    }
  }
  return { valido: true, mensaje: null, rangoUtc: { desde: inicio, hasta: finExclusivo } }
}
