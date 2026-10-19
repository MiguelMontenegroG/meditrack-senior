// Campos del formulario de agendar/editar una cita. Componente presentacional:
// el estado y la validacion viven en FormularioCita.

import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import type { Especialidad } from '@/features/citas/types'
import type { PacienteResumen } from '@/features/pacientes/types'

/** Errores de validacion por campo del formulario de cita. */
export interface ErroresCita {
  pacienteId?: string
  fecha?: string
  hora?: string
  profesional?: string
  lugar?: string
}

/** Valores controlados del formulario. */
export interface ValoresCita {
  pacienteId: string
  especialidadId: string
  profesional: string
  lugar: string
  motivo: string
  fecha: string
  hora: string
}

interface PropsCamposCita {
  valores: ValoresCita
  errores: ErroresCita
  esEdicion: boolean
  residentes: PacienteResumen[]
  cargandoResidentes: boolean
  errorResidentes: string | null
  especialidades: Especialidad[]
  nombreResidente: string
  fechaMinima: string
  alCambiar: (campo: keyof ValoresCita, valor: string) => void
}

/** Campos del formulario de cita en una sola columna. */
export function CamposCita({
  valores,
  errores,
  esEdicion,
  residentes,
  cargandoResidentes,
  errorResidentes,
  especialidades,
  nombreResidente,
  fechaMinima,
  alCambiar,
}: PropsCamposCita) {
  return (
    <>
      {esEdicion ? (
        <label>
          Residente
          <input value={nombreResidente} disabled readOnly />
        </label>
      ) : (
        <label>
          Residente
          {cargandoResidentes && <EstadoCarga mensaje="Cargando residentes..." />}
          {!cargandoResidentes && errorResidentes && <EstadoError mensaje={errorResidentes} />}
          {!cargandoResidentes && !errorResidentes && (
            <select
              value={valores.pacienteId}
              onChange={(evento) => alCambiar('pacienteId', evento.target.value)}
              aria-invalid={Boolean(errores.pacienteId)}
            >
              <option value="">Selecciona un residente</option>
              {residentes.map((residente) => (
                <option key={residente.id} value={residente.id}>
                  {residente.nombreCompleto}
                  {residente.habitacion ? ` (Hab. ${residente.habitacion})` : ''}
                </option>
              ))}
            </select>
          )}
          {errores.pacienteId && (
            <span className="campo-error" role="alert">
              {errores.pacienteId}
            </span>
          )}
        </label>
      )}

      <label>
        Especialidad (opcional)
        <select
          value={valores.especialidadId}
          onChange={(evento) => alCambiar('especialidadId', evento.target.value)}
        >
          <option value="">Visita general</option>
          {especialidades.map((especialidad) => (
            <option key={especialidad.id} value={especialidad.id}>
              {especialidad.nombre}
            </option>
          ))}
        </select>
      </label>

      <label>
        Profesional
        <input
          value={valores.profesional}
          onChange={(evento) => alCambiar('profesional', evento.target.value)}
          maxLength={150}
          aria-invalid={Boolean(errores.profesional)}
        />
        {errores.profesional && (
          <span className="campo-error" role="alert">
            {errores.profesional}
          </span>
        )}
      </label>

      <label>
        Lugar
        <input
          value={valores.lugar}
          onChange={(evento) => alCambiar('lugar', evento.target.value)}
          maxLength={150}
          aria-invalid={Boolean(errores.lugar)}
        />
        {errores.lugar && (
          <span className="campo-error" role="alert">
            {errores.lugar}
          </span>
        )}
      </label>

      <label>
        Motivo
        <textarea
          value={valores.motivo}
          onChange={(evento) => alCambiar('motivo', evento.target.value)}
          rows={3}
        />
      </label>

      <label>
        Fecha
        <input
          type="date"
          value={valores.fecha}
          min={fechaMinima}
          onChange={(evento) => alCambiar('fecha', evento.target.value)}
          aria-invalid={Boolean(errores.fecha)}
          required
        />
        {errores.fecha && (
          <span className="campo-error" role="alert">
            {errores.fecha}
          </span>
        )}
      </label>

      <label>
        Hora
        <input
          type="time"
          value={valores.hora}
          onChange={(evento) => alCambiar('hora', evento.target.value)}
          aria-invalid={Boolean(errores.hora)}
          required
        />
        {errores.hora && (
          <span className="campo-error" role="alert">
            {errores.hora}
          </span>
        )}
      </label>
    </>
  )
}