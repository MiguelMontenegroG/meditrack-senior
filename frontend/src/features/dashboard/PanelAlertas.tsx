import { useNavigate } from 'react-router-dom'
import { AlertTriangle, ChevronRight } from 'lucide-react'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import type { TomaAbierta } from '@/features/dashboard/useResumenOperativo'

// Subpanel de alertas activas del panel de administrador. Solo muestra alertas
// reales: las dosis pendientes o atrasadas calculadas con la agenda de hoy.

/** Maximo de tomas abiertas que se listan; el resto se resume en el pie. */
const MAX_ALERTAS = 4

interface PropsPanelAlertas {
  tomasAbiertas: TomaAbierta[]
  cargando: boolean
}

/** Lista de alertas de medicacion reales del dia. */
export function PanelAlertas({ tomasAbiertas, cargando }: PropsPanelAlertas) {
  const navegar = useNavigate()
  const visibles = tomasAbiertas.slice(0, MAX_ALERTAS)
  const ocultas = tomasAbiertas.length - visibles.length

  return (
    <section className="panel alerts-panel">
      <div className="panel-header">
        <div>
          <h3>Alertas activas</h3>
          <p className="muted">
            {cargando
              ? 'Calculando dosis pendientes...'
              : tomasAbiertas.length === 0
                ? 'Sin dosis pendientes ni atrasadas'
                : `${tomasAbiertas.length} ${tomasAbiertas.length === 1 ? 'dosis abierta' : 'dosis abiertas'} hoy`}
          </p>
        </div>
      </div>
      <div className="alert-list">
        {cargando && <p className="muted panel-vacio">Cargando alertas...</p>}
        {!cargando && tomasAbiertas.length === 0 && (
          <EstadoVacio
            titulo="Sin alertas"
            descripcion="No hay dosis pendientes ni atrasadas hoy."
          />
        )}
        {visibles.map((toma) => {
          const atrasada = toma.estado === 'ATRASADA'
          const tono = atrasada ? 'danger' : 'warning'
          return (
            <div
              className="alert-row"
              key={`${toma.pacienteId}-${toma.hora}-${toma.medicamentoNombre}`}
            >
              <span className={`alert-symbol ${tono}`}>
                <AlertTriangle size={17} />
              </span>
              <div className="alert-copy">
                <strong>
                  {atrasada ? 'Dosis atrasada' : 'Dosis pendiente'} - {toma.medicamentoNombre}
                </strong>
                <span>{toma.pacienteNombre}</span>
              </div>
              <div className="alert-meta">
                <span>
                  {atrasada ? 'Programada a las ' : 'Hasta las '}
                  {toma.hora}
                </span>
                <EtiquetaEstado tono={tono}>{atrasada ? 'Alta' : 'Media'}</EtiquetaEstado>
              </div>
              <button
                type="button"
                className="row-action"
                aria-label={`Abrir la agenda de ${toma.pacienteNombre}`}
                onClick={() => navegar(`/residentes/${toma.pacienteId}/tomas`)}
              >
                <ChevronRight size={17} />
              </button>
            </div>
          )
        })}
      </div>
      {!cargando && ocultas > 0 && (
        <div className="panel-footer">
          <p className="muted">
            {ocultas === 1
              ? '1 dosis mas abierta hoy.'
              : `${ocultas} dosis mas abiertas hoy.`}{' '}
            Revisalas en la agenda de cada residente.
          </p>
        </div>
      )}
    </section>
  )
}
