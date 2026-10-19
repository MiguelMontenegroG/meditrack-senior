import { ClipboardList } from 'lucide-react'

// Pantalla reutilizable para rutas que aun no tienen datos reales que mostrar.
// Regla del proyecto: este componente NO ofrece acciones. Un boton "Crear
// nuevo" aqui no tiene destino ni endpoint detras, asi que enganaba al usuario;
// si una seccion necesita un alta, la pinta su propia pantalla con su ruta real.
interface PropsEnConstruccion {
  /** Nombre de la seccion que aun no tiene pantalla. */
  titulo: string
  /** Texto explicativo opcional; por defecto se describe el estado. */
  descripcion?: string
}

/** Aviso de seccion en construccion, con el lenguaje visual del diseno. */
export function EnConstruccion({ titulo, descripcion }: PropsEnConstruccion) {
  return (
    <div className="page-content">
      <div className="empty-page">
        <span className="empty-icon">
          <ClipboardList size={26} />
        </span>
        <p className="eyebrow">MediTrack Senior</p>
        <h2>{titulo}</h2>
        <p className="muted">
          {descripcion ?? 'Esta seccion aun no tiene una pantalla con datos reales.'}
        </p>
      </div>
    </div>
  )
}
