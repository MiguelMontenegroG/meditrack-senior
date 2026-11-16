// Preparacion de usuarios y residentes por API.
//
// Para los casos de rol (cuidador y familiar) no interesa demostrar el alta de
// usuarios ni de residentes: se crean por API con el cliente del administrador
// y el navegador arranca con la sesion del rol ya sembrada. Asi el caso se
// concentra en lo que si es visible para el rol.
//
// Las contrasenas se generan por ejecucion y nunca se imprimen; el token del
// login solo se usa para sembrar la sesion del navegador.

import type { Page } from '@playwright/test'
import { ENTORNO } from './entorno'
import { clienteAdmin, crearCliente, type ClienteApi, type Credenciales } from './api'
import { sembrarSesion, type SesionApi } from './datos'

/** Rol tal como lo espera el backend. */
export type RolApi = 'ADMINISTRADOR' | 'CUIDADOR_ENFERMERO' | 'FAMILIAR_AUTORIZADO'

/** Usuario devuelto por /api/usuarios. */
export interface UsuarioApi {
  id: number
  nombreCompleto: string
  correo: string
  telefono: string | null
  rol: RolApi
  activo: boolean
}

/** Residente devuelto por /api/pacientes. */
export interface PacienteApi {
  id: number
  nombreCompleto: string
  edad: number
  habitacion: string | null
  activo: boolean
}

/** Pagina generica del backend (PageResponse). */
export interface PaginaApi<T> {
  contenido: T[]
  pagina: number
  tamano: number
  total: number
  totalPaginas?: number
}

/** Usuario creado por API junto con su contrasena (nunca se imprime). */
export interface UsuarioCreado {
  id: number
  nombreCompleto: string
  correo: string
  contrasena: string
  rol: RolApi
}

/** Crea un usuario activo por API y devuelve sus datos de acceso. */
export async function crearUsuario(
  api: ClienteApi,
  datos: { nombreCompleto: string; correo: string; contrasena: string; rol: RolApi },
): Promise<UsuarioCreado> {
  const creado = await api.post<UsuarioApi>('/usuarios', datos)
  return { ...datos, id: creado.id }
}

/** Crea un residente por API (documento numerico unico de la ejecucion). */
export async function crearResidente(
  api: ClienteApi,
  datos: { documento: string; nombres: string; apellidos: string; habitacion?: string },
): Promise<PacienteApi> {
  return api.post<PacienteApi>('/pacientes', {
    documento: datos.documento,
    nombres: datos.nombres,
    apellidos: datos.apellidos,
    fechaNacimiento: '1940-05-12',
    sexo: 'FEMENINO',
    habitacion: datos.habitacion ?? null,
    situacionClinica: 'Sin antecedentes relevantes',
    fechaIngreso: null,
  })
}

/** Une el administrador con el residente indicado (cuidador o familiar). */
export async function asignarCuidador(api: ClienteApi, pacienteId: number, usuarioId: number) {
  return api.post(`/pacientes/${pacienteId}/cuidadores`, { usuarioId })
}

/** Vincula un familiar autorizado al residente con su parentesco. */
export async function vincularFamiliar(
  api: ClienteApi,
  pacienteId: number,
  usuarioId: number,
  parentesco: string,
) {
  return api.post(`/pacientes/${pacienteId}/familiares`, { usuarioId, parentesco })
}

/**
 * Inicia sesion por API con el usuario indicado y devuelve la sesion lista para
 * `sembrarSesion` (misma forma que guarda el frontend: token + usuario).
 */
export async function sesionDe(correo: string, contrasena: string): Promise<SesionApi> {
  const anonimo = await crearCliente(null)
  try {
    const respuesta = await anonimo.post<{
      token: string
      usuario: { id: number; nombreCompleto: string; correo: string; rol: RolApi }
    }>('/auth/login', { correo, contrasena })
    return {
      token: respuesta.token,
      usuario: {
        id: respuesta.usuario.id,
        nombreCompleto: respuesta.usuario.nombreCompleto,
        correo: respuesta.usuario.correo,
        rol: respuesta.usuario.rol,
      },
    }
  } finally {
    await anonimo.cerrar()
  }
}

/**
 * Autentica en el navegador al usuario indicado y deja la sesion en
 * sessionStorage (el frontend la valida contra /auth/me al arrancar).
 */
export async function entrarComo(
  pagina: Page,
  correo: string,
  contrasena: string,
): Promise<void> {
  const sesion = await sesionDe(correo, contrasena)
  await sembrarSesion(pagina, sesion, ENTORNO.baseUrl)
}

/** Cliente del administrador autenticado (atajo para los casos). */
export function admin(credenciales: Credenciales): Promise<ClienteApi> {
  return clienteAdmin(credenciales)
}
