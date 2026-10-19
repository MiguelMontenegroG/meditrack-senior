// Reglas de permisos del modulo de bitacora de signos vitales reflejadas en la
// interfaz. El backend es la fuente de verdad (aplica el alcance por rol y
// responde 403/404); aqui solo se decide que controles mostrar. Centralizar
// evita que cada componente repita la logica de rol.
//
// Permisos vigentes (docs/api/openapi.yaml, tag "Bitacora de signos vitales"):
// - ADMINISTRADOR y CUIDADOR_ENFERMERO: registran y leen.
// - FAMILIAR_AUTORIZADO: solo lee; nunca ve el formulario ni botones de
//   registro.
// Los registros son inmutables: ningun rol ve edicion ni borrado.

import type { RolUsuario } from '@/features/auth/types'

/** Los tres roles pueden abrir la bitacora y ver el historial. */
export function puedeLeerBitacora(rol: RolUsuario | undefined): boolean {
  return (
    rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO' || rol === 'FAMILIAR_AUTORIZADO'
  )
}

/** Registrar signos vitales: ADMINISTRADOR o CUIDADOR_ENFERMERO. */
export function puedeRegistrarSignosVitales(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO'
}
