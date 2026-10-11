// Reglas de permisos reflejadas en la interfaz. El backend YA aplica la
// seguridad real (404 fuera de alcance, 403 en escritura); esto solo decide
// que controles mostrar. La fuente de verdad es el backend, no este archivo.

import type { RolUsuario } from '@/features/pacientes/types'

/**
 * ADMINISTRADOR es el unico rol que puede crear, editar, cambiar estado y
 * gestionar contactos, familiares y cuidadores.
 */
export function puedeEscribir(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR'
}
