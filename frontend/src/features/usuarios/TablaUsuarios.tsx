// Tabla del listado de usuarios para escritorio. Solo presenta: no carga,
// no filtra y no decide permisos (los recibe ya resueltos).

import { ChevronRight } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { ETIQUETA_ROL } from '@/features/usuarios/permisos'
import type { Usuario } from '@/features/usuarios/types'

interface PropsTablaUsuarios {
  usuarios: Usuario[]
  /** Id del usuario autenticado, para marcarlo como "Tú". */
  usuarioActualId: number | undefined
  /** Recibe el id al elegir editar una fila. */
  alEditar: (id: number) => void
}

/** Tabla accesible del listado de usuarios (escritorio). */
export function TablaUsuarios({ usuarios, usuarioActualId, alEditar }: PropsTablaUsuarios) {
  return (
    <div className="table-wrap">
      <table className="tabla-residentes">
        <caption className="sr-only">Listado de usuarios</caption>
        <thead>
          <tr>
            <th scope="col">Usuario</th>
            <th scope="col">Rol</th>
            <th scope="col">Estado</th>
            <th scope="col"><span className="sr-only">Acciones</span></th>
          </tr>
        </thead>
        <tbody>
          {usuarios.map((usuario) => (
            <tr key={usuario.id}>
              <td>
                <div className="resident-cell">
                  <strong>
                    {usuario.nombreCompleto}
                    {usuario.id === usuarioActualId && (
                      <span className="marca-tu">Tú</span>
                    )}
                  </strong>
                  <small>{usuario.correo}</small>
                </div>
              </td>
              <td>{ETIQUETA_ROL[usuario.rol]}</td>
              <td>
                <EtiquetaEstado tono={usuario.activo ? 'success' : 'neutral'}>
                  {usuario.activo ? 'Activo' : 'Inactivo'}
                </EtiquetaEstado>
              </td>
              <td>
                <button
                  type="button"
                  className="row-action"
                  aria-label={`Editar a ${usuario.nombreCompleto}`}
                  onClick={() => alEditar(usuario.id)}
                >
                  <ChevronRight size={18} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
