// Pantalla para que cualquier usuario autenticado cambie su propia
// contrasena: actual, nueva y confirmacion. Tras el exito valida que el token
// siga vigente (GET /auth/me); si el backend lo rechaza, cierra sesion.

import { useState } from 'react'
import { CheckCircle2 } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { cambiarContrasena, obtenerUsuarioActual } from '@/features/auth/services/authService'

const LONGITUD_MINIMA = 8

/** Pantalla de cambio de la contrasena propia. */
export function CambioContrasena() {
  const { cerrarSesion } = useAutenticacion()
  const [actual, setActual] = useState('')
  const [nueva, setNueva] = useState('')
  const [confirmacion, setConfirmacion] = useState('')
  const [errorActual, setErrorActual] = useState<string | null>(null)
  const [errorNueva, setErrorNueva] = useState<string | null>(null)
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [exito, setExito] = useState(false)
  const [enviando, setEnviando] = useState(false)

  /** Valida en cliente y devuelve true si el formulario es correcto. */
  function validar(): boolean {
    let valido = true
    setErrorActual(null)
    setErrorNueva(null)
    setErrorGeneral(null)
    setExito(false)
    if (!actual) {
      setErrorActual('Ingresa tu contraseña actual.')
      valido = false
    }
    if (nueva.length < LONGITUD_MINIMA) {
      setErrorNueva(`La contraseña nueva debe tener al menos ${LONGITUD_MINIMA} caracteres.`)
      valido = false
    } else if (nueva === actual) {
      setErrorNueva('La contraseña nueva no puede ser igual a la actual.')
      valido = false
    } else if (nueva !== confirmacion) {
      setErrorNueva('La confirmación no coincide con la contraseña nueva.')
      valido = false
    }
    return valido
  }

  const enviar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    if (!validar()) return
    setEnviando(true)
    try {
      await cambiarContrasena({ contrasenaActual: actual, contrasenaNueva: nueva })
      // Tras el cambio, se comprueba que el token siga siendo valido.
      try {
        await obtenerUsuarioActual()
      } catch {
        // El token dejo de ser valido: se cierra sesion y se avisa.
        cerrarSesion()
        return
      }
      setExito(true)
      setActual('')
      setNueva('')
      setConfirmacion('')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.status === 409) {
        setErrorActual('La contraseña actual no es correcta.')
      } else if (fallo instanceof ErrorApi && fallo.status === 400) {
        setErrorNueva(`La contraseña nueva debe tener al menos ${LONGITUD_MINIMA} caracteres.`)
      } else {
        setErrorGeneral(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Mi cuenta</p>
          <h2>Cambiar contraseña</h2>
        </div>
      </div>

      <form className="formulario-residente" onSubmit={enviar} noValidate>
        {exito && (
          <p className="aviso-exito" role="status">
            <CheckCircle2 size={18} /> Tu contraseña se actualizó correctamente.
          </p>
        )}

        <label>
          Contraseña actual
          <input
            type="password"
            value={actual}
            onChange={(e) => setActual(e.target.value)}
            autoComplete="current-password"
            aria-invalid={Boolean(errorActual)}
            aria-describedby={errorActual ? 'error-actual' : undefined}
          />
          {errorActual && (
            <span className="campo-error" id="error-actual" role="alert">
              {errorActual}
            </span>
          )}
        </label>

        <label>
          Contraseña nueva
          <input
            type="password"
            value={nueva}
            onChange={(e) => setNueva(e.target.value)}
            autoComplete="new-password"
            minLength={LONGITUD_MINIMA}
            aria-invalid={Boolean(errorNueva)}
            aria-describedby={errorNueva ? 'error-nueva' : 'ayuda-nueva'}
          />
          <small className="muted" id="ayuda-nueva">Mínimo {LONGITUD_MINIMA} caracteres.</small>
          {errorNueva && (
            <span className="campo-error" id="error-nueva" role="alert">
              {errorNueva}
            </span>
          )}
        </label>

        <label>
          Confirmar contraseña nueva
          <input
            type="password"
            value={confirmacion}
            onChange={(e) => setConfirmacion(e.target.value)}
            autoComplete="new-password"
            aria-invalid={Boolean(errorNueva)}
            aria-describedby={errorNueva ? 'error-nueva' : undefined}
          />
        </label>

        {errorGeneral && (
          <p className="form-error" role="alert">
            {errorGeneral}
          </p>
        )}

        <div className="form-acciones">
          <BotonAccion type="submit" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Cambiar contraseña'}
          </BotonAccion>
        </div>
      </form>
    </div>
  )
}
