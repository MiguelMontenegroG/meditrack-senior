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
