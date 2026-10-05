import { Line, LineChart, ResponsiveContainer } from 'recharts'
import { serieMiniGrafico } from '@/features/dashboard/mock-data'

// Mini grafica de tendencia usada dentro de cada indicador KPI.

interface PropsMiniGrafico {
  color?: string
}

/** Grafico de linea compacto para los KPI. */
export function MiniGrafico({ color = '#0E5A64' }: PropsMiniGrafico) {
  return (
    <div className="spark">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={serieMiniGrafico}>
          <Line type="monotone" dataKey="valor" stroke={color} strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
