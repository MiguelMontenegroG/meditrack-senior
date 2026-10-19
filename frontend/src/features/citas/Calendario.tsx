// Pantalla del calendario de citas (los tres roles). Rango por defecto: del dia
// actual a +30 dias (siempre se envia desde/hasta). Filtros: rango, estado y
// residente del alcance. Agrupado por dia, paginado. Solo el ADMINISTRADOR ve
// el boton para agendar; las acciones por fila respetan el rol.

import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { Paginacion } from '@/components/ui/Paginacion'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeAgendarCitas } from '@/features/citas/permisos'
import { hoyBogota, sumarDias } from '@/features/citas/fechas'
import {
  FiltroEstadoCita,
  RangoFechas,
  SelectorResidente,
} from '@/features/citas/ControlesCalendario'
import { ListadoCalendario } from '@/features/citas/ListadoCalendario'
import { useCalendarioCitas } from '@/features/citas/useCalendarioCitas'
import { useResidentesAlcance } from '@/features/citas/useResidentesAlcance'
import type { CitaDetalle, EstadoCita } from '@/features/citas/types'

const TAMANO_PAGINA = 20

/** Pantalla del calendario de citas. */
export function Calendario() {
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const [params] = useSearchParams()
  const puedeAgendar = puedeAgendarCitas(usuario?.rol)

  const [desde, setDesde] = useState(() => hoyBogota())
  const [hasta, setHasta] = useState(() => sumarDias(hoyBogota(), 30))
  const [estado, setEstado] = useState<EstadoCita | ''>('')
  const [pacienteId, setPacienteId] = useState<number | ''>(() => {
    const inicial = Number(params.get('pacienteId'))
    return Number.isFinite(inicial) && inicial > 0 ? inicial : ''
  })
  const [pagina, setPagina] = useState(0)

  // Al cambiar cualquier filtro se vuelve a la primera pagina.
  useEffect(() => {
    setPagina(0)
  }, [desde, hasta, estado, pacienteId])

  const { residentes } = useResidentesAlcance()
  const nombrePorId = useMemo(() => {
    const mapa = new Map<number, string>()
    for (const residente of residentes) mapa.set(residente.id, residente.nombreCompleto)
    return mapa
  }, [residentes])
  const nombrePorResidente = (id: number) => nombrePorId.get(id) ?? `Residente #${id}`


  const { datos, cargando, error, recargar } = useCalendarioCitas({
    desde,
    hasta,
    estado: estado || undefined,
    pacienteId: pacienteId === '' ? undefined : pacienteId,
    pagina,
    tamano: TAMANO_PAGINA,
  })

  const citas = useMemo(() => datos?.contenido ?? [], [datos])
  const agendar = () => navegar('/calendario/nuevo')

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Agenda</p>
          <h2>Calendario de citas</h2>
          <p className="muted">Del dia actual a los proximos 30 dias.</p>
        </div>
        {puedeAgendar && (
          <BotonAccion onClick={agendar}>
            <Plus size={17} /> Agendar cita
          </BotonAccion>
        )}
      </div>

      <div className="barra-listado filtros-calendario">
        <RangoFechas
          desde={desde}
          hasta={hasta}
          alCambiarDesde={setDesde}
          alCambiarHasta={setHasta}
        />
        <FiltroEstadoCita valor={estado} alCambiar={setEstado} />
        <SelectorResidente valor={pacienteId} residentes={residentes} alCambiar={setPacienteId} />
      </div>

      {cargando && <EstadoCarga mensaje="Cargando citas..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <BotonAccion variant="outline" onClick={recargar}>
              Reintentar
            </BotonAccion>
          }
        />
      )}
      {!cargando && !error && citas.length === 0 && (
        <EstadoVacio
          titulo="No hay citas"
          descripcion="No se encontraron citas en el rango y los filtros seleccionados."
        />
      )}
      {!cargando && !error && citas.length > 0 && (
        <>
          <ListadoCalendario
            citas={citas}
            nombrePorResidente={nombrePorResidente}
            rol={usuario?.rol}
            alEditar={(cita: CitaDetalle) =>
              navegar(`/calendario/${cita.id}/editar`, { state: { cita } })
            }
            alCambiarEstado={recargar}
          />
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'cita', plural: 'citas' }}
              etiqueta="Paginacion de citas"
              alCambiarPagina={setPagina}
            />
          )}
        </>
      )}
    </div>
  )
}