// Datos unicos de una ejecucion y estado compartido entre pasos.
//
// Todo lo que se crea lleva el prefijo E2E- y un sufijo aleatorio, para no
// chocar con datos reales ni con ejecuciones anteriores. Las contrasenas de los
// usuarios creados se generan por ejecucion y nunca se imprimen.
//
// El estado se guarda en test-results/estado-compartido.json para que sobreviva
// a un reintento o a una ejecucion por separado de cada caso.

import { createHash, randomBytes, randomInt } from 'node:crypto'
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'
import type { Page } from '@playwright/test'
import { DIRECTORIO_E2E } from './entorno'

/** Datos aleatorios de la ejecucion. */
export interface DatosEjecucion {
    sufijo: string
    documento: string
    /** Documento distinto, para el 409 de duplicado (CP-04). */
    documentoDuplicadoBase: string
    nombres: string
    apellidos: string
    nombreResidente: string
    /** Residentes adicionales creados por API para cuidadores y familiar. */
    nombreResidenteSanitario: string
    nombreResidenteClinica: string
    nombreMedicamento: string
    codigoMedicamento: string
    correoUsuarioDuplicado: string
    presentacionMedicamento: string
    correoCuidador: string
    correoFamiliar: string
    nombreCuidador: string
    nombreFamiliar: string
    contrasenaCuidador: string
    contrasenaFamiliar: string
    motivoDesactivacion: string
    profesionalCita: string
}

/** Estado acumulado entre casos (ids creados y datos derivados). */
export interface EstadoCompartido {
    /** Semilla de la ejecucion (sirve para saber si el estado es del mismo run). */
    semilla: string
    residenteNombre: string
    residenteDocumento: string
    pacienteId?: number
    pacienteSanitarioId?: number
    pacienteClinicaId?: number
    medicamentoId?: number
    esquemaId?: number
    cuidadorId?: number
    cuidadorCorreo?: string
    cuidadorContrasena?: string
    familiarId?: number
    familiarCorreo?: string
    familiarContrasena?: string
    citaId?: number
    /** true cuando el residente ya quedo inactivo. */
    residenteInactivo?: boolean
    /** Estado actual del residente principal (para reinicios/reintentos). */
    residenteActivo?: boolean
    /** true cuando el correo del cuidador ya esta ocupado por un usuario creado. */
    correoCuidadorEnUso?: boolean
    /** Ultimo horario programado ya registrado en el historial de tomas. */
    ultimaTomaRegistrada?: string
}

/** Sesion que el frontend restaura desde sessionStorage. */
export interface SesionApi {
    token: string
    usuario: {
        id: number
        nombreCompleto: string
        correo: string
        rol: string
    }
}

const CARPETA_RESULTADOS = join(DIRECTORIO_E2E, 'test-results')
const ARCHIVO_ESTADO = join(CARPETA_RESULTADOS, 'estado-compartido.json')

/** Sufijo corto y alfanumerico unico por ejecucion (o por worker). */
function nuevoSufijo(): string {
    const base = `${Date.now().toString(36)}${randomBytes(3).toString('hex')}`
    return base.replace(/[^a-z0-9]/g, '').slice(-10)
}

/** Contrasena aleatoria de la ejecucion (nunca se imprime). */
function nuevaContrasena(semilla: string): string {
    return `E2e-${createHash('sha256').update(semilla).digest('hex').slice(0, 12)}`
}

/** Genera un documento numerico aleatorio de 10 digitos. */
function nuevoDocumento(): string {
    return String(randomInt(1_000_000_000, 9_999_999_999))
}

/** Crea los datos de la ejecucion a partir de una semilla. */
export function crearDatosEjecucion(semilla = nuevoSufijo()): DatosEjecucion {
    const documento = nuevoDocumento()
    // El documento del duplicado debe ser distinto: el 409 se prueba con un
    // residente nuevo reutilizando el documento del principal (CP-04).
    let documentoDuplicado = nuevoDocumento()
    while (documentoDuplicado === documento) documentoDuplicado = nuevoDocumento()
    const nombres = `E2E-${semilla} Ana`
    const apellidos = `Residente ${semilla}`
    return {
        sufijo: semilla,
        documento,
        documentoDuplicadoBase: documentoDuplicado,
        nombres,
        apellidos,
        nombreResidente: `${apellidos}, ${nombres}`,
        nombreResidenteSanitario: `E2E-${semilla} Sofia`,
        nombreResidenteClinica: `E2E-${semilla} Elena`,
        nombreMedicamento: `E2E-${semilla} Losartan`,
        codigoMedicamento: `e2e-${semilla}-medi`,
        correoUsuarioDuplicado: `e2e-${semilla}-cuidador@meditrack.test`,
        presentacionMedicamento: 'Tabletas 50 mg',
        correoCuidador: `e2e-${semilla}-cuidador@meditrack.test`,
        correoFamiliar: `e2e-${semilla}-familiar@meditrack.test`,
        nombreCuidador: `E2E-${semilla} Cuidador Prueba`,
        nombreFamiliar: `E2E-${semilla} Familiar Prueba`,
        contrasenaCuidador: nuevaContrasena(`${semilla}:cuidador`),
        contrasenaFamiliar: nuevaContrasena(`${semilla}:familiar`),
        motivoDesactivacion: `E2E-${semilla} alta por recuperacion`,
        profesionalCita: `E2E-${semilla} Dra. Control`,
    }
}

/** Estado vacio asociado a una semilla. */
export function estadoInicial(datos: DatosEjecucion): EstadoCompartido {
    return {
        semilla: datos.sufijo,
        residenteNombre: datos.nombreResidente,
        residenteDocumento: datos.documento,
    }
}

/** Lee el estado compartido si existe y pertenece a esta ejecucion. */
export function leerEstado(semilla: string): EstadoCompartido | null {
    if (!existsSync(ARCHIVO_ESTADO)) return null
    try {
        const estado = JSON.parse(readFileSync(ARCHIVO_ESTADO, 'utf-8')) as EstadoCompartido
        return estado.semilla === semilla ? estado : null
    } catch {
        return null
    }
}

/** Persiste el estado compartido (best effort, no falla la prueba). */
export function guardarEstado(estado: EstadoCompartido): void {
    try {
        if (!existsSync(CARPETA_RESULTADOS)) mkdirSync(CARPETA_RESULTADOS, { recursive: true })
        writeFileSync(ARCHIVO_ESTADO, JSON.stringify(estado, null, 2), 'utf-8')
    } catch {
        // Si no se puede escribir el estado, se pierde el compartir entre pasos.
    }
}

/** Datos aleatorios de la ejecucion, compartidos por todos los casos. */
export const DATOS: DatosEjecucion = crearDatosEjecucion()

/**
 * Estado compartido entre casos: arranca del estado guardado de esta misma
 * ejecucion (reintentos o casos corridos por separado) o del estado inicial.
 */
export const ESTADO: EstadoCompartido = leerEstado(DATOS.sufijo) ?? estadoInicial(DATOS)

/** Persiste el estado compartido actual (ids creados durante el flujo). */
export function persistir(): void {
    guardarEstado(ESTADO)
}

/**
 * Deja en sessionStorage la sesion ya autenticada. Se usa para no repetir el
 * formulario de login en todos los casos: el usuario se autentica una vez por
 * API y el navegador arranca ya dentro de la aplicacion.
 */
export async function sembrarSesion(
    pagina: Page,
    sesion: SesionApi,
    baseUrl: string,
): Promise<void> {
    await pagina.goto(`${baseUrl}/login`)
    await pagina.evaluate((datos) => {
        sessionStorage.setItem('meditrack.token', datos.token)
        sessionStorage.setItem('meditrack.usuario', JSON.stringify(datos.usuario))
    }, sesion)
}

/**
 * Fecha ISO (YYYY-MM-DD) de dentro de N dias segun la zona de Bogota.
 * Sirve para citas futuras y para nombres de recursos sin colisiones.
 */
export function fechaEnDias(dias: number, base: Date = new Date()): string {
    const local = new Date(base.toLocaleString('en-US', { timeZone: 'America/Bogota' }))
    local.setDate(local.getDate() + dias)
    const mes = String(local.getMonth() + 1).padStart(2, '0')
    const dia = String(local.getDate()).padStart(2, '0')
    return `${local.getFullYear()}-${mes}-${dia}`
}

/**
 * Partes de la fecha y hora actuales en Bogota. Se calculan una sola vez por
 * proceso para que la UI (que tambien trabaja en America/Bogota) y las
 * aserciones coincidan.
 */
function partesBogota(): { fecha: string; hora: string; minutos: number } {
    const partes = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'America/Bogota',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).formatToParts(new Date())
    const valor = (tipo: string) => partes.find((p) => p.type === tipo)?.value ?? '00'
    const hora = valor('hour') === '24' ? '00' : valor('hour')
    const minuto = valor('minute')
    return {
        fecha: `${valor('year')}-${valor('month')}-${valor('day')}`,
        hora: `${hora}:${minuto}`,
        minutos: Number(hora) * 60 + Number(minuto),
    }
}

/** Fecha de hoy (YYYY-MM-DD) en Bogota. */
export function hoyEnBogota(): string {
    return partesBogota().fecha
}

/** Hora actual (HH:mm) en Bogota. */
export function horaActualEnBogota(): string {
    return partesBogota().hora
}

/** Hora (HH:mm) de dentro de N minutos, en el mismo dia de Bogota. */
export function horaEnMinutos(minutos: number): string {
    const total = partesBogota().minutos + minutos
    // Se mantiene dentro del mismo dia para no cruzar la medianoche.
    const acotado = Math.min(Math.max(total, 1), 23 * 60 + 55)
    const hh = String(Math.floor(acotado / 60)).padStart(2, '0')
    const mm = String(acotado % 60).padStart(2, '0')
    return `${hh}:${mm}`
}
