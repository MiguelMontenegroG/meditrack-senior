import { Bell, ChevronDown, Menu, Search } from 'lucide-react'
import { MarcaMediTrack } from '@/components/layout/MarcaMediTrack'

// Encabezado de la aplicacion: marca en movil, titulo de pagina y acciones
// (busqueda, alertas y menu de usuario). Los botones de icono cumplen el
// minimo tactil de 44px en movil (ver clase .icon-button en globals.css).

interface PropsEncabezadoApp {
  titulo: string
}

/** Cabecera superior del area principal. */
export function EncabezadoApp({ titulo }: PropsEncabezadoApp) {
  return (
    <header className="topbar">
      <div className="mobile-brand">
        <button className="icon-button" aria-label="Abrir menu">
          <Menu size={21} />
        </button>
        <MarcaMediTrack />
      </div>
      <div className="topbar-title">
        <span className="eyebrow">Centro de Vida Sol de los Andes</span>
        <h1>{titulo}</h1>
      </div>
      <div className="topbar-actions">
        <label className="search">
          <Search size={18} />
          <input aria-label="Buscar residente" placeholder="Buscar residente..." />
          <kbd>⌘ K</kbd>
        </label>
        <button className="icon-button alert-button" aria-label="Ver alertas">
          <Bell size={20} />
          <i>4</i>
        </button>
        <button className="user-menu">
          <span className="avatar avatar-gold">ML</span>
          <span className="user-menu-copy">
            <strong>Mariana Lopez</strong>
            <small>Administradora</small>
          </span>
          <ChevronDown size={16} />
        </button>
      </div>
    </header>
  )
}
