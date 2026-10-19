// Logica pura de la bitacora de signos vitales: conversiones de texto a numero
// (acepta coma o punto decimal), validacion de los rangos de plausibilidad del
// contrato y formateo para mostrar. No depende de React, por lo que es facil
// de probar y se reutiliza entre el formulario y el historial.
//
// Los rangos replican los CHECK de la tabla registro_signos_vitales y las
// reglas de docs/api/openapi.yaml: se validan en el cliente para dar un mensaje
// inmediato; el backend sigue siendo la fuente de verdad (400 se muestran en
// espanol mediante lib/mensajes-error).

import type { SignosVitales } from '@/features/bitacora/types'

/** Limites de plausibilidad del contrato (no clinicos: solo atrapan errores). */
export const RANGOS = {
  sistolica: { min: 50, max: 300, unidad: 'mmHg' },
  diastolica: { min: 30, max: 200, unidad: 'mmHg' },
  glucosa: { min: 20, max: 800, unidad: 'mg/dL' },
  temperatura: { min: 30, max: 45, unidad: 'grados C' },
} as const

/** Valores del formulario, tal como se escriben (texto acepta coma o punto). */
export interface ValoresSignosVitales {
  fecha: string
  hora: string
  sistolica: string
  diastolica: string
  glucosa: string
  temperatura: string
  observaciones: string
}

/** Errores por campo del formulario. */
export type ErroresSignosVitales = Partial<Record<keyof ValoresSignosVitales, string>>

/**
 * Convierte un texto con coma o punto decimal a numero. Devuelve null si no es
 * un numero finito (vacio, con letras, etc.).
 */
export function aNumero(valor: string): number | null {
  const limpio = valor.trim().replace(',', '.')
  if (!limpio) return null
  if (!/^-?\d+(\.\d+)?$/.test(limpio)) return null
  const numero = Number(limpio)
  return Number.isFinite(numero) ? numero : null
}

/**
 * Indica si un texto es un entero valido (sin decimales). Se usa en la presion,
 * que en el contrato es integer.
 */
export function aEntero(valor: string): number | null {
  const limpio = valor.trim()
  if (!/^\d+$/.test(limpio)) return null
  return Number(limpio)
}

/** Texto de rango para los mensajes: "debe estar entre 50 y 300 mmHg". */
function textoRango(rango: { min: number; max: number; unidad: string }): string {
  return `debe estar entre ${rango.min} y ${rango.max} ${rango.unidad}`
}

/**
 * Valida los valores del formulario y devuelve los errores por campo. Un campo
 * vacio es valido (se puede registrar un subconjunto), pero se exige al menos
 * un signo; ese error se informa en la clave `sistolica` solo si los cuatro
 * signos estan vacios y no hay otro error, para no duplicar el aviso.
 */
export function validarSignosVitales(valores: ValoresSignosVitales): ErroresSignosVitales {
  const errores: ErroresSignosVitales = {}

  const sistolica = valores.sistolica.trim() ? aEntero(valores.sistolica) : null
  const diastolica = valores.diastolica.trim() ? aEntero(valores.diastolica) : null
  const glucosa = valores.glucosa.trim() ? aEntero(valores.glucosa) : null
  const temperatura = valores.temperatura.trim() ? aNumero(valores.temperatura) : null

  if (valores.sistolica.trim() && sistolica === null) {
    errores.sistolica = 'La presion sistolica debe ser un numero entero.'
  } else if (
    sistolica !== null &&
    (sistolica < RANGOS.sistolica.min || sistolica > RANGOS.sistolica.max)
  ) {
    errores.sistolica = `La presion sistolica ${textoRango(RANGOS.sistolica)}.`
  }

  if (valores.diastolica.trim() && diastolica === null) {
    errores.diastolica = 'La presion diastolica debe ser un numero entero.'
  } else if (
    diastolica !== null &&
    (diastolica < RANGOS.diastolica.min || diastolica > RANGOS.diastolica.max)
  ) {
    errores.diastolica = `La presion diastolica ${textoRango(RANGOS.diastolica)}.`
  } else if (sistolica !== null && diastolica !== null && diastolica >= sistolica) {
    errores.diastolica = 'La presion diastolica debe ser menor que la sistolica.'
  }

  if (valores.glucosa.trim() && glucosa === null) {
    errores.glucosa = 'La glucosa debe ser un numero entero.'
  } else if (
    glucosa !== null &&
    (glucosa < RANGOS.glucosa.min || glucosa > RANGOS.glucosa.max)
  ) {
    errores.glucosa = `La glucosa ${textoRango(RANGOS.glucosa)}.`
  }

  if (valores.temperatura.trim() && temperatura === null) {
    errores.temperatura = 'La temperatura debe ser un numero (por ejemplo 36,5).'
  } else if (
    temperatura !== null &&
    (temperatura < RANGOS.temperatura.min || temperatura > RANGOS.temperatura.max)
  ) {
    errores.temperatura = `La temperatura ${textoRango(RANGOS.temperatura)}.`
  }

  if (valores.observaciones.length > 500) {
    errores.observaciones = 'Las observaciones no pueden superar 500 caracteres.'
  }

  if (!valores.fecha) {
    errores.fecha = 'La fecha es obligatoria.'
  }
  if (!valores.hora) {
    errores.hora = 'La hora es obligatoria.'
  }

  const algunSigno =
    valores.sistolica.trim() ||
    valores.diastolica.trim() ||
    valores.glucosa.trim() ||
    valores.temperatura.trim()
  if (!algunSigno && Object.keys(errores).length === 0) {
    errores.sistolica = 'Registra al menos un signo vital: presion, glucosa o temperatura.'
  }

  return errores
}

/** Texto corto de la presion ("120/80"); null si no hay ningun valor. */
export function textoPresion(registro: SignosVitales): string | null {
  const { presionSistolica, presionDiastolica } = registro
  if (presionSistolica === null && presionDiastolica === null) return null
  return `${presionSistolica ?? '—'}/${presionDiastolica ?? '—'} mmHg`
}

/** Texto de la glucosa ("102 mg/dL"); null si no se registro. */
export function textoGlucosa(registro: SignosVitales): string | null {
  return registro.glucosaMgDl === null ? null : `${registro.glucosaMgDl} mg/dL`
}

/** Texto de la temperatura con un decimal ("36,5 C"); null si no se registro. */
export function textoTemperatura(registro: SignosVitales): string | null {
  if (registro.temperaturaC === null) return null
  return `${registro.temperaturaC.toFixed(1).replace('.', ',')} C`
}
