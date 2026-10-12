// Listado de usuarios como tarjetas para movil. Cada tarjeta es un boton
// completo (objetivo tactil amplio) que abre la edicion.

import { ChevronRight } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { ETIQUETA_ROL } from '@/features/usuarios/permisos'
import type { Usuario } from '@/features/usuarios/types'

interface PropsTarjetasUsuarios {
  usuarios: Usuario[]
  usuarioActualId: number | undefined
  alEditar: (id: number) => void
}

/** Tarjetas de usuarios (movil); mismo dato que la tabla de escritorio. */
export function TarjetasUsuarios({ usuarios, usuarioActualId, alEditar }: PropsTarjetasUsuarios) {
  return (
    <ul className="tarjetas-residentes">
      {usuarios.map((usuario) => (
        <li key={usuario.id}>
          <button type="button" className="tarjeta-residente" onClick={() => alEditar(usuario.id)}>
            <span className="tarjeta-residente-info">
              <strong>
                {usuario.nombreCompleto}
                {usuario.id === usuarioActualId && <span className="marca-tu">Tú</span>}
              </strong>
              <small>
                {ETIQUETA_ROL[usuario.rol]} · {usuario.correo}
              </small>
              <EtiquetaEstado tono={usuario.activo ? 'success' : 'neutral'}>
                {usuario.activo ? 'Activo' : 'Inactivo'}
              </EtiquetaEstado>
            </span>
            <ChevronRight size={20} aria-hidden="true" />
          </button>
        </li>
      ))}
    </ul>
  )
}
