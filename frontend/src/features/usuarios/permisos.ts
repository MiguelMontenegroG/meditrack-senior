// Reglas de la interfaz para el modulo de usuarios. El backend YA aplica la
// seguridad real (solo ADMINISTRADOR gestiona usuarios y no permite que un
// administrador se desactive a si mismo ni se quite su rol). Esto solo decide
// que controles mostrar; la fuente de verdad es el backend.

import type { RolUsuario } from '@/features/usuarios/types'

/** Etiqueta legible y en espanol de cada rol. */
export const ETIQUETA_ROL: Record<RolUsuario, string> = {
  ADMINISTRADOR: 'Administrador',
  CUIDADOR_ENFERMERO: 'Cuidador enfermero',
  FAMILIAR_AUTORIZADO: 'Familiar autorizado',
}

/** Opciones de rol para los desplegables (crear y editar). */
export const OPCIONES_ROL: { valor: RolUsuario; etiqueta: string }[] = [
  { valor: 'ADMINISTRADOR', etiqueta: ETIQUETA_ROL.ADMINISTRADOR },
  { valor: 'CUIDADOR_ENFERMERO', etiqueta: ETIQUETA_ROL.CUIDADOR_ENFERMERO },
  { valor: 'FAMILIAR_AUTORIZADO', etiqueta: ETIQUETA_ROL.FAMILIAR_AUTORIZADO },
]

/**
 * Indica si el usuario autenticado es el mismo que la fila. Al propio usuario
 * no se le ofrece desactivarse ni cambiar su rol (el backend responde 409).
 */
export function esUsuarioPropio(usuarioId: number | undefined, filaId: number): boolean {
  return usuarioId !== undefined && usuarioId === filaId
}
