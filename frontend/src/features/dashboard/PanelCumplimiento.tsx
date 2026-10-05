import { MoreHorizontal } from 'lucide-react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { datosCumplimiento } from '@/features/dashboard/mock-data'

// Subpanel del grafico de barras con el cumplimiento de los ultimos 7 dias.

/** Grafico de cumplimiento diario (citas vs bitacoras). */
export function PanelCumplimiento() {
  return (
    <section className="panel chart-panel">
      <div className="panel-header">
        <div>
          <h3>Cumplimiento por dia</h3>
          <p className="muted">Ultimos 7 dias</p>
        </div>
        <button className="icon-button" aria-label="Mas opciones">
          <MoreHorizontal size={19} />
        </button>
      </div>
      <div className="legend">
        <span>
          <i className="dot teal" />
          Citas
        </span>
        <span>
          <i className="dot gold" />
          Bitacoras
        </span>
      </div>
      <div className="main-chart">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={datosCumplimiento} barGap={5}>
            <CartesianGrid vertical={false} stroke="#ebe7df" />
            <XAxis dataKey="dia" axisLine={false} tickLine={false} tick={{ fill: '#718084', fontSize: 12 }} />
            <YAxis axisLine={false} tickLine={false} tick={{ fill: '#718084', fontSize: 12 }} width={25} />
            <Tooltip cursor={{ fill: '#f5f2ec' }} contentStyle={{ border: '1px solid #ddd7cb', borderRadius: 6, boxShadow: 'none' }} />
            <Bar dataKey="citas" fill="#0e5a64" radius={[3, 3, 0, 0]} barSize={9} />
            <Bar dataKey="bitacoras" fill="#e9b872" radius={[3, 3, 0, 0]} barSize={9} />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </section>
  )
}
