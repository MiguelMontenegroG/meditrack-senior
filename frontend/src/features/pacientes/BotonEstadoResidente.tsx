// Boton para activar/desactivar un residente (solo ADMINISTRADOR). Al
// desactivar (borrado logico) pide confirmacion y un motivo obligatorio, que el
// backend registra en la auditoria. Muestra el estado del envio para evitar
// doble clic.

import { useState } from 'react'
import { Power } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { cambiarEstadoPaciente } from '@/features/pacientes/services/pacienteService'

const MAX_MOTIVO = 200

interface PropsBotonEstadoResidente {
  pacienteId: number
  activo: boolean
  alCambiar: () => Promise<void>
}

/** Activa o desactiva un residente con confirmacion y motivo. */
export function BotonEstadoResidente({
  pacienteId,
  activo,
  alCambiar,
}: PropsBotonEstadoResidente) {
  const [confirmando, setConfirmando] = useState(false)
  const [motivo, setMotivo] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const cancelar = () => {
    setConfirmando(false)
    setMotivo('')
    setError(null)
  }

  const aplicar = async (razon?: string) => {
    if (enviando) return
    setEnviando(true)
    setError(null)
    try {
      await cambiarEstadoPaciente(pacienteId, !activo, razon)
      cancelar()
      await alCambiar()
    } catch (fallo) {
      if (!activo && fallo instanceof ErrorApi && fallo.codigo === 'VALIDACION') {
        setError(mensajeDeErrorContexto('VALIDACION', 'motivo-obligatorio'))
      } else {
        setError(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  if (activo && !confirmando) {
    return (
      <BotonAccion variant="outline" onClick={() => setConfirmando(true)}>
        <Power size={16} /> Desactivar
      </BotonAccion>
    )
  }

  if (activo && confirmando) {
    const motivoValido = motivo.trim().length > 0
    return (
      <div className="confirmacion-inline" role="alertdialog" aria-label="Confirmar desactivacion">
        <span>Desactivar este residente (borrado logico del acceso)?</span>
        <label className="campo-motivo">
          Motivo de la desactivacion
          <textarea
            value={motivo}
            onChange={(e) => setMotivo(e.target.value.slice(0, MAX_MOTIVO))}
            rows={2}
            maxLength={MAX_MOTIVO}
            placeholder="Ej.: traslado a otro centro, alta por recuperacion"
            aria-required="true"
            aria-invalid={Boolean(error) || undefined}
          />
        </label>
        <BotonAccion
          variant="danger"
          onClick={() => void aplicar(motivo)}
          disabled={enviando || !motivoValido}
        >
          {enviando ? 'Desactivando...' : 'Si, desactivar'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={cancelar} disabled={enviando}>
          Cancelar
        </BotonAccion>
        {error && <span className="campo-error" role="alert">{error}</span>}
      </div>
    )
  }

  return (
    <BotonAccion variant="outline" onClick={() => void aplicar()} disabled={enviando}>
      <Power size={16} /> {enviando ? 'Activando...' : 'Activar'}
    </BotonAccion>
  )
}
