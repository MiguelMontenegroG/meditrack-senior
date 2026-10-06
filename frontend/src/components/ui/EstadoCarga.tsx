import { Loader2 } from 'lucide-react'

// Componente reutilizable de estado de carga. Se usa mientras se valida una
// sesion guardada o se esperan datos del backend.

interface PropsEstadoCarga {
  /** Texto mostrado junto al indicador (por defecto "Cargando..."). */
  mensaje?: string
}

/** Indicador de carga accesible (aria-busy + texto para lectores de pantalla). */
export function EstadoCarga({ mensaje = 'Cargando...' }: PropsEstadoCarga) {
  return (
    <div className="estado estado-carga" role="status" aria-live="polite" aria-busy="true">
      <Loader2 className="estado-spinner" size={26} />
      <p>{mensaje}</p>
    </div>
  )
}
