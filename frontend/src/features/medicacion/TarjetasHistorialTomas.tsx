// Tarjetas del historial de tomas (movil); mismo dato que la tabla.

import { EtiquetaEstadoToma } from '@/features/medicacion/EtiquetaEstadoToma'
import { fechaHoraLocal } from '@/features/medicacion/fechas'
import type { RegistroToma } from '@/features/medicacion/types'

interface PropsTarjetasHistorialTomas {
  tomas: RegistroToma[]
  nombrePorEsquema: Record<number, string>
}

/** Tarjetas del historial de tomas (movil). */
export function TarjetasHistorialTomas({ tomas, nombrePorEsquema }: PropsTarjetasHistorialTomas) {
  return (
    <ul className="tarjetas-residentes">
      {tomas.map((toma) => (
        <li key={toma.id} className="tarjeta-residente">
          <span className="tarjeta-residente-info">
            <strong>{nombrePorEsquema[toma.esquemaId] ?? `Esquema #${toma.esquemaId}`}</strong>
            <small>{fechaHoraLocal(toma.fechaHoraProgramada)}</small>
            <EtiquetaEstadoToma estado={toma.estado} />
            {toma.observaciones && <small>{toma.observaciones}</small>}
          </span>
        </li>
      ))}
    </ul>
  )
}
