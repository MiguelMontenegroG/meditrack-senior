// Acciones de una cita segun el rol y su estado. Se reutiliza en la tabla
// (escritorio) y en las tarjetas (movil):
// - Editar: solo ADMINISTRADOR y estado PROGRAMADA.
// - Marcar CUMPLIDA/INCUMPLIDA: ADMINISTRADOR o CUIDADOR_ENFERMERO, estado
//   PROGRAMADA y con la fecha y hora ya llegadas.
// - Cancelar: solo ADMINISTRADOR, estado PROGRAMADA, con confirmacion.

import { useState } from 'react'
import { Check, Pencil, X } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { puedeAgendarCitas, puedeCancelarCitas, puedeMarcarEstadoCita } from '@/features/citas/permisos'
import { instanteYaPaso } from '@/features/citas/fechas'
import { useAccionesCita } from '@/features/citas/useAccionesCita'
import type { CitaDetalle } from '@/features/citas/types'
import type { RolUsuario } from '@/features/auth/types'

interface PropsAccionesCita {
  cita: CitaDetalle
  rol: RolUsuario | undefined
  alEditar: (cita: CitaDetalle) => void
  alCambiarEstado: () => void
}

/** Botones de accion de una cita, habilitados segun rol y estado. */
export function AccionesCita({ cita, rol, alEditar, alCambiarEstado }: PropsAccionesCita) {
  const { enviando, error, marcar } = useAccionesCita()
  const [confirmando, setConfirmando] = useState(false)
  const esProgramada = cita.estado === 'PROGRAMADA'
  const horaPaso = instanteYaPaso(cita.fechaHora)

  const editar = puedeAgendarCitas(rol) && esProgramada
  const marcarEstado = puedeMarcarEstadoCita(rol) && esProgramada && horaPaso
  const cancelar = puedeCancelarCitas(rol) && esProgramada

  if (!editar && !marcarEstado && !cancelar) {
    return null
  }

  const accion = async (estado: 'CUMPLIDA' | 'INCUMPLIDA' | 'CANCELADA') => {
    const ok = await marcar(cita.pacienteId, cita.id, estado)
    if (ok) {
      setConfirmando(false)
      alCambiarEstado()
    }
  }

  return (
    <div className="acciones-cita">
      {marcarEstado && (
        <>
          <BotonAccion variant="outline" disabled={enviando} onClick={() => void accion('CUMPLIDA')}>
            <Check size={16} /> Cumplida
          </BotonAccion>
          <BotonAccion
            variant="outline"
            disabled={enviando}
            onClick={() => void accion('INCUMPLIDA')}
          >
            <X size={16} /> Incumplida
          </BotonAccion>
        </>
      )}
      {editar && (
        <BotonAccion variant="ghost" onClick={() => alEditar(cita)}>
          <Pencil size={16} /> Editar
        </BotonAccion>
      )}
      {cancelar && !confirmando && (
        <BotonAccion variant="danger" onClick={() => setConfirmando(true)}>
          <X size={16} /> Cancelar
        </BotonAccion>
      )}
      {cancelar && confirmando && (
        <span className="confirmar-cancelacion">
          <span>¿Cancelar esta cita?</span>
          <BotonAccion variant="danger" disabled={enviando} onClick={() => void accion('CANCELADA')}>
            Si, cancelar
          </BotonAccion>
          <BotonAccion variant="ghost" disabled={enviando} onClick={() => setConfirmando(false)}>
            No
          </BotonAccion>
        </span>
      )}
      {error && (
        <span className="campo-error" role="alert">
          {error}
        </span>
      )}
    </div>
  )
}