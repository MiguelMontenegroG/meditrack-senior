// Campos del formulario de residente con etiquetas accesibles, mensajes de
// error por campo (aria-describedby) e inputmode/autocomplete apropiados.

import type { DatosPaciente, Sexo } from '@/features/pacientes/types'

/** Errores por campo del formulario de residente. */
export interface ErroresResidente {
  documento?: string
  nombres?: string
  apellidos?: string
  fechaNacimiento?: string
  sexo?: string
  habitacion?: string
  situacionClinica?: string
  fechaIngreso?: string
}

interface PropsCamposResidente {
  datos: DatosPaciente
  errores: ErroresResidente
  alCambiar: (campo: keyof DatosPaciente, valor: string) => void
}

const OPCIONES_SEXO: { valor: Sexo; etiqueta: string }[] = [
  { valor: 'MASCULINO', etiqueta: 'Masculino' },
  { valor: 'FEMENINO', etiqueta: 'Femenino' },
  { valor: 'OTRO', etiqueta: 'Otro' },
  { valor: 'NO_ESPECIFICA', etiqueta: 'No especifica' },
]

/** Campos del alta/edicion de residente. */
export function CamposResidente({ datos, errores, alCambiar }: PropsCamposResidente) {
  return (
    <>
      <label>
        Documento
        <input
          value={datos.documento}
          onChange={(e) => alCambiar('documento', e.target.value)}
          inputMode="numeric"
          autoComplete="off"
          maxLength={30}
          aria-invalid={Boolean(errores.documento)}
          aria-describedby={errores.documento ? 'error-documento' : undefined}
        />
        {errores.documento && (
          <span className="campo-error" id="error-documento" role="alert">
            {errores.documento}
          </span>
        )}
      </label>
      <label>
        Nombres
        <input
          value={datos.nombres}
          onChange={(e) => alCambiar('nombres', e.target.value)}
          autoComplete="given-name"
          maxLength={100}
          aria-invalid={Boolean(errores.nombres)}
          aria-describedby={errores.nombres ? 'error-nombres' : undefined}
        />
        {errores.nombres && (
          <span className="campo-error" id="error-nombres" role="alert">
            {errores.nombres}
          </span>
        )}
      </label>
      <label>
        Apellidos
        <input
          value={datos.apellidos}
          onChange={(e) => alCambiar('apellidos', e.target.value)}
          autoComplete="family-name"
          maxLength={100}
          aria-invalid={Boolean(errores.apellidos)}
          aria-describedby={errores.apellidos ? 'error-apellidos' : undefined}
        />
        {errores.apellidos && (
          <span className="campo-error" id="error-apellidos" role="alert">
            {errores.apellidos}
          </span>
        )}
      </label>
      <label>
        Fecha de nacimiento
        <input
          type="date"
          value={datos.fechaNacimiento}
          onChange={(e) => alCambiar('fechaNacimiento', e.target.value)}
          aria-invalid={Boolean(errores.fechaNacimiento)}
          aria-describedby={errores.fechaNacimiento ? 'error-fecha-nacimiento' : undefined}
        />
        {errores.fechaNacimiento && (
          <span className="campo-error" id="error-fecha-nacimiento" role="alert">
            {errores.fechaNacimiento}
          </span>
        )}
      </label>
      <label>
        Sexo
        <select
          value={datos.sexo}
          onChange={(e) => alCambiar('sexo', e.target.value)}
          aria-invalid={Boolean(errores.sexo)}
          aria-describedby={errores.sexo ? 'error-sexo' : undefined}
        >
          {OPCIONES_SEXO.map((opcion) => (
            <option key={opcion.valor} value={opcion.valor}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
        {errores.sexo && (
          <span className="campo-error" id="error-sexo" role="alert">
            {errores.sexo}
          </span>
        )}
      </label>
      <label>
        Habitación (opcional)
        <input
          value={datos.habitacion ?? ''}
          onChange={(e) => alCambiar('habitacion', e.target.value)}
          autoComplete="off"
          maxLength={20}
          placeholder="Déjala vacía en centros de día"
          aria-invalid={Boolean(errores.habitacion)}
          aria-describedby={errores.habitacion ? 'error-habitacion' : undefined}
        />
        {errores.habitacion && (
          <span className="campo-error" id="error-habitacion" role="alert">
            {errores.habitacion}
          </span>
        )}
      </label>
      <label className="campo-ancho">
        Situación clínica
        <textarea
          value={datos.situacionClinica ?? ''}
          onChange={(e) => alCambiar('situacionClinica', e.target.value)}
          rows={3}
          maxLength={1000}
          placeholder="Diagnósticos, condiciones y cuidados relevantes"
          aria-invalid={Boolean(errores.situacionClinica)}
          aria-describedby={errores.situacionClinica ? 'error-situacion-clinica' : undefined}
        />
        {errores.situacionClinica && (
          <span className="campo-error" id="error-situacion-clinica" role="alert">
            {errores.situacionClinica}
          </span>
        )}
      </label>
      <label>
        Fecha de ingreso
        <input
          type="date"
          value={datos.fechaIngreso ?? ''}
          onChange={(e) => alCambiar('fechaIngreso', e.target.value)}
          aria-invalid={Boolean(errores.fechaIngreso)}
          aria-describedby={errores.fechaIngreso ? 'error-fecha-ingreso' : undefined}
        />
        {errores.fechaIngreso && (
          <span className="campo-error" id="error-fecha-ingreso" role="alert">
            {errores.fechaIngreso}
          </span>
        )}
      </label>
    </>
  )
}
