// Reglas de permisos reflejadas en la interfaz para el modulo de medicacion.
// El backend YA aplica la seguridad real (403 en escritura, 404 fuera de
// alcance); esto solo decide que controles mostrar. La fuente de verdad es el
// backend, no este archivo.

import type { RolUsuario } from '@/features/auth/types'

/**
 * Catalogo de medicamentos: escritura solo ADMINISTRADOR. Lectura tambien para
 * CUIDADOR_ENFERMERO; el FAMILIAR_AUTORIZADO no lo ve en la navegacion.
 */
export function puedeGestionarMedicamentos(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR'
}

/**
 * Esquemas de medicacion: crear y editar solo ADMINISTRADOR. Los demas roles con
 * alcance solo leen.
 */
export function puedeGestionarEsquemas(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR'
}

/**
 * Registro de tomas: ADMINISTRADOR y CUIDADOR_ENFERMERO (con alcance). El
 * FAMILIAR_AUTORIZADO es solo lectura.
 */
export function puedeRegistrarTomas(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO'
}
