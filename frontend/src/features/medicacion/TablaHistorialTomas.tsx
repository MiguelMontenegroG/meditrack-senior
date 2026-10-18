// Tabla del historial de tomas (escritorio). Solo presenta; los datos llegan
// ya paginados. Muestra la fecha programada, el esquema, el estado y las
// observaciones.

import { EtiquetaEstadoToma } from '@/features/medicacion/EtiquetaEstadoToma'
import { fechaHoraLocal } from '@/features/medicacion/fechas'
import type { RegistroToma } from '@/features/medicacion/types'

interface PropsTablaHistorialTomas {
  tomas: RegistroToma[]
  /** Nombre legible del esquema (medicamento + dosis) por esquemaId. */
  nombrePorEsquema: Record<number, string>
}

/** Tabla accesible del historial de tomas (escritorio). */
export function TablaHistorialTomas({ tomas, nombrePorEsquema }: PropsTablaHistorialTomas) {
  return (
    <div className="table-wrap">
      <table className="tabla-residentes">
        <caption className="sr-only">Historial de tomas</caption>
        <thead>
          <tr>
            <th scope="col">Hora programada</th>
            <th scope="col">Esquema</th>
            <th scope="col">Estado</th>
            <th scope="col">Observaciones</th>
          </tr>
        </thead>
        <tbody>
          {tomas.map((toma) => (
            <tr key={toma.id}>
              <td>{fechaHoraLocal(toma.fechaHoraProgramada)}</td>
              <td>{nombrePorEsquema[toma.esquemaId] ?? `Esquema #${toma.esquemaId}`}</td>
              <td>
                <EtiquetaEstadoToma estado={toma.estado} />
              </td>
              <td>{toma.observaciones ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
