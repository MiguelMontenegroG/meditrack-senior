// Unico lugar donde se traduce un codigo de error del backend (ApiError)
// a un mensaje en espanol apto para el usuario final.
//
// Regla de seguridad/seleccion: nunca se muestran mensajes tecnicos, el
// cuerpo crudo de la respuesta ni detalles internos. Si el codigo es
// desconocido se usa un mensaje generico.

/** Codigos que puede devolver el backend (ver docs/api/openapi.yaml). */
export type CodigoError =
  | 'CREDENCIALES_INVALIDAS'
  | 'NO_AUTENTICADO'
  | 'ACCESO_DENEGADO'
  | 'VALIDACION'
  | 'CUERPO_INVALIDO'
  | 'CONFLICTO'
  | 'ERROR_INTERNO'
  // Codigos generados en el cliente (no vienen del backend):
  | 'ERROR_RED'
  | 'ERROR_INESPERADO'

const MENSAJES: Record<CodigoError, string> = {
  // No se revela si el correo existe: el mensaje es el mismo para cualquier
  // combinacion invalida de correo/contrasena.
  CREDENCIALES_INVALIDAS: 'Correo o contrasena incorrectos.',
  NO_AUTENTICADO: 'Tu sesion expiro. Ingresa de nuevo.',
  ACCESO_DENEGADO: 'No tienes permiso para realizar esta accion.',
  VALIDACION: 'Revisa los datos ingresados e intentalo de nuevo.',
  CUERPO_INVALIDO: 'No pudimos procesar la solicitud. Intentalo de nuevo.',
  CONFLICTO: 'La operacion entra en conflicto con el estado actual.',
  ERROR_INTERNO: 'Ocurrio un error en el servidor. Intentalo mas tarde.',
  ERROR_RED: 'No pudimos conectar con el servidor. Verifica tu conexion e intentalo de nuevo.',
  ERROR_INESPERADO: 'Ocurrio un error inesperado. Intentalo de nuevo.',
}

/** Mensaje en espanol para un codigo de error; generico si es desconocido. */
export function mensajeDeError(codigo: string | undefined): string {
  if (codigo && codigo in MENSAJES) {
    return MENSAJES[codigo as CodigoError]
  }
  return MENSAJES.ERROR_INESPERADO
}

/** Contextos de negocio que afinan el mensaje generico de un codigo. */
export type ContextoError =
  | 'documento-duplicado'
  | 'correo-duplicado'
  | 'usuario-propio'
  | 'medicamento-duplicado'
  | 'esquema-solapado'
  | 'residente-inactivo'
  | 'toma-duplicada'
  | 'toma-cerrada'
  | 'perfil-clinico-incompleto'
  | 'motivo-obligatorio'
  | 'cita-fecha-pasada'
  | 'cita-duplicada'
  | 'cita-estado-final'
  | 'cita-hora-no-llegada'
  | 'especialidad-inexistente'
  | 'bitacora-fecha-futura'
  | 'bitacora-sin-signos'
  | 'bitacora-valores-incoherentes'

const MENSAJES_CONTEXTO: Record<ContextoError, Partial<Record<CodigoError, string>>> = {
  // El backend responde 409 CONFLICTO cuando el documento ya existe.
  'documento-duplicado': {
    CONFLICTO: 'Ya existe un residente con ese documento.',
  },
  // El backend responde 409 CONFLICTO cuando el correo ya esta registrado
  // (comparacion sin distinguir mayusculas).
  'correo-duplicado': {
    CONFLICTO: 'Ya existe un usuario con ese correo.',
  },
  // El backend responde 409 CONFLICTO si un administrador intenta quitarse su
  // propio rol o desactivar su propia cuenta.
  'usuario-propio': {
    CONFLICTO: 'No puedes cambiar tu propio rol ni desactivar tu cuenta.',
  },
  // El backend responde 409 CONFLICTO si ya existe un medicamento con ese
  // nombre (comparacion sin distinguir mayusculas).
  'medicamento-duplicado': {
    CONFLICTO: 'Ya existe un medicamento con ese nombre.',
  },
  // El backend responde 409 CONFLICTO si ya hay un esquema activo del mismo
  // medicamento con vigencia solapada.
  'esquema-solapado': {
    CONFLICTO: 'Ya existe un esquema activo de ese medicamento con fechas que se solapan.',
  },
  // El backend responde 409 CONFLICTO si el residente o el medicamento estan
  // inactivos al crear un esquema.
  'residente-inactivo': {
    CONFLICTO: 'No se puede crear el esquema: el residente o el medicamento estan inactivos.',
  },
  // El backend responde 409 CONFLICTO si ya hay una toma para esa hora.
  'toma-duplicada': {
    CONFLICTO: 'Ya se registro una toma para esa hora programada.',
  },
  // El backend responde 400 VALIDACION si la hora no es un horario del esquema.
  'toma-cerrada': {
    VALIDACION: 'La hora seleccionada no corresponde a un horario programado del esquema.',
  },
  // El backend responde 400 VALIDACION si un residente activo no trae situacion
  // clinica (la habitacion es opcional) o si la fecha de ingreso es futura.
  'perfil-clinico-incompleto': {
    VALIDACION: 'Completa la situación clínica del residente; la fecha de ingreso no puede ser futura.',
  },
  // El backend responde 400 VALIDACION si se desactiva sin motivo.
  'motivo-obligatorio': {
    VALIDACION: 'Indica el motivo de la desactivación del residente.',
  },
  // 400 VALIDACION si la fecha y hora de la cita es pasada.
  'cita-fecha-pasada': {
    VALIDACION: 'La fecha y hora de la cita no puede estar en el pasado.',
  },
  // 400 VALIDACION si ya hay una cita del residente con esa misma fecha y hora.
  'cita-duplicada': {
    VALIDACION: 'Ya existe una cita para ese residente en la misma fecha y hora.',
  },
  // 409 CONFLICTO si la cita ya no esta en estado PROGRAMADA.
  'cita-estado-final': {
    CONFLICTO: 'La cita ya no esta en estado inicial (Programada); no se puede modificar.',
  },
  // 400 VALIDACION si se marca CUMPLIDA/INCUMPLIDA antes de la hora.
  'cita-hora-no-llegada': {
    VALIDACION: 'Aun no llega la fecha y hora de la cita; espera para marcarla.',
  },
  // 404 si la especialidad indicada no existe en el catalogo.
  'especialidad-inexistente': {
    CUERPO_INVALIDO: 'La especialidad seleccionada no existe.',
  },
  // 400 VALIDACION si la fecha de medicion de la bitacora es futura.
  'bitacora-fecha-futura': {
    VALIDACION: 'La fecha y hora de la medicion no puede estar en el futuro.',
  },
  // 400 VALIDACION si no se envia ningun signo vital.
  'bitacora-sin-signos': {
    VALIDACION: 'Registra al menos un signo vital: presion, glucosa o temperatura.',
  },
  // 400 VALIDACION por valores fuera de rango o presion incoherente.
  'bitacora-valores-incoherentes': {
    VALIDACION: 'Revisa los valores: deben estar dentro del rango y la diastolica ser menor que la sistolica.',
  },
}

/**
 * Mensaje contextual para un codigo dado. Si no hay contexto especifico,
 * cae en el mensaje general del codigo (nunca en detalles tecnicos).
 */
export function mensajeDeErrorContexto(
  codigo: string | undefined,
  contexto: ContextoError,
): string {
  if (codigo && codigo in MENSAJES_CONTEXTO[contexto]) {
    return MENSAJES_CONTEXTO[contexto][codigo as CodigoError] as string
  }
  return mensajeDeError(codigo)
}


/** Codigos de la API que puede traducir el helper de fallos. */
export interface FalloConCodigo {
  codigo: string
  mensajeUsuario: string
  status: number
}

/** Estrangula cualquier valor atrapado en un catch a un mensaje en espanol. */
export function mensajeDeFallo(fallo: unknown): string {
  if (fallo && typeof fallo === 'object' && 'mensajeUsuario' in fallo) {
    return (fallo as FalloConCodigo).mensajeUsuario
  }
  return MENSAJES.ERROR_INESPERADO
}