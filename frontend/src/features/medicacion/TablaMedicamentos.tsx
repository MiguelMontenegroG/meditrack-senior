// Tabla del catalogo de medicamentos para escritorio. Solo presenta: no carga,
// no filtra y no decide permisos (los recibe ya resueltos).

import { ChevronRight } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import type { Medicamento } from '@/features/medicacion/types'

interface PropsTablaMedicamentos {
  medicamentos: Medicamento[]
  /** Muestra la accion de editar (solo ADMINISTRADOR). */
  puedeGestionar: boolean
  alEditar: (medicamento: Medicamento) => void
}

/** Tabla accesible del catalogo de medicamentos (escritorio). */
export function TablaMedicamentos({
  medicamentos,
  puedeGestionar,
  alEditar,
}: PropsTablaMedicamentos) {
  return (
    <div className="table-wrap">
      <table className="tabla-residentes">
        <caption className="sr-only">Catalogo de medicamentos</caption>
        <thead>
          <tr>
            <th scope="col">Medicamento</th>
            <th scope="col">Presentacion</th>
            <th scope="col">Estado</th>
            <th scope="col"><span className="sr-only">Acciones</span></th>
          </tr>
        </thead>
        <tbody>
          {medicamentos.map((medicamento) => (
            <tr key={medicamento.id}>
              <td>
                <div className="resident-cell">
                  <strong>{medicamento.nombre}</strong>
                </div>
              </td>
              <td>{medicamento.presentacion ?? '—'}</td>
              <td>
                <EtiquetaEstado tono={medicamento.activo ? 'success' : 'neutral'}>
                  {medicamento.activo ? 'Activo' : 'Inactivo'}
                </EtiquetaEstado>
              </td>
              <td>
                {puedeGestionar && (
                  <button
                    type="button"
                    className="row-action"
                    aria-label={`Editar ${medicamento.nombre}`}
                    onClick={() => alEditar(medicamento)}
                  >
                    <ChevronRight size={18} />
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
