// Listado de residentes como tarjetas para movil. Cada tarjeta es un boton
// completo (objetivo tactil amplio) que abre el detalle.

import { ChevronRight } from 'lucide-react'
import type { PacienteResumen } from '@/features/pacientes/types'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'

interface PropsTarjetasListado {
  residentes: PacienteResumen[]
  alAbrir: (id: number) => void
}

/** Tarjetas de residentes (movil); mismo dato que la tabla de escritorio. */
export function TarjetasListado({ residentes, alAbrir }: PropsTarjetasListado) {
  return (
    <ul className="tarjetas-residentes">
      {residentes.map((residente) => (
        <li key={residente.id}>
          <button type="button" className="tarjeta-residente" onClick={() => alAbrir(residente.id)}>
            <span className="tarjeta-residente-info">
              <strong>{residente.nombreCompleto}</strong>
              <small>
                {residente.edad} años · {residente.habitacion ?? 'Sin habitación'}
              </small>
              <EtiquetaEstado tono={residente.activo ? 'success' : 'neutral'}>
                {residente.activo ? 'Activo' : 'Inactivo'}
              </EtiquetaEstado>
            </span>
            <ChevronRight size={20} aria-hidden="true" />
          </button>
        </li>
      ))}
    </ul>
  )
}
