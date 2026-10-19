// Utilidades puras de fecha/hora en America/Bogota, compartidas por los modulos
// de citas y de bitacora. No dependen de React ni del backend, por lo que son
// faciles de probar.
//
// America/Bogota es UTC-05:00 todo el ano (no aplica horario de verano), por lo
// que la conversion es un desplazamiento fijo de 5 horas. Se evita depender de
// la zona del navegador: las fechas clinicas siempre se interpretan y se
// muestran en hora de Colombia.

/** Desplazamiento fijo de America/Bogota respecto a UTC, en minutos. */
const OFFSET_BOGOTA_MINUTOS = -5 * 60

/**
 * Convierte una fecha (YYYY-MM-DD) y hora (HH:mm) locales de Bogota al instante
 * ISO-8601 en UTC (sufijo Z) que espera el backend. Devuelve null si el formato
 * es invalido.
 */
export function aInstanteUtc(fecha: string, hora: string): string | null {
  const partesFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(fecha)
  const partesHora = /^(\d{2}):(\d{2})$/.exec(hora)
  if (!partesFecha || !partesHora) {
    return null
  }
  const anio = Number(partesFecha[1])
  const mes = Number(partesFecha[2])
  const dia = Number(partesFecha[3])
  const h = Number(partesHora[1])
  const m = Number(partesHora[2])
  if (
    mes < 1 || mes > 12 || dia < 1 || dia > 31 || h > 23 || m > 59
  ) {
    return null
  }
  // Se construye la hora de Bogota como si fuera UTC y luego se resta el
  // desplazamiento: fecha/hora de Bogota + 5h = instante UTC real.
  const comoUtc = Date.UTC(anio, mes - 1, dia, h, m, 0, 0)
  const instante = comoUtc - OFFSET_BOGOTA_MINUTOS * 60 * 1000
  return new Date(instante).toISOString()
}

/**
 * Fecha de calendario (YYYY-MM-DD) en la que cae un instante UTC, expresada en
 * hora de Bogota. Se usa para agrupar citas y registros por dia.
 */
export function fechaBogota(iso: string): string {
  const fecha = new Date(new Date(iso).getTime() + OFFSET_BOGOTA_MINUTOS * 60 * 1000)
  const anio = fecha.getUTCFullYear()
  const mes = String(fecha.getUTCMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getUTCDate()).padStart(2, '0')
  return `${anio}-${mes}-${dia}`
}

/** Hora local de Bogota (HH:mm) de un instante UTC. */
export function horaBogota(iso: string): string {
  const fecha = new Date(new Date(iso).getTime() + OFFSET_BOGOTA_MINUTOS * 60 * 1000)
  const hora = String(fecha.getUTCHours()).padStart(2, '0')
  const minuto = String(fecha.getUTCMinutes()).padStart(2, '0')
  return `${hora}:${minuto}`
}

/** Fecha y hora local de Bogota legible (dd/mm/aaaa HH:mm). */
export function fechaHoraBogota(iso: string): string {
  const fecha = fechaBogota(iso).split('-')
  return `${fecha[2]}/${fecha[1]}/${fecha[0]} ${horaBogota(iso)}`
}

/**
 * Encabezado de dia en espanol a partir de una fecha YYYY-MM-DD, por ejemplo
 * "viernes, 17 de octubre de 2026". Se calcula a mediodia para evitar saltos por
 * zona horaria del navegador al formatear.
 */
export function diaEnEspanol(fecha: string): string {
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const referencia = new Date(Date.UTC(anio, (mes ?? 1) - 1, dia ?? 1, 12, 0, 0))
  return referencia.toLocaleDateString('es-CO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  })
}

/** Fecha de hoy (Bogota) en formato YYYY-MM-DD, para los inputs type="date". */
export function hoyBogota(): string {
  const ahora = new Date()
  const local = new Date(ahora.getTime() + OFFSET_BOGOTA_MINUTOS * 60 * 1000)
  const mes = String(local.getUTCMonth() + 1).padStart(2, '0')
  const dia = String(local.getUTCDate()).padStart(2, '0')
  return `${local.getUTCFullYear()}-${mes}-${dia}`
}

/** Hora actual (Bogota) en formato HH:mm, para los inputs type="time". */
export function horaActualBogota(): string {
  return horaBogota(new Date().toISOString())
}

/** Suma (o resta) dias a una fecha YYYY-MM-DD y la devuelve en ese formato. */
export function sumarDias(fecha: string, dias: number): string {
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const base = new Date(Date.UTC(anio, (mes ?? 1) - 1, dia ?? 1))
  base.setUTCDate(base.getUTCDate() + dias)
  const m = String(base.getUTCMonth() + 1).padStart(2, '0')
  const d = String(base.getUTCDate()).padStart(2, '0')
  return `${base.getUTCFullYear()}-${m}-${d}`
}

/**
 * Indica si la fecha/hora local de Bogota ya paso respecto a `ahora` (por
 * defecto el instante actual). Se usa para impedir agendar en el pasado.
 */
export function yaPaso(fecha: string, hora: string, ahora: Date = new Date()): boolean {
  const instante = aInstanteUtc(fecha, hora)
  if (!instante) return false
  return new Date(instante).getTime() <= ahora.getTime()
}

/**
 * Indica si un instante ISO (UTC) ya llego respecto a `ahora`. Determina si una
 * cita PROGRAMADA puede marcarse CUMPLIDA/INCUMPLIDA y si una fecha de medicion
 * de signos vitales queda en el futuro.
 */
export function instanteYaPaso(iso: string, ahora: Date = new Date()): boolean {
  return new Date(iso).getTime() <= ahora.getTime()
}

/** Agrupa elementos por su dia (Bogota) a partir de un campo de instante ISO. */
export function agruparPorDia<T extends { fechaHora: string }>(
  citas: T[],
): { fecha: string; citas: T[] }[] {
  const grupos = new Map<string, T[]>()
  for (const cita of citas) {
    const dia = fechaBogota(cita.fechaHora)
    const actual = grupos.get(dia)
    if (actual) {
      actual.push(cita)
    } else {
      grupos.set(dia, [cita])
    }
  }
  return [...grupos.entries()]
    .map(([fecha, lista]) => ({ fecha, citas: lista }))
    .sort((a, b) => a.fecha.localeCompare(b.fecha))
}
