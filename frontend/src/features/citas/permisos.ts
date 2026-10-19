// Reglas de permisos del modulo de citas reflejadas en la interfaz. El backend
// es la fuente de verdad (aplica el alcance por rol y responde 403/404); aqui
// solo se decide que controles mostrar. Centralizar evita que cada componente
// repita la logica de rol.
//
// Permisos vigentes:
// - ADMINISTRADOR: agenda, edita, cancela y marca CUMPLIDA/INCUMPLIDA.
// - CUIDADOR_ENFERMERO: ve y marca CUMPLIDA/INCUMPLIDA (solo PROGRAMADA). No
//   agenda, no edita, no cancela.
// - FAMILIAR_AUTORIZADO: solo lectura; nunca ve botones de escritura.

import type { RolUsuario } from '@/features/auth/types'

/** Los tres roles pueden abrir el calendario de lectura. */
export function puedeVerCalendario(rol: RolUsuario | undefined): boolean {
  return (
    rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO' || rol === 'FAMILIAR_AUTORIZADO'
  )
}

/** Agendar y editar citas: solo ADMINISTRADOR. */
export function puedeAgendarCitas(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR'
}

/** Cancelar una cita: solo ADMINISTRADOR. */
export function puedeCancelarCitas(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR'
}

/**
 * Marcar CUMPLIDA/INCUMPLIDA: ADMINISTRADOR o CUIDADOR_ENFERMERO. El FAMILIAR
 * no. La condicion de "solo mientras siga PROGRAMADA" y de que la hora haya
 * llegado se evalua ademas con el estado de la cita y la fecha/hora.
 */
export function puedeMarcarEstadoCita(rol: RolUsuario | undefined): boolean {
  return rol === 'ADMINISTRADOR' || rol === 'CUIDADOR_ENFERMERO'
}