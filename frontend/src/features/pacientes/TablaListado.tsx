// Tabla del listado de residentes para escritorio. Solo presenta: no carga
// no busca, no pagina y no decide permisos (los recibe ya resueltos).

import { ChevronRight } from 'lucide-react'
import type { PacienteResumen } from '@/features/pacientes/types'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'

interface PropsTablaListado {
  residentes: PacienteResumen[]
  /** Recibe el id al abrir el detalle de una fila. */
  alAbrir: (id: number) => void
}

/** Tabla accesible del listado de residentes (escritorio). */
export function TablaListado({ residentes, alAbrir }: PropsTablaListado) {
  return (
    <div className="table-wrap">
      <table className="tabla-residentes">
        <caption className="sr-only">Listado de residentes</caption>
        <thead>
          <tr>
            <th scope="col">Residente</th>
            <th scope="col">Edad</th>
            <th scope="col">Habitación</th>
            <th scope="col">Estado</th>
            <th scope="col"><span className="sr-only">Acciones</span></th>
          </tr>
        </thead>
        <tbody>
          {residentes.map((residente) => (
            <tr key={residente.id}>
              <td>
                <div className="resident-cell">
                  <strong>{residente.nombreCompleto}</strong>
                </div>
              </td>
              <td>{residente.edad} años</td>
              <td>{residente.habitacion ?? 'Sin asignar'}</td>
              <td>
                <EtiquetaEstado tono={residente.activo ? 'success' : 'neutral'}>
                  {residente.activo ? 'Activo' : 'Inactivo'}
                </EtiquetaEstado>
              </td>
              <td>
                <button
                  type="button"
                  className="row-action"
                  aria-label={`Ver detalle de ${residente.nombreCompleto}`}
                  onClick={() => alAbrir(residente.id)}
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
