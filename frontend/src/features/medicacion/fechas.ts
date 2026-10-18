// Utilidades puras del modulo de medicacion: formateo de horas y fechas y
// calculo de los horarios sugeridos segun la frecuencia. No dependen de React
// ni del backend, por lo que son faciles de probar.

/**
 * Genera las horas del dia (HH:mm) sugeridas para una frecuencia en horas.
 * Cuando 24 es divisible por la frecuencia se generan exactamente 24/frecuencia
 * horas equiespaciadas empezando a la hora de inicio indicada; si no, devuelve
 * un unico horario a la hora de inicio.
 */
export function sugerirHorarios(frecuenciaHoras: number, horaInicio = 8): string[] {
  const frecuencia = Math.trunc(frecuenciaHoras)
  if (!Number.isFinite(frecuencia) || frecuencia < 1 || frecuencia > 24) {
    return []
  }
  if (24 % frecuencia !== 0) {
    return [formatearHora(horaInicio)]
  }
  const cantidad = 24 / frecuencia
  const horas: string[] = []
  for (let i = 0; i < cantidad; i += 1) {
    horas.push(formatearHora((horaInicio + i * frecuencia) % 24))
  }
  return horas
}

/** Formatea una hora entera (0-23) como "HH:00". */
export function formatearHora(hora: number): string {
  const h = ((hora % 24) + 24) % 24
  return `${String(h).padStart(2, '0')}:00`
}

/** Valida el formato HH:mm (24 horas). */
export function esHoraValida(valor: string): boolean {
  return /^([01]\d|2[0-3]):[0-5]\d$/.test(valor)
}

/** Indica si 24 es divisible por la frecuencia (horarios equiespaciados). */
export function frecuenciaPermiteHorariosExactos(frecuenciaHoras: number): boolean {
  const frecuencia = Math.trunc(frecuenciaHoras)
  return Number.isFinite(frecuencia) && frecuencia >= 1 && frecuencia <= 24 && 24 % frecuencia === 0
}

/**
 * Convierte una fecha local (YYYY-MM-DD) y una hora (HH:mm) al instante ISO
 * (con zona) que espera el backend para `fechaHoraProgramada`.
 */
export function aFechaHoraIso(fecha: string, hora: string): string {
  const [anio, mes, dia] = fecha.split('-').map(Number)
  const [h, m] = hora.split(':').map(Number)
  return new Date(anio, (mes ?? 1) - 1, dia ?? 1, h ?? 0, m ?? 0, 0).toISOString()
}

/** Formatea un instante ISO a "HH:mm" en la zona local del navegador. */
export function horaLocal(iso: string): string {
  const fecha = new Date(iso)
  return `${String(fecha.getHours()).padStart(2, '0')}:${String(fecha.getMinutes()).padStart(2, '0')}`
}

/** Formatea un instante ISO como fecha y hora local legible (dd/mm/aaaa HH:mm). */
export function fechaHoraLocal(iso: string | null): string {
  if (!iso) return '—'
  const fecha = new Date(iso)
  const fechaTexto = fecha.toLocaleDateString('es-CO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
  return `${fechaTexto} ${horaLocal(iso)}`
}

/** Fecha local de hoy como YYYY-MM-DD (para los inputs type="date"). */
export function hoyIso(): string {
  const ahora = new Date()
  const mes = String(ahora.getMonth() + 1).padStart(2, '0')
  const dia = String(ahora.getDate()).padStart(2, '0')
  return `${ahora.getFullYear()}-${mes}-${dia}`
}
