// Editor de los horarios (HH:mm) de un esquema de medicacion. Permite agregar,
// quitar y editar horas, sugiere las equiespaciadas segun la frecuencia y
// avisa cuando la frecuencia exige una cantidad exacta de horarios.

import { Plus, Trash2 } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import {
  esHoraValida,
  frecuenciaPermiteHorariosExactos,
} from '@/features/medicacion/fechas'

interface PropsHorariosEsquema {
  frecuenciaHoras: number
  horarios: string[]
  alCambiar: (horarios: string[]) => void
  /** Error de negocio mostrado bajo la lista (p. ej. horarios incoherentes). */
  error?: string
}

/** Lista editable de horas del dia para el esquema. */
export function HorariosEsquema({
  frecuenciaHoras,
  horarios,
  alCambiar,
  error,
}: PropsHorariosEsquema) {
  const exigirCantidadExacta = frecuenciaPermiteHorariosExactos(frecuenciaHoras)
  const cantidadEsperada = exigirCantidadExacta ? 24 / Math.trunc(frecuenciaHoras) : null

  const actualizarHora = (indice: number, valor: string) => {
    const copia = [...horarios]
    copia[indice] = valor
    alCambiar(copia)
  }

  const agregar = () => alCambiar([...horarios, ''])
  const quitar = (indice: number) => alCambiar(horarios.filter((_, i) => i !== indice))

  return (
    <fieldset className="horarios-esquema">
      <legend>Horarios</legend>
      {cantidadEsperada !== null && (
        <p className="muted">
          Con una frecuencia de cada {Math.trunc(frecuenciaHoras)} horas se esperan{' '}
          {cantidadEsperada} horarios equiespaciados.
        </p>
      )}
      <ul className="lista-horarios">
        {horarios.map((hora, indice) => {
          const invalida = hora !== '' && !esHoraValida(hora)
          return (
            <li key={indice}>
              <label>
                <span className="sr-only">Hora {indice + 1}</span>
                <input
                  type="time"
                  value={hora}
                  onChange={(e) => actualizarHora(indice, e.target.value)}
                  aria-invalid={invalida}
                />
              </label>
              <button
                type="button"
                className="row-action"
                aria-label={`Quitar la hora ${indice + 1}`}
                onClick={() => quitar(indice)}
              >
                <Trash2 size={17} />
              </button>
            </li>
          )
        })}
      </ul>
      <BotonAccion variant="outline" onClick={agregar}>
        <Plus size={16} /> Agregar horario
      </BotonAccion>
      {error && (
        <span className="campo-error" role="alert">
          {error}
        </span>
      )}
    </fieldset>
  )
}
