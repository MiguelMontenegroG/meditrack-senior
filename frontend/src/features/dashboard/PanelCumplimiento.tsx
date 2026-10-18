import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { PuntoCumplimiento } from '@/features/dashboard/types'

// Subpanel del grafico de barras con las tomas programadas. El backend no
// expone un historico en lote; la unica medida real disponible es la agenda de
// hoy, por lo que el subtitulo lo advierte en lugar de simular una tendencia.

interface PropsPanelCumplimiento {
  serie: PuntoCumplimiento[]
  cargando: boolean
}

/** Grafico de tomas programadas por dia (real solo para hoy). */
export function PanelCumplimiento({ serie, cargando }: PropsPanelCumplimiento) {
  const totalHoy = serie.find((punto) => punto.dia === 'Hoy')?.tomas ?? 0

  return (
    <section className="panel chart-panel">
      <div className="panel-header">
        <div>
          <h3>Tomas programadas</h3>
          <p className="muted">
            {cargando
              ? 'Calculando agenda de hoy...'
              : `Hoy: ${totalHoy} ${totalHoy === 1 ? 'toma' : 'tomas'} en residentes activos. El historico por dia aun no tiene endpoint.`}
          </p>
        </div>
      </div>
      <div className="legend">
        <span>
          <i className="dot teal" />
          Tomas programadas
        </span>
      </div>
      <div className="main-chart">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={serie} barGap={5}>
            <CartesianGrid vertical={false} stroke="#ebe7df" />
            <XAxis
              dataKey="dia"
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#718084', fontSize: 12 }}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fill: '#718084', fontSize: 12 }}
              width={25}
              allowDecimals={false}
            />
            <Tooltip
              cursor={{ fill: '#f5f2ec' }}
              contentStyle={{
                border: '1px solid #ddd7cb',
                borderRadius: 6,
                boxShadow: 'none',
              }}
              formatter={(valor) => [`${valor}`, 'Tomas programadas']}
            />
            <Bar dataKey="tomas" fill="#0e5a64" radius={[3, 3, 0, 0]} barSize={14} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}
