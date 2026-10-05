import { AlertTriangle, Check, ChevronRight } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { alertasActivas } from '@/features/dashboard/mock-data'

// Subpanel de alertas activas del panel de administrador.

/** Lista de alertas activas que requieren seguimiento. */
export function PanelAlertas() {
  return (
    <section className="panel alerts-panel">
      <div className="panel-header">
        <div>
          <h3>Alertas activas</h3>
          <p className="muted">Requieren seguimiento del equipo</p>
        </div>
        <button className="text-link">
          Ver todas <ChevronRight size={15} />
        </button>
      </div>
      <div className="alert-list">
        {alertasActivas.map((alerta) => (
          <div className="alert-row" key={alerta.tipo}>
            <span className={`alert-symbol ${alerta.tono}`}>
              <AlertTriangle size={17} />
            </span>
            <div className="alert-copy">
              <strong>{alerta.tipo}</strong>
              <span>{alerta.residente}</span>
            </div>
            <div className="alert-meta">
              <span>{alerta.momento}</span>
              <EtiquetaEstado tono={alerta.tono}>{alerta.severidad}</EtiquetaEstado>
            </div>
            <button className="row-action" aria-label={`Atender alerta de ${alerta.residente}`}>
              <ChevronRight size={17} />
            </button>
          </div>
        ))}
      </div>
      <div className="panel-footer">
        <button className="outline-link">
          Marcar todas como revisadas <Check size={15} />
        </button>
      </div>
    </section>
  )
}
