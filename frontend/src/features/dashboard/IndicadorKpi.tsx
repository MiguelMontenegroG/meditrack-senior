import { ArrowUpRight, ClipboardCheck, Clock3, FileBarChart, HeartPulse } from 'lucide-react'
import { MiniGrafico } from '@/features/dashboard/MiniGrafico'
import type { IndicadorKpiDatos } from '@/features/dashboard/types'

// Tarjeta de indicador clave (KPI) con icono, valor, tendencia y mini grafica.

const ICONOS_KPI = {
  primary: HeartPulse,
  green: ClipboardCheck,
  warm: Clock3,
  blue: FileBarChart,
} as const

const COLORES_LINEA: Record<IndicadorKpiDatos['tono'], string> = {
  primary: '#0E5A64',
  green: '#2F7D5A',
  warm: '#A96B0B',
  blue: '#2C5F8A',
}

/** Tarjeta de KPI del panel de administrador. */
export function IndicadorKpi({ etiqueta, valor, detalle, tendencia, tono = 'primary' }: IndicadorKpiDatos) {
  const Icono = ICONOS_KPI[tono]
  return (
    <section className="kpi-card">
      <div className="kpi-top">
        <span>{etiqueta}</span>
        <span className={`kpi-icon kpi-${tono}`}>
          <Icono size={18} />
        </span>
      </div>
      <strong>{valor}</strong>
      <div className="kpi-bottom">
        <span className="trend">
          <ArrowUpRight size={13} />
          {tendencia}
        </span>
        <span>{detalle}</span>
      </div>
      <MiniGrafico color={COLORES_LINEA[tono]} />
    </section>
  )
}
