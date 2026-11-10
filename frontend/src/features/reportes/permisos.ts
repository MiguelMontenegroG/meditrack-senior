// Reglas de permisos del modulo de reportes reflejadas en la interfaz. El
// backend es la fuente de verdad: ReporteController permite a los tres roles
// listar y generar reportes, siempre sobre residentes dentro de su alcance
// (404 si no es visible). Aqui solo se decide que controles mostrar.

import type { RolUsuario } from '@/features/auth/types'

/** Los tres roles pueden ver el historial y generar reportes. */
export function puedeVerReportes(rol: RolUsuario | undefined): boolean {
  return (
    rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO' || rol === 'FAMILIAR_AUTORIZADO'
  )
}

/** Generar/descargar reportes: los tres roles (lectura agregada). */
export function puedeGenerarReportes(rol: RolUsuario | undefined): boolean {
  return (
    rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO' || rol === 'FAMILIAR_AUTORIZADO'
  )
}
