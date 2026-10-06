import { CircleAlert } from 'lucide-react'
import type { ReactNode } from 'react'

// Componente reutilizable de estado de error. Muestra un mensaje en espanol
// (nunca tecnicos) y una accion opcional para reintentar.

interface PropsEstadoError {
  /** Mensaje en espanol listo para mostrar. */
  mensaje: string
  /** Accion opcional, por ejemplo un boton "Reintentar". */
  accion?: ReactNode
}

/** Aviso de error accesible (role="alert"). */
export function EstadoError({ mensaje, accion }: PropsEstadoError) {
  return (
    <div className="estado estado-error" role="alert">
      <span className="estado-icon">
        <CircleAlert size={26} />
      </span>
      <p>{mensaje}</p>
      {accion}
    </div>
  )
}
