// Campos compartidos del formulario de esquema (crear y editar): medicamento,
// dosis, frecuencia, via, fechas, indicaciones y horarios. Con etiquetas
// accesibles y errores por campo (aria-describedby).

import { OPCIONES_VIA } from '@/features/medicacion/etiquetas'
import { HorariosEsquema } from '@/features/medicacion/HorariosEsquema'
import { SelectorMedicamento } from '@/features/medicacion/SelectorMedicamento'
import type { ViaAdministracion } from '@/features/medicacion/types'

/** Errores por campo del formulario de esquema. */
export interface ErroresEsquema {
  medicamentoId?: string
  dosis?: string
  frecuenciaHoras?: string
  fechaInicio?: string
  fechaFin?: string
  horarios?: string
}

interface PropsCamposEsquema {
  esEdicion: boolean
  medicamentoId: string
  dosis: string
  frecuenciaHoras: string
  via: ViaAdministracion
  indicaciones: string
  fechaInicio: string
  fechaFin: string
  horarios: string[]
  errores: ErroresEsquema
  alCambiar: (
    campo: 'dosis' | 'frecuenciaHoras' | 'indicaciones' | 'fechaInicio' | 'fechaFin',
    valor: string,
  ) => void
  alCambiarMedicamento: (valor: string) => void
  alCambiarVia: (via: ViaAdministracion) => void
  alCambiarHorarios: (horarios: string[]) => void
}

/** Campos del formulario de esquema de medicacion. */
export function CamposEsquema({
  esEdicion,
  medicamentoId,
  dosis,
  frecuenciaHoras,
  via,
  indicaciones,
  fechaInicio,
  fechaFin,
  horarios,
  errores,
  alCambiar,
  alCambiarMedicamento,
  alCambiarVia,
  alCambiarHorarios,
}: PropsCamposEsquema) {
  return (
    <>
      <SelectorMedicamento
        valor={medicamentoId}
        alCambiar={alCambiarMedicamento}
        deshabilitado={esEdicion}
      />
      {esEdicion && (
        <small className="muted">El medicamento no se puede cambiar al editar.</small>
      )}
      {errores.medicamentoId && (
        <span className="campo-error" role="alert">
          {errores.medicamentoId}
        </span>
      )}

      <label>
        Dosis
        <input
          value={dosis}
          onChange={(e) => alCambiar('dosis', e.target.value)}
          maxLength={50}
          placeholder="Por ejemplo: 50 mg"
          aria-invalid={Boolean(errores.dosis)}
          aria-describedby={errores.dosis ? 'error-dosis' : undefined}
        />
        {errores.dosis && (
          <span className="campo-error" id="error-dosis" role="alert">
            {errores.dosis}
          </span>
        )}
      </label>

      <label>
        Frecuencia (cada cuantas horas)
        <input
          type="number"
          min={1}
          max={24}
          value={frecuenciaHoras}
          onChange={(e) => alCambiar('frecuenciaHoras', e.target.value)}
          inputMode="numeric"
          aria-invalid={Boolean(errores.frecuenciaHoras)}
          aria-describedby={errores.frecuenciaHoras ? 'error-frecuencia' : 'ayuda-frecuencia'}
        />
        <small className="muted" id="ayuda-frecuencia">Entre 1 y 24 horas.</small>
        {errores.frecuenciaHoras && (
          <span className="campo-error" id="error-frecuencia" role="alert">
            {errores.frecuenciaHoras}
          </span>
        )}
      </label>

      <label>
        Via de administracion
        <select value={via} onChange={(e) => alCambiarVia(e.target.value as ViaAdministracion)}>
          {OPCIONES_VIA.map((opcion) => (
            <option key={opcion.valor} value={opcion.valor}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </label>

      <label>
        Fecha de inicio
        <input
          type="date"
          value={fechaInicio}
          onChange={(e) => alCambiar('fechaInicio', e.target.value)}
          aria-invalid={Boolean(errores.fechaInicio)}
          aria-describedby={errores.fechaInicio ? 'error-fecha-inicio' : undefined}
        />
        {errores.fechaInicio && (
          <span className="campo-error" id="error-fecha-inicio" role="alert">
            {errores.fechaInicio}
          </span>
        )}
      </label>

      <label>
        Fecha de fin (opcional)
        <input
          type="date"
          value={fechaFin}
          onChange={(e) => alCambiar('fechaFin', e.target.value)}
          aria-invalid={Boolean(errores.fechaFin)}
          aria-describedby={errores.fechaFin ? 'error-fecha-fin' : 'ayuda-fecha-fin'}
        />
        <small className="muted" id="ayuda-fecha-fin">Vacio = vigencia abierta.</small>
        {errores.fechaFin && (
          <span className="campo-error" id="error-fecha-fin" role="alert">
            {errores.fechaFin}
          </span>
        )}
      </label>

      <label>
        Indicaciones (opcional)
        <textarea
          value={indicaciones}
          onChange={(e) => alCambiar('indicaciones', e.target.value)}
          rows={3}
        />
      </label>

      <HorariosEsquema
        frecuenciaHoras={Number(frecuenciaHoras) || 0}
        horarios={horarios}
        alCambiar={alCambiarHorarios}
        error={errores.horarios}
      />
    </>
  )
}
