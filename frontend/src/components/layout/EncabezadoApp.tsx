import { Bell, LogOut, Menu, Search } from 'lucide-react'
import { MarcaMediTrack } from '@/components/layout/MarcaMediTrack'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import type { RolUsuario } from '@/features/auth/types'

// Encabezado de la aplicacion: marca en movil, titulo de pagina y acciones
// (busqueda, alertas y menu de usuario). Los botones de icono cumplen el
// minimo tactil de 44px en movil (ver clase .icon-button en globals.css).

/** Etiqueta legible del rol del usuario autenticado. */
const ETIQUETA_ROL: Record<RolUsuario, string> = {
  ADMINISTRADOR: 'Administrador',
  CUIDADOR_ENFERMERO: 'Cuidador enfermero',
  FAMILIAR_AUTORIZADO: 'Familiar autorizado',
}

/** Iniciales del nombre completo (maximo dos letras). */
function iniciales(nombreCompleto: string): string {
  return nombreCompleto
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

interface PropsEncabezadoApp {
  titulo: string
}

/** Cabecera superior del area principal. */
export function EncabezadoApp({ titulo }: PropsEncabezadoApp) {
  const { usuario, cerrarSesion } = useAutenticacion()

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
          <kbd>Ctrl K</kbd>
        </label>
        <button className="icon-button alert-button" aria-label="Ver alertas">
          <Bell size={20} />
          <i>4</i>
        </button>
        <div className="user-menu">
          <span className="avatar avatar-gold">{usuario ? iniciales(usuario.nombreCompleto) : '--'}</span>
          <span className="user-menu-copy">
            <strong>{usuario?.nombreCompleto ?? 'Sin sesion'}</strong>
            <small>{usuario ? ETIQUETA_ROL[usuario.rol] : ''}</small>
          </span>
        </div>
        <button className="icon-button" onClick={cerrarSesion} aria-label="Cerrar sesion" title="Cerrar sesion">
          <LogOut size={19} />
        </button>
      </div>
    </header>
  )
}