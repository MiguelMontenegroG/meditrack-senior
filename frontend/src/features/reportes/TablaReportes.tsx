// Tabla del historial de reportes generados de un residente. Muestra tipo,
// periodo y fecha de generacion. El PDF no se almacena: no hay enlace de
// descarga del historial, solo metadatos (el PDF se regenera desde el
// formulario). Respeta tablas accesibles con caption.

import { ETIQUETA_TIPO_REPORTE } from '@/features/reportes/etiquetas'
import type { ReporteGeneradoResumen } from '@/features/reportes/types'
import { fechaBogota, fechaHoraBogota } from '@/lib/fechas-bogota'

interface PropsTablaReportes {
  reportes: ReporteGeneradoResumen[]
}

/** Tabla de reportes generados (escritorio). */
export function TablaReportes({ reportes }: PropsTablaReportes) {
  return (
    <div className="table-wrap">
      <table className="tabla-reportes">
        <caption className="sr-only">Reportes generados para este residente</caption>
        <thead>
          <tr>
            <th scope="col">Tipo</th>
            <th scope="col">Periodo</th>
            <th scope="col">Generado</th>
          </tr>
        </thead>
        <tbody>
          {reportes.map((reporte) => (
            <tr key={reporte.id}>
              <td>
                <strong>{ETIQUETA_TIPO_REPORTE[reporte.tipo]}</strong>
              </td>
              <td>
                {fechaBogota(reporte.rangoDesde)} - {fechaBogota(reporte.rangoHasta)}
              </td>
              <td>
                <time dateTime={reporte.creadoEn}>{fechaHoraBogota(reporte.creadoEn)}</time>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
