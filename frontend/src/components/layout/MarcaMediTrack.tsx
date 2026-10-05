import { Activity } from 'lucide-react'

// Marca visual de MediTrack Senior, reutilizada en el login, la barra lateral
// y el encabezado. Se extrae para no duplicar el logotipo en cada pantalla.

function Simbolo() {
  return (
    <span className="brand-mark" aria-hidden="true">
      <Activity size={19} strokeWidth={2.3} />
    </span>
  )
}

interface PropsMarca {
  /** Version para fondos oscuros (barra lateral y aside del login). */
  oscuro?: boolean
}

/** Logotipo textual + simbolo de MediTrack Senior. */
export function MarcaMediTrack({ oscuro = false }: PropsMarca) {
  return (
    <div className={`brand ${oscuro ? 'brand-dark' : ''}`}>
      <Simbolo />
      <span>
        MediTrack <b>Senior</b>
      </span>
    </div>
  )
}
