import type { ReactNode } from 'react'

// Boton de accion con las variantes visuales del diseno original.
// Se usa en formularios y encabezados de panel; no dependE de estado global.

export type VarianteBoton = 'primary' | 'ghost' | 'outline' | 'danger'

interface PropsBotonAccion {
  children: ReactNode
  variant?: VarianteBoton
  className?: string
  onClick?: () => void
  type?: 'button' | 'submit'
  /** Deshabilita el boton (p. ej. durante el envio, para evitar doble envio). */
  disabled?: boolean
}

/** Boton reutilizable con las variantes del sistema de diseno. */
export function BotonAccion({
  children,
  variant = 'primary',
  className = '',
  onClick,
  type = 'button',
  disabled = false,
}: PropsBotonAccion) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`btn btn-${variant} ${className}`}
    >
      {children}
    </button>
  )
}
