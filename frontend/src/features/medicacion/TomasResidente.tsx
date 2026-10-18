// Pantalla de medicacion de un residente: agenda del dia e historial de tomas.
// Todos los roles con alcance pueden consultarla; registrar tomas esta
// limitado a ADMINISTRADOR y CUIDADOR_ENFERMERO.

import { useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { Paginacion } from '@/components/ui/Paginacion'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeRegistrarTomas } from '@/features/medicacion/permisos'
import { hoyIso } from '@/features/medicacion/fechas'
import { TablaHistorialTomas } from '@/features/medicacion/TablaHistorialTomas'
import { TarjetasHistorialTomas } from '@/features/medicacion/TarjetasHistorialTomas'
import { TarjetaAgendaEsquema } from '@/features/medicacion/TarjetaAgendaEsquema'
import { useAgendaTomas } from '@/features/medicacion/useAgendaTomas'
import { useHistorialTomas } from '@/features/medicacion/useHistorialTomas'
import type {
  AgendaEsquema,
  PaginaTomas,
} from '@/features/medicacion/types'

const TAMANO_HISTORIAL = 10

type Pestana = 'agenda' | 'historial'

/** Pantalla de agenda e historial de tomas de un residente. */
export function TomasResidente() {
  const { id } = useParams()
  const pacienteId = Number(id)
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const puedeRegistrar = puedeRegistrarTomas(usuario?.rol)

  const [pestana, setPestana] = useState<Pestana>('agenda')
  const [fecha, setFecha] = useState(hoyIso())
  const [paginaHistorial, setPaginaHistorial] = useState(0)

  const agenda = useAgendaTomas(pacienteId, fecha)
  const historial = useHistorialTomas(pacienteId, {}, paginaHistorial, TAMANO_HISTORIAL)

  // Mapa esquemaId -> "Medicamento · dosis" usando la agenda del dia; si el
  // historial incluye esquemas fuera del mapa, se cae al id del esquema.
  const nombrePorEsquema = useMemo(() => {
    const mapa: Record<number, string> = {}
    for (const esquema of agenda.agenda as AgendaEsquema[]) {
      const nombre = esquema.medicamentoNombre ?? `Medicamento #${esquema.medicamentoId}`
      mapa[esquema.esquemaId] = `${nombre} · ${esquema.dosis}`
    }
    return mapa
  }, [agenda.agenda])

  if (agenda.noEncontrado || historial.error) {
    return (
      <div className="page-content">
        <EstadoError mensaje="No pudimos cargar la medicacion de este residente." />
        <div className="acciones-detalle">
          <BotonAccion variant="outline" onClick={() => navegar(`/residentes/${pacienteId}`)}>
            Volver al residente
          </BotonAccion>
        </div>
      </div>
    )
  }

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={() => navegar(`/residentes/${pacienteId}`)}>
          <ArrowLeft size={16} /> Volver al residente
        </BotonAccion>
      </div>

      <div className="page-intro">
        <div>
          <p className="eyebrow">Medicacion</p>
          <h2>Tomas del residente</h2>
        </div>
      </div>

      <div className="pestanas" role="tablist" aria-label="Agenda o historial de tomas">
        <button
          type="button"
          role="tab"
          aria-selected={pestana === 'agenda'}
          className={pestana === 'agenda' ? 'selected' : ''}
          onClick={() => setPestana('agenda')}
        >
          Agenda del dia
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={pestana === 'historial'}
          className={pestana === 'historial' ? 'selected' : ''}
          onClick={() => setPestana('historial')}
        >
          Historial
        </button>
      </div>

      {pestana === 'agenda' && (
        <SeccionAgenda
          pacienteId={pacienteId}
          fecha={fecha}
          alCambiarFecha={setFecha}
          agenda={agenda.agenda}
          cargando={agenda.cargando}
          error={agenda.error}
          puedeRegistrar={puedeRegistrar}
          alRegistrar={agenda.recargar}
        />
      )}

      {pestana === 'historial' && (
        <SeccionHistorial
          datos={historial.datos}
          cargando={historial.cargando}
          error={historial.error}
          nombrePorEsquema={nombrePorEsquema}
          alCambiarPagina={setPaginaHistorial}
          alReintentar={historial.recargar}
        />
      )}
    </div>
  )
}

interface PropsSeccionAgenda {
  pacienteId: number
  fecha: string
  alCambiarFecha: (fecha: string) => void
  agenda: AgendaEsquema[]
  cargando: boolean
  error: string | null
  puedeRegistrar: boolean
  alRegistrar: () => Promise<void>
}

/** Agenda del dia con selector de fecha y registro de tomas. */
function SeccionAgenda({
  pacienteId,
  fecha,
  alCambiarFecha,
  agenda,
  cargando,
  error,
  puedeRegistrar,
  alRegistrar,
}: PropsSeccionAgenda) {
  return (
    <>
      <div className="barra-listado">
        <label className="filtro-rol">
          Fecha
          <input
            type="date"
            value={fecha}
            onChange={(e) => alCambiarFecha(e.target.value)}
          />
        </label>
      </div>

      {cargando && <EstadoCarga mensaje="Cargando agenda..." />}
      {!cargando && error && <EstadoError mensaje={error} />}
      {!cargando && !error && agenda.length === 0 && (
        <EstadoVacio
          titulo="Sin tomas para este dia"
          descripcion="No hay esquemas con tomas programadas en la fecha seleccionada."
        />
      )}
      {!cargando && !error && agenda.length > 0 && (
        <div className="grid-agenda">
          {agenda.map((esquema) => (
            <TarjetaAgendaEsquema
              key={esquema.esquemaId}
              pacienteId={pacienteId}
              agenda={esquema}
              puedeRegistrar={puedeRegistrar}
              alRegistrar={alRegistrar}
            />
          ))}
        </div>
      )}
    </>
  )
}

interface PropsSeccionHistorial {
  datos: PaginaTomas | null
  cargando: boolean
  error: string | null
  nombrePorEsquema: Record<number, string>
  alCambiarPagina: (pagina: number) => void
  alReintentar: () => void
}

/** Historial paginado de tomas registradas. */
function SeccionHistorial({
  datos,
  cargando,
  error,
  nombrePorEsquema,
  alCambiarPagina,
  alReintentar,
}: PropsSeccionHistorial) {
  const tomas = datos?.contenido ?? []

  return (
    <>
      {cargando && <EstadoCarga mensaje="Cargando historial..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <BotonAccion variant="outline" onClick={alReintentar}>
              Reintentar
            </BotonAccion>
          }
        />
      )}
      {!cargando && !error && tomas.length === 0 && (
        <EstadoVacio
          titulo="Sin tomas registradas"
          descripcion="Aun no hay tomas registradas para este residente."
        />
      )}
      {!cargando && !error && tomas.length > 0 && (
        <>
          <div className="listado-escritorio">
            <TablaHistorialTomas tomas={tomas} nombrePorEsquema={nombrePorEsquema} />
          </div>
          <div className="listado-movil">
            <TarjetasHistorialTomas tomas={tomas} nombrePorEsquema={nombrePorEsquema} />
          </div>
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'toma', plural: 'tomas' }}
              etiqueta="Paginacion del historial de tomas"
              alCambiarPagina={alCambiarPagina}
            />
          )}
        </>
      )}
    </>
  )
}
