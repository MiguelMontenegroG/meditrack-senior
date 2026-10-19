// Controles del calendario de citas: rango de fechas, filtro de estado y
// selector de residente. Componentes sin estado: los valores viven en la
// pantalla del calendario.

import { ESTADOS_FILTRABLES } from '@/features/citas/etiquetas'
import type { EstadoCita } from '@/features/citas/types'
import type { PacienteResumen } from '@/features/pacientes/types'

interface PropsRango {
  desde: string
  hasta: string
  alCambiarDesde: (valor: string) => void
  alCambiarHasta: (valor: string) => void
}

/** Dos inputs type="date" (desde/hasta) obligatorios. */
export function RangoFechas({ desde, hasta, alCambiarDesde, alCambiarHasta }: PropsRango) {
  return (
    <fieldset className="filtro-rango">
      <legend className="sr-only">Rango de fechas</legend>
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
  )
}

interface PropsEstado {
  valor: EstadoCita | ''
  alCambiar: (valor: EstadoCita | '') => void
}

/** Selector de estado de la cita (vacio = todos). */
export function FiltroEstadoCita({ valor, alCambiar }: PropsEstado) {
  return (
    <label>
      Estado
      <select value={valor} onChange={(evento) => alCambiar(evento.target.value as EstadoCita | '')}>
        <option value="">Todos</option>
        {ESTADOS_FILTRABLES.map((opcion) => (
          <option key={opcion.valor} value={opcion.valor}>
            {opcion.etiqueta}
          </option>
        ))}
      </select>
    </label>
  )
}

interface PropsResidente {
  valor: number | ''
  residentes: PacienteResumen[]
  alCambiar: (valor: number | '') => void
}

/** Selector de residente del alcance (vacio = todos). */
export function SelectorResidente({ valor, residentes, alCambiar }: PropsResidente) {
  return (
    <label>
      Residente
      <select
        value={valor === '' ? '' : String(valor)}
        onChange={(evento) => alCambiar(evento.target.value === '' ? '' : Number(evento.target.value))}
      >
        <option value="">Todos</option>
        {residentes.map((residente) => (
          <option key={residente.id} value={residente.id}>
            {residente.nombreCompleto}
          </option>
        ))}
      </select>
    </label>
  )
}