// Reglas de permisos del modulo de alertas reflejadas en la interfaz. El
// backend es la fuente de verdad: AlertaController protege GET /api/alertas
// para los tres roles (lectura segun alcance) y PATCH /api/alertas/{id}/atender
// solo para ADMINISTRADOR y CUIDADOR_ENFERMERO. Aqui solo se decide que
// controles mostrar.

import type { RolUsuario } from '@/features/auth/types'

/** Los tres roles pueden ver el listado de alertas de su alcance. */
export function puedeVerAlertas(rol: RolUsuario | undefined): boolean {
  return (
    rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO' || rol === 'FAMILIAR_AUTORIZADO'
  )
}

/**
 * Atender una alerta: ADMINISTRADOR o CUIDADOR_ENFERMERO. El FAMILIAR_AUTORIZADO
 * no ve el boton (el backend le responde 403).
 */
export function puedeAtenderAlertas(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO'
}
