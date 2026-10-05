import { ClipboardList, Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'

// Pantalla reutilizable para rutas sin implementar todavia.
// Reemplaza al componente generico de relleno: cada ruta pendiente
// (pacientes, medicacion, citas, bitacora, reportes, admin) muestra esto.

interface PropsEnConstruccion {
  /** Nombre de la seccion que aun no tiene pantalla. */
  titulo: string
}

/** Aviso de seccion en construccion, con el lenguaje visual del diseno. */
export function EnConstruccion({ titulo }: PropsEnConstruccion) {
  return (
    <div className="page-content">
      <div className="empty-page">
        <span className="empty-icon">
          <ClipboardList size={26} />
        </span>
        <p className="eyebrow">MediTrack Senior</p>
        <h2>{titulo}</h2>
        <p className="muted">
          Esta vista esta lista para conectar sus datos clinicos y operaciones.
        </p>
        <BotonAccion>
          <Plus size={17} /> Crear nuevo
        </BotonAccion>
      </div>
    </div>
  )
}
