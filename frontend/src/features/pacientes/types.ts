// Tipos del contrato del modulo paciente. Espejo exacto de los DTO de
// docs/api/openapi.yaml (PacienteResumen, PacienteDetalle, ContactoEmergencia,
// VinculacionFamiliar, AsignacionCuidador y PagePacienteResumen).

/** Sexo registrado del residente. */
export type Sexo = 'MASCULINO' | 'FEMENINO' | 'OTRO' | 'NO_ESPECIFICA'

/** Roles que puede tener un usuario vinculable (familiar o cuidador). */
export type RolUsuario = 'ADMINISTRADOR' | 'CUIDADOR_ENFERMERO' | 'FAMILIAR_AUTORIZADO'

/** Fila del listado de residentes (resumen sin situacion clinica). */
export interface PacienteResumen {
  id: number
  nombreCompleto: string
  edad: number
  habitacion: string | null
  activo: boolean
}

/** Contacto de emergencia de un residente. */
export interface ContactoEmergencia {
  id: number
  nombre: string
  parentesco: string | null
  telefono: string
  principal: boolean
}

/** Detalle completo del residente, incluido su listado de contactos. */
export interface PacienteDetalle {
  id: number
  documento: string
  nombres: string
  apellidos: string
  nombreCompleto: string
  fechaNacimiento: string
  edad: number
  sexo: Sexo
  habitacion: string | null
  situacionClinica: string | null
  fechaIngreso: string | null
  activo: boolean
  contactos: ContactoEmergencia[]
}

/** Cuerpo para crear o actualizar un residente (POST/PUT /api/pacientes). */
export interface DatosPaciente {
  documento: string
  nombres: string
  apellidos: string
  fechaNacimiento: string
  sexo: Sexo
  habitacion?: string | null
  situacionClinica?: string | null
  fechaIngreso?: string | null
}

/** Cuerpo para crear o actualizar un contacto de emergencia. */
export interface DatosContacto {
  nombre: string
  parentesco?: string | null
  telefono: string
  principal: boolean
}

/** Vinculacion de un familiar autorizado con un residente. */
export interface VinculacionFamiliar {
  id: number
  usuarioId: number
  pacienteId: number
  parentesco: string | null
  autorizado: boolean
}

/** Asignacion vigente de un cuidador enfermero a un residente. */
export interface AsignacionCuidador {
  id: number
  usuarioId: number
  pacienteId: number
  desde: string
  hasta: string | null
}

/** Pagina del listado de residentes devuelta por el backend. */
export interface PaginaPacientes {
  contenido: PacienteResumen[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del listado de residentes (se envian como query string). */
export interface FiltrosPacientes {
  texto?: string
  activo?: boolean
  pagina?: number
  tamano?: number
}
