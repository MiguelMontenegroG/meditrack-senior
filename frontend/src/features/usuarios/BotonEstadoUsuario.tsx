// Boton para activar/desactivar un usuario (solo ADMINISTRADOR). Pide
// confirmacion antes de desactivar y muestra los mensajes de la regla de
// negocio (409: no puedes desactivarte a ti mismo). Evita el doble envio.

import { useState } from 'react'
import { Power } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { cambiarEstadoUsuario } from '@/features/usuarios/services/usuarioService'

interface PropsBotonEstadoUsuario {
  usuarioId: number
  activo: boolean
  /** Si es true, no se ofrece desactivar (es el propio usuario). */
  esPropio: boolean
  alCambiar: () => Promise<void>
}

/** Activa o desactiva un usuario con confirmacion. */
export function BotonEstadoUsuario({
  usuarioId,
  activo,
  esPropio,
  alCambiar,
}: PropsBotonEstadoUsuario) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const aplicar = async () => {
    if (enviando) return
    setEnviando(true)
    setError(null)
    try {
      await cambiarEstadoUsuario(usuarioId, !activo)
      setConfirmando(false)
      await alCambiar()
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        setError(mensajeDeErrorContexto('CONFLICTO', 'usuario-propio'))
      } else {
        setError(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  // Al propio usuario no se le ofrece desactivarse (el backend lo impide).
  if (esPropio && activo) {
    return <p className="muted">No puedes desactivar tu propia cuenta.</p>
  }

  if (activo && !confirmando) {
    return (
      <BotonAccion variant="outline" onClick={() => setConfirmando(true)}>
        <Power size={16} /> Desactivar
      </BotonAccion>
    )
  }

  if (activo && confirmando) {
    return (
      <span className="confirmacion-inline" role="alertdialog" aria-label="Confirmar desactivación">
        <span>¿Desactivar este usuario?</span>
        <BotonAccion variant="danger" onClick={aplicar} disabled={enviando}>
          {enviando ? 'Desactivando...' : 'Sí, desactivar'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={() => setConfirmando(false)} disabled={enviando}>
          Cancelar
        </BotonAccion>
        {error && <span className="campo-error" role="alert">{error}</span>}
      </span>
    )
  }

  return (
    <span className="estado-accion">
      <BotonAccion variant="outline" onClick={aplicar} disabled={enviando}>
        <Power size={16} /> {enviando ? 'Activando...' : 'Activar'}
      </BotonAccion>
      {error && <span className="campo-error" role="alert">{error}</span>}
    </span>
  )
}
