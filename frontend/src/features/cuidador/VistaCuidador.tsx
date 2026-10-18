// Vista del cuidador enfermero: agenda de tomas del dia (pendientes y
// atrasadas) y residentes a su cargo con el conteo de tomas abiertas, todo con
// datos reales (endpoints de pacientes y tomas). La bitacora de signos vitales
// aun no tiene endpoint, asi que se muestra como "proximamente" en lugar de
// cifras de ejemplo.

import { useNavigate } from 'react-router-dom'
import { Check, ChevronRight, Clock3, ClipboardList } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EnConstruccion } from '@/components/ui/EnConstruccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { useTareasCuidador } from '@/features/medicacion/useTareasCuidador'
import { useResidentesAsignados } from '@/features/medicacion/useResidentesAsignados'

/** Pantalla principal del rol CUIDADOR_ENFERMERO. */
export function VistaCuidador() {
  const { usuario } = useAutenticacion()
  const { tareas, total, cargando, error, recargar } = useTareasCuidador()
  const primerNombre = usuario?.nombreCompleto?.split(' ')[0] ?? ''
  const pendientes = tareas.length

  return (
    <div className="page-content caregiver-page">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Jornada de hoy</p>
          <h2>Tu agenda de medicacion</h2>
          <p className="muted">
            {cargando
              ? 'Cargando tus tareas...'
              : pendientes === 0
                ? `Hola, ${primerNombre}. No tienes tomas pendientes.`
                : `Hola, ${primerNombre}. Tienes ${pendientes} ${pendientes === 1 ? 'toma pendiente' : 'tomas pendientes'}.`}
          </p>
        </div>
      </div>

      <ResumenJornada total={total} pendientes={pendientes} cargando={cargando} />
      <ListaTareas
        tareas={tareas}
        cargando={cargando}
        error={error}
        alReintentar={() => void recargar()}
      />
      <ResidentesAsignados />
      <section className="panel vitals-quick">
        <div className="panel-header">
          <div>
            <h3>Bitacora de signos vitales</h3>
            <p className="muted">Proximamente: disponible cuando exista el modulo de bitacora.</p>
          </div>
          <span className="kpi-icon kpi-blue">
            <ClipboardList size={18} />
          </span>
        </div>
        <EnConstruccion titulo="Bitacora" />
      </section>
    </div>
  )
}

interface PropsResumenJornada {
  total: number
  pendientes: number
  cargando: boolean
}

/** Resumen compacto del avance de la jornada. */
function ResumenJornada({ total, pendientes, cargando }: PropsResumenJornada) {
  const completadas = Math.max(total - pendientes, 0)
  const porcentaje = total > 0 ? Math.round((completadas / total) * 100) : 100
  return (
    <div className="care-summary">
      <div>
        <span className="progress-ring">{cargando ? '--' : `${porcentaje}%`}</span>
        <span>
          <strong>{cargando ? '--' : `${completadas} de ${total} tomas`}</strong>
          <small>completadas hoy</small>
        </span>
      </div>
      <div className="summary-divider" />
      <div>
        <strong>{cargando ? '--' : pendientes}</strong>
        <small>tomas pendientes</small>
      </div>
    </div>
  )
}

interface PropsListaTareas {
  tareas: ReturnType<typeof useTareasCuidador>['tareas']
  cargando: boolean
  error: string | null
  alReintentar: () => void
}

/** Lista de tomas pendientes y atrasadas ordenadas por hora. */
function ListaTareas({ tareas, cargando, error, alReintentar }: PropsListaTareas) {
  const navegar = useNavigate()

  return (
    <section className="panel task-panel">
      <div className="panel-header">
        <div>
          <h3>Tomas pendientes</h3>
          <p className="muted">Ordenadas por hora</p>
        </div>
      </div>

      {cargando && <EstadoCarga mensaje="Cargando tareas..." />}
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
      {!cargando && !error && tareas.length === 0 && (
        <EstadoVacio
          titulo="Sin tomas pendientes"
          descripcion="Todas las tomas de hoy ya estan registradas."
        />
      )}
      {!cargando && !error && tareas.length > 0 && (
        <div className="task-list">
          {tareas.map((tarea) => (
            <div
              className={`task-row ${tarea.estado === 'ATRASADA' ? 'task-urgent' : ''}`}
              key={`${tarea.pacienteId}-${tarea.esquemaId}-${tarea.fechaHoraProgramada}`}
            >
              <time>{tarea.hora}</time>
              <span className="task-icon info">
                <Clock3 size={18} />
              </span>
              <div className="task-copy">
                <strong>{tarea.medicamentoNombre}</strong>
                <span>
                  {tarea.pacienteNombre}
                  {tarea.habitacion ? ` - Hab. ${tarea.habitacion}` : ''}
                </span>
                <small>
                  {tarea.dosis} - {tarea.estado === 'ATRASADA' ? 'Atrasada' : 'Pendiente'}
                </small>
              </div>
              <button
                type="button"
                className="task-check"
                aria-label={`Abrir la agenda de ${tarea.pacienteNombre}`}
                onClick={() => navegar(`/residentes/${tarea.pacienteId}/tomas`)}
              >
                <Check size={18} />
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

/** Residentes a cargo del cuidador con sus tomas abiertas de hoy. */
function ResidentesAsignados() {
  const navegar = useNavigate()
  const { residentes, cargando, error, recargar } = useResidentesAsignados()

  return (
    <section className="panel">
      <div className="panel-header">
        <div>
          <h3>Residentes a tu cargo</h3>
          <p className="muted">Con sus tomas abiertas de hoy</p>
        </div>
      </div>

      {cargando && <EstadoCarga mensaje="Cargando residentes..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <BotonAccion variant="outline" onClick={() => void recargar()}>
              Reintentar
            </BotonAccion>
          }
        />
      )}
      {!cargando && !error && residentes.length === 0 && (
        <EstadoVacio
          titulo="Sin residentes asignados"
          descripcion="Aun no tienes residentes asignados con tomas abiertas."
        />
      )}
      {!cargando && !error && residentes.length > 0 && (
        <ul className="task-list">
          {residentes.map((residente) => (
            <li key={residente.pacienteId} className="task-row">
              <div className="task-copy">
                <strong>{residente.pacienteNombre}</strong>
                <span>{residente.habitacion ? `Hab. ${residente.habitacion}` : 'Sin habitacion'}</span>
                <small>
                  {residente.abiertas} {residente.abiertas === 1 ? 'toma abierta' : 'tomas abiertas'}
                  {residente.atrasadas > 0 ? ` (${residente.atrasadas} atrasadas)` : ''}
                </small>
              </div>
              <button
                type="button"
                className="row-action"
                aria-label={`Abrir la agenda de ${residente.pacienteNombre}`}
                onClick={() => navegar(`/residentes/${residente.pacienteId}/tomas`)}
              >
                <ChevronRight size={17} />
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
