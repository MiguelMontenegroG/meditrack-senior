// Formulario de generacion de un reporte: residente (de su alcance), tipo de
// reporte y rango de fechas. Valida en cliente antes de enviar y muestra el
// error contextual del hook de generacion. No descarga por si mismo: delega en
// `alGenerar`.

import { BotonAccion } from '@/components/ui/BotonAccion'
import { AVISO_GENERACION, DESCRIPCION_TIPO_REPORTE, TIPOS_REPORTE } from '@/features/reportes/etiquetas'
import { DIAS_MAXIMOS_REPORTE } from '@/features/reportes/fechas'
import type { TipoReporte } from '@/features/reportes/types'
import type { PacienteResumen } from '@/features/pacientes/types'

interface PropsFormularioReporte {
  residentes: PacienteResumen[]
  cargandoResidentes: boolean
  residenteId: number | ''
  tipo: TipoReporte
  desde: string
  hasta: string
  generando: boolean
  error: string | null
  alCambiarResidente: (valor: number | '') => void
  alCambiarTipo: (valor: TipoReporte) => void
  alCambiarDesde: (valor: string) => void
  alCambiarHasta: (valor: string) => void
  alGenerar: () => void
}

/** Formulario de generacion de reportes PDF. */
export function FormularioReporte({
  residentes,
  cargandoResidentes,
  residenteId,
  tipo,
  desde,
  hasta,
  generando,
  error,
  alCambiarResidente,
  alCambiarTipo,
  alCambiarDesde,
  alCambiarHasta,
  alGenerar,
}: PropsFormularioReporte) {
  return (
    <form
      className="formulario-reporte"
      onSubmit={(evento) => {
        evento.preventDefault()
        alGenerar()
      }}
    >
      <div className="filtros-listado">
        <label>
          Residente
          <select
            value={residenteId === '' ? '' : String(residenteId)}
            onChange={(evento) =>
              alCambiarResidente(evento.target.value === '' ? '' : Number(evento.target.value))
            }
            required
            disabled={cargandoResidentes}
          >
            <option value="">{cargandoResidentes ? 'Cargando...' : 'Elige un residente'}</option>
            {residentes.map((residente) => (
              <option key={residente.id} value={residente.id}>
                {residente.nombreCompleto}
              </option>
            ))}
          </select>
        </label>
        <label>
          Tipo de reporte
          <select
            value={tipo}
            onChange={(evento) => alCambiarTipo(evento.target.value as TipoReporte)}
          >
            {TIPOS_REPORTE.map((opcion) => (
              <option key={opcion.valor} value={opcion.valor}>
                {opcion.etiqueta}
              </option>
            ))}
          </select>
        </label>
      </div>

      <p className="muted">{DESCRIPCION_TIPO_REPORTE[tipo]}</p>

      <fieldset className="filtro-rango">
        <legend>Periodo del reporte (maximo {DIAS_MAXIMOS_REPORTE} dias)</legend>
        <label>
          Desde
          <input
            type="date"
            value={desde}
            max={hasta || undefined}
            onChange={(evento) => alCambiarDesde(evento.target.value)}
            required
          />
        </label>
        <label>
          Hasta
          <input
            type="date"
            value={hasta}
            min={desde || undefined}
            onChange={(evento) => alCambiarHasta(evento.target.value)}
            required
          />
        </label>
      </fieldset>

      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}

      <p className="muted nota-datos">{AVISO_GENERACION}</p>

      <div className="form-acciones">
        <BotonAccion type="submit" disabled={generando || residenteId === ''}>
          {generando ? 'Generando PDF...' : 'Generar y descargar PDF'}
        </BotonAccion>
      </div>
    </form>
  )
}
