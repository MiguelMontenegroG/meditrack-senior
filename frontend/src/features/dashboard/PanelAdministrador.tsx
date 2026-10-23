import { useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { IndicadorKpi } from '@/features/dashboard/IndicadorKpi'
import { PanelAlertas } from '@/features/dashboard/PanelAlertas'
import { PanelCumplimiento } from '@/features/dashboard/PanelCumplimiento'
import { PanelProximasCitas } from '@/features/dashboard/PanelProximasCitas'
import { useResumenOperativo } from '@/features/dashboard/useResumenOperativo'
import type { IndicadorKpiDatos } from '@/features/dashboard/types'

// Pantalla del panel de administrador. Todos los KPI salen de endpoints reales
// (residentes, medicamentos y agenda de tomas); lo que depende de modulos
// inexistentes (citas, bitacora, reportes) se muestra como "en construccion"
// en lugar de cifras de ejemplo.

/** Saludo segun la hora del dia del navegador. */
function saludoDelDia(): string {
  const hora = new Date().getHours()
  if (hora < 12) return 'Buenos dias'
  if (hora < 19) return 'Buenas tardes'
  return 'Buenas noches'
}

/** Fecha de hoy en espanol (p. ej. "viernes, 17 de octubre de 2026"). */
function fechaDeHoy(): string {
  return new Date().toLocaleDateString('es-CO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
}

/** Panel principal para el rol ADMINISTRADOR. */
export function PanelAdministrador() {
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const primerNombre = usuario?.nombreCompleto?.trim().split(/\s+/)[0] ?? ''
  const fecha = useMemo(() => fechaDeHoy(), [])
  const {
    resumen,
    totalResidentes,
    totalMedicamentos,
    avance,
    serie,
    tomasAbiertas,
    cargando,
    error,
    recargar,
  } = useResumenOperativo()

  // Los cuatro KPI usan solo datos que existen hoy. Sin serie historica no se
  // dibuja tendencia ni se inventa una variacion.
  const indicadores: IndicadorKpiDatos[] = [
    {
      etiqueta: 'Medicacion de hoy',
      valor: cargando ? '--' : `${avance}%`,
      detalle: cargando
        ? 'Cargando agenda...'
        : `${resumen.tomasCerradas} de ${resumen.tomasTotales} tomas registradas`,
      tendencia: '',
      tono: 'primary',
      serie: [],
    },
    {
      etiqueta: 'Tomas abiertas',
      valor: cargando ? '--' : String(tomasAbiertas.length),
      detalle: 'pendientes o atrasadas hoy',
      tendencia: '',
      tono: 'warm',
      serie: [],
    },
    {
      etiqueta: 'Residentes activos',
      valor: cargando ? '--' : String(totalResidentes),
      detalle: 'en el centro',
      tendencia: '',
      tono: 'green',
      serie: [],
    },
    {
      etiqueta: 'Medicamentos',
      valor: cargando ? '--' : String(totalMedicamentos),
      detalle: 'activos en el catalogo',
      tendencia: '',
      tono: 'blue',
      serie: [],
    },
  ]

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Resumen operativo</p>
          <h2>
            {saludoDelDia()}
            {primerNombre ? `, ${primerNombre}` : ''}
          </h2>
          <p className="muted">Esto es lo mas importante para el cuidado de hoy, {fecha}.</p>
        </div>
      </div>

      {error && (
        <div className="panel" role="alert">
          <p className="form-error">{error}</p>
          <button type="button" className="outline-link" onClick={() => void recargar()}>
            Reintentar
          </button>
        </div>
      )}

      <div className="kpi-grid">
        {indicadores.map((indicador) => (
          <IndicadorKpi key={indicador.etiqueta} {...indicador} />
        ))}
      </div>
      <p className="muted nota-datos">
        Los indicadores se calculan con la agenda de hoy y el catalogo reales.{' '}
        <Link to="/residentes">Ver residentes</Link> · <Link to="/medicamentos">Ver medicamentos</Link>
      </p>

      {/* En tablet y movil la tabla de residentes se oculta, asi que el alta
          se ofrece aqui para no perder la accion principal. */}
      <BotonAccion
        variant="outline"
        className="panel-alta-movil"
        onClick={() => navegar('/residentes/nuevo')}
      >
        <Plus size={16} /> Registrar residente
      </BotonAccion>

      <div className="dashboard-grid">
        <PanelAlertas tomasAbiertas={tomasAbiertas} cargando={cargando} />
        <PanelCumplimiento serie={serie} cargando={cargando} />
      </div>

      <PanelProximasCitas />
    </div>
  )
}