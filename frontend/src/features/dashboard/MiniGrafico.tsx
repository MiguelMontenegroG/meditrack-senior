import { Line, LineChart, ResponsiveContainer } from 'recharts'

// Mini grafica de tendencia usada dentro de cada indicador KPI. La serie la
// aporta quien la invoca; si no hay serie real, el KPI no la dibuja.

interface PropsMiniGrafico {
    color?: string
    /** Valores de la serie (al menos 2 para trazar una linea). */
    valores: number[]
}

/** Grafico de linea compacto para los KPI. */
export function MiniGrafico({ color = '#0E5A64', valores }: PropsMiniGrafico) {
    const datos = valores.map((valor) => ({ valor }))
    return (
        <div className="spark">
            <ResponsiveContainer width="100%" height="100%">
                <LineChart data={datos}>
                    <Line type="monotone" dataKey="valor" stroke={color} strokeWidth={2} dot={false} />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}
