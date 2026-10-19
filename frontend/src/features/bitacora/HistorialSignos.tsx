// Historial de signos vitales de un residente: tabla en escritorio y tarjetas
// en movil, con el mismo dato. Incluye el filtro de rango de fechas y la
// paginacion. Los registros son inmutables: no hay acciones de editar ni
// borrar. Los valores no registrados se muestran como un guion.

import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { Paginacion } from '@/components/ui/Paginacion'
import { fechaHoraBogota } from '@/lib/fechas-bogota'
import {
  textoGlucosa,
  textoPresion,
  textoTemperatura,
} from '@/features/bitacora/signosVitales'
import type { SignosVitales } from '@/features/bitacora/types'

interface PropsFiltroRango {
  desde: string
  hasta: string
  alCambiarDesde: (valor: string) => void
  alCambiarHasta: (valor: string) => void
  alLimpiar: () => void
}

/** Filtro por rango de fechas (ambos extremos opcionales). */
export function FiltroRangoSignos({
  desde,
  hasta,
  alCambiarDesde,
  alCambiarHasta,
  alLimpiar,
}: PropsFiltroRango) {
  return (
    <div className="barra-listado">
      <fieldset className="filtro-rango">
        <legend className="sr-only">Rango de fechas del historial</legend>
        <label>
          Desde
          <input
            type="date"
            value={desde}
            max={hasta || undefined}
            onChange={(evento) => alCambiarDesde(evento.target.value)}
          />
        </label>
        <label>
          Hasta
          <input
            type="date"
            value={hasta}
            min={desde || undefined}
            onChange={(evento) => alCambiarHasta(evento.target.value)}
          />
        </label>
      </fieldset>
      {(desde || hasta) && (
        <BotonAccion variant="outline" onClick={alLimpiar}>
          Limpiar rango
        </BotonAccion>
      )}
    </div>
  )
}

/** Tres textos compactos con los valores clinicos de un registro. */
function ResumenSignos({ registro }: { registro: SignosVitales }) {
  const presion = textoPresion(registro)
  const glucosa = textoGlucosa(registro)
  const temperatura = textoTemperatura(registro)
  return (
    <>
      <span>{presion ?? 'Presion —'}</span>
      <span>{glucosa ?? 'Glucosa —'}</span>
      <span>{temperatura ?? 'Temperatura —'}</span>
    </>
  )
}

interface PropsTablaSignos {
  registros: SignosVitales[]
}

/** Tabla accesible del historial (escritorio). */
function TablaSignos({ registros }: PropsTablaSignos) {
  return (
    <div className="table-wrap">
      <table className="tabla-residentes tabla-signos">
        <caption className="sr-only">Historial de signos vitales</caption>
        <thead>
          <tr>
            <th scope="col">Fecha y hora</th>
            <th scope="col">Presion</th>
            <th scope="col">Glucosa</th>
            <th scope="col">Temperatura</th>
            <th scope="col">Observaciones</th>
          </tr>
        </thead>
        <tbody>
          {registros.map((registro) => (
            <tr key={registro.id}>
              <td>{fechaHoraBogota(registro.fechaHora)}</td>
              <td>{textoPresion(registro) ?? '—'}</td>
              <td>{textoGlucosa(registro) ?? '—'}</td>
              <td>{textoTemperatura(registro) ?? '—'}</td>
              <td className="celda-motivo">{registro.observaciones ?? '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

/** Tarjetas del historial (movil). */
function TarjetasSignos({ registros }: PropsTablaSignos) {
  return (
    <ul className="tarjetas-residentes">
      {registros.map((registro) => (
        <li key={registro.id} className="tarjeta-residente tarjeta-signos">
          <span className="tarjeta-residente-info">
            <strong>{fechaHoraBogota(registro.fechaHora)}</strong>
            <small>
              <ResumenSignos registro={registro} />
            </small>
            {registro.observaciones && <small>{registro.observaciones}</small>}
          </span>
        </li>
      ))}
    </ul>
  )
}

interface PropsHistorialSignos {
  registros: SignosVitales[]
  total: number
  pagina: number
  totalPaginas: number
  cargando: boolean
  error: string | null
  alCambiarPagina: (pagina: number) => void
  alReintentar: () => void
}

/** Seccion del historial con sus estados de carga, error y vacio. */
export function HistorialSignos({
  registros,
  total,
  pagina,
  totalPaginas,
  cargando,
  error,
  alCambiarPagina,
  alReintentar,
}: PropsHistorialSignos) {
  if (cargando) return <EstadoCarga mensaje="Cargando signos vitales..." />

  if (error) {
    return (
      <EstadoError
        mensaje={error}
        accion={
          <BotonAccion variant="outline" onClick={alReintentar}>
            Reintentar
          </BotonAccion>
        }
      />
    )
  }

  if (registros.length === 0) {
    return (
      <EstadoVacio
        titulo="Sin registros de signos vitales"
        descripcion="Aun no hay mediciones para este residente en el rango seleccionado."
      />
    )
  }

  return (
    <>
      <div className="listado-escritorio">
        <TablaSignos registros={registros} />
      </div>
      <div className="listado-movil">
        <TarjetasSignos registros={registros} />
      </div>
      {totalPaginas > 1 && (
        <Paginacion
          pagina={pagina}
          totalPaginas={totalPaginas}
          total={total}
          sustantivo={{ singular: 'registro', plural: 'registros' }}
          etiqueta="Paginacion del historial de signos vitales"
          alCambiarPagina={alCambiarPagina}
        />
      )}
    </>
  )
}
