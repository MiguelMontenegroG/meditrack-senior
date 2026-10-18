// Listado del catalogo de medicamentos como tarjetas para movil. Cada tarjeta
// es un boton completo (objetivo tactil amplio) cuando el rol puede gestionar.

import { ChevronRight } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import type { Medicamento } from '@/features/medicacion/types'

interface PropsTarjetasMedicamentos {
  medicamentos: Medicamento[]
  puedeGestionar: boolean
  alEditar: (medicamento: Medicamento) => void
}

/** Tarjetas de medicamentos (movil); mismo dato que la tabla de escritorio. */
export function TarjetasMedicamentos({
  medicamentos,
  puedeGestionar,
  alEditar,
}: PropsTarjetasMedicamentos) {
  return (
    <ul className="tarjetas-residentes">
      {medicamentos.map((medicamento) => (
        <li key={medicamento.id}>
          {puedeGestionar ? (
            <button
              type="button"
              className="tarjeta-residente"
              onClick={() => alEditar(medicamento)}
            >
              <span className="tarjeta-residente-info">
                <strong>{medicamento.nombre}</strong>
                <small>{medicamento.presentacion ?? 'Sin presentacion'}</small>
                <EtiquetaEstado tono={medicamento.activo ? 'success' : 'neutral'}>
                  {medicamento.activo ? 'Activo' : 'Inactivo'}
                </EtiquetaEstado>
              </span>
              <ChevronRight size={20} aria-hidden="true" />
            </button>
          ) : (
            <div className="tarjeta-residente">
              <span className="tarjeta-residente-info">
                <strong>{medicamento.nombre}</strong>
                <small>{medicamento.presentacion ?? 'Sin presentacion'}</small>
                <EtiquetaEstado tono={medicamento.activo ? 'success' : 'neutral'}>
                  {medicamento.activo ? 'Activo' : 'Inactivo'}
                </EtiquetaEstado>
              </span>
            </div>
          )}
        </li>
      ))}
    </ul>
  )
}
