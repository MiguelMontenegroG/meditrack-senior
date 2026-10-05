import { useState } from 'react'
import { IndicadorKpi } from '@/features/dashboard/IndicadorKpi'
import { PanelAlertas } from '@/features/dashboard/PanelAlertas'
import { PanelCumplimiento } from '@/features/dashboard/PanelCumplimiento'
import { TablaResidentes } from '@/features/dashboard/TablaResidentes'
import { indicadoresKpi } from '@/features/dashboard/mock-data'

// Pantalla del panel de administrador. Compone los KPI y los subpaneles;
// el unico estado local es el periodo de tiempo seleccionado.

const PERIODOS = ['Hoy', '7 dias', '30 dias']

/** Panel principal para el rol ADMINISTRADOR. */
export function PanelAdministrador() {
  const [periodo, setPeriodo] = useState('7 dias')

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Resumen operativo</p>
          <h2>Buenos dias, Mariana</h2>
          <p className="muted">
            Esto es lo mas importante para el cuidado de hoy, 27 de septiembre de 2026.
          </p>
        </div>
        <div className="period-select">
          {PERIODOS.map((opcion) => (
            <button
              key={opcion}
              className={periodo === opcion ? 'selected' : ''}
              onClick={() => setPeriodo(opcion)}
            >
              {opcion}
            </button>
          ))}
        </div>
      </div>

      <div className="kpi-grid">
        {indicadoresKpi.map((indicador) => (
          <IndicadorKpi key={indicador.etiqueta} {...indicador} />
        ))}
      </div>

      <div className="dashboard-grid">
        <PanelAlertas />
        <PanelCumplimiento />
      </div>

      <TablaResidentes />
    </div>
  )
}
