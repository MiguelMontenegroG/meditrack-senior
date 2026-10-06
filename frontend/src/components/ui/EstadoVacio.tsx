import { Inbox } from 'lucide-react'
import type { ReactNode } from 'react'

// Componente reutilizable de estado vacio: cuando una consulta fue exitosa
// pero no hay datos que mostrar.

interface PropsEstadoVacio {
  /** Titulo breve del estado vacio. */
  titulo: string
  /** Descripcion opcional. */
  descripcion?: string
  /** Accion opcional (por ejemplo un boton para crear el primer registro). */
  accion?: ReactNode
}

/** Aviso de lista/contenido vacio, accesible. */
export function EstadoVacio({ titulo, descripcion, accion }: PropsEstadoVacio) {
  return (
    <div className="estado estado-vacio" role="status">
      <span className="estado-icon">
        <Inbox size={26} />
      </span>
      <h3>{titulo}</h3>
      {descripcion && <p>{descripcion}</p>}
      {accion}
    </div>
  )
}
