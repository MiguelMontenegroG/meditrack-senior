// Tabla de alertas para escritorio. Cada fila muestra tipo, mensaje,
// severidad, estado, fecha y (si aplica) el boton "Atender". Solo presenta:
// recibe las alertas y callbacks desde la pantalla.

import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import {
  ETIQUETA_ESTADO_ALERTA,
  ETIQUETA_SEVERIDAD,
  ETIQUETA_TIPO_ALERTA,
  TONO_ESTADO_ALERTA,
  TONO_SEVERIDAD,
} from '@/features/alertas/etiquetas'
import type { Alerta } from '@/features/alertas/types'
import { fechaHoraBogota } from '@/lib/fechas-bogota'

interface PropsTablaAlertas {
  alertas: Alerta[]
  puedeAtender: boolean
  alAtender: (id: number) => void
  /** Id de la alerta en proceso de atencion (bloquea su boton). */
  atendiendoId: number | null
}

/** Tabla de alertas (escritorio). */
export function TablaAlertas({ alertas, puedeAtender, alAtender, atendiendoId }: PropsTablaAlertas) {
  return (
    <div className="table-wrap">
      <table className="tabla-alertas">
        <caption className="sr-only">Listado de alertas</caption>
        <thead>
          <tr>
            <th scope="col">Tipo</th>
            <th scope="col">Detalle</th>
            <th scope="col">Severidad</th>
            <th scope="col">Estado</th>
            <th scope="col">Fecha</th>
            {puedeAtender && (
              <th scope="col">
                <span className="sr-only">Acciones</span>
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {alertas.map((alerta) => {
            const mostrarBoton = puedeAtender && alerta.estado === 'ACTIVA'
            return (
              <tr key={alerta.id}>
                <td>
                  <strong>{ETIQUETA_TIPO_ALERTA[alerta.tipo]}</strong>
                </td>
                <td>{alerta.mensaje}</td>
                <td>
                  <EtiquetaEstado tono={TONO_SEVERIDAD[alerta.severidad]}>
                    {ETIQUETA_SEVERIDAD[alerta.severidad]}
                  </EtiquetaEstado>
                </td>
                <td>
                  <EtiquetaEstado tono={TONO_ESTADO_ALERTA[alerta.estado]}>
                    {ETIQUETA_ESTADO_ALERTA[alerta.estado]}
                  </EtiquetaEstado>
                </td>
                <td>
                  <time dateTime={alerta.creadoEn}>{fechaHoraBogota(alerta.creadoEn)}</time>
                </td>
                {puedeAtender && (
                  <td>
                    {mostrarBoton && (
                      <button
                        type="button"
                        className="btn btn-outline"
                        onClick={() => alAtender(alerta.id)}
                        disabled={atendiendoId === alerta.id}
                      >
                        {atendiendoId === alerta.id ? 'Atendiendo...' : 'Atender'}
                      </button>
                    )}
                  </td>
                )}
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
