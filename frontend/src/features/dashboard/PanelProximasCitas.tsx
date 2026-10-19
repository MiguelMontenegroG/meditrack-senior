// Panel del dashboard con las proximas citas programadas (datos reales de
// GET /api/citas). Si falla o no hay citas se informa sin romper el panel.

import { Link } from 'react-router-dom'
import { CalendarDays, ChevronRight } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { fechaHoraBogota } from '@/features/citas/fechas'
import { useProximasCitasPanel } from '@/features/dashboard/useProximasCitasPanel'

/** Panel de proximas citas programadas para el administrador. */
export function PanelProximasCitas() {
  const { citas, nombrePorResidente, cargando, error, recargar } = useProximasCitasPanel()

  return (
    <section className="panel panel-proximas-citas">
      <div className="panel-header">
        <div>
          <h3>Proximas citas</h3>
          <p className="muted">Programadas en los proximos 30 dias</p>
        </div>
        <span className="kpi-icon kpi-blue">
          <CalendarDays size={18} />
        </span>
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
        <p className="muted panel-vacio">No hay citas programadas proximas.</p>
      )}
      {!cargando && !error && citas.length > 0 && (
        <ul className="alert-list lista-citas-panel">
          {citas.map((cita) => (
            <li key={cita.id} className="alert-row">
              <span className="alert-symbol info">
                <CalendarDays size={16} />
              </span>
              <div className="alert-copy">
                <strong>{nombrePorResidente(cita.pacienteId)}</strong>
                <span>
                  {cita.especialidadNombre ?? 'Visita general'}
                  {cita.profesional ? ` · ${cita.profesional}` : ''}
                </span>
              </div>
              <div className="alert-meta">
                <span>{fechaHoraBogota(cita.fechaHora)}</span>
              </div>
            </li>
          ))}
        </ul>
      )}

      <div className="panel-footer">
        <Link className="text-link" to="/calendario">
          Ver calendario <ChevronRight size={15} />
        </Link>
      </div>
    </section>
  )
}