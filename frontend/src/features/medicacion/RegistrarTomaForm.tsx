// Formulario en linea para registrar una toma de un slot de la agenda. El
// estado por defecto es ADMINISTRADA; al elegir OMITIDA se exige una
// observacion (regla del backend). Evita el doble envio.

import { useState } from 'react'
import { Check, X } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { registrarToma } from '@/features/medicacion/services/tomaService'
import type { EstadoToma } from '@/features/medicacion/types'

interface PropsRegistrarTomaForm {
  pacienteId: number
  esquemaId: number
  /** Instante programado (ISO) del slot que se registra. */
  fechaHoraProgramada: string
  /** Hora legible para el encabezado del formulario. */
  etiquetaHora: string
  alCerrar: () => void
  alRegistrar: () => Promise<void>
}

/** Registra una toma (ADMINISTRADA u OMITIDA) para un slot concreto. */
export function RegistrarTomaForm({
  pacienteId,
  esquemaId,
  fechaHoraProgramada,
  etiquetaHora,
  alCerrar,
  alRegistrar,
}: PropsRegistrarTomaForm) {
  const [estado, setEstado] = useState<EstadoToma>('ADMINISTRADA')
  const [observaciones, setObservaciones] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const guardar = async () => {
    if (enviando) return
    if (estado === 'OMITIDA' && !observaciones.trim()) {
      setError('Indica el motivo por el que se omite la toma.')
      return
    }
    setEnviando(true)
    setError(null)
    try {
      await registrarToma(pacienteId, esquemaId, {
        fechaHoraProgramada,
        estado,
        observaciones: observaciones.trim() || null,
      })
      await alRegistrar()
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        setError(mensajeDeErrorContexto('CONFLICTO', 'toma-duplicada'))
      } else if (fallo instanceof ErrorApi && fallo.codigo === 'VALIDACION') {
        setError(mensajeDeErrorContexto('VALIDACION', 'toma-cerrada'))
      } else {
        setError(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="registro-toma" role="group" aria-label={`Registrar toma de las ${etiquetaHora}`}>
      <p className="muted">Registrar toma de las {etiquetaHora}</p>
      <fieldset className="filtro-estado">
        <legend className="sr-only">Estado de la toma</legend>
        <button
          type="button"
          className={estado === 'ADMINISTRADA' ? 'selected' : ''}
          aria-pressed={estado === 'ADMINISTRADA'}
          onClick={() => setEstado('ADMINISTRADA')}
        >
          <Check size={15} /> Administrada
        </button>
        <button
          type="button"
          className={estado === 'OMITIDA' ? 'selected' : ''}
          aria-pressed={estado === 'OMITIDA'}
          onClick={() => setEstado('OMITIDA')}
        >
          <X size={15} /> Omitida
        </button>
      </fieldset>
      {estado === 'OMITIDA' && (
        <label>
          Motivo de la omision
          <input
            value={observaciones}
            onChange={(e) => setObservaciones(e.target.value)}
            maxLength={200}
            aria-invalid={Boolean(error)}
          />
        </label>
      )}
      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
      <div className="form-acciones">
        <BotonAccion onClick={guardar} disabled={enviando}>
          {enviando ? 'Registrando...' : 'Registrar toma'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={alCerrar} disabled={enviando}>
          Cancelar
        </BotonAccion>
      </div>
    </div>
  )
}
