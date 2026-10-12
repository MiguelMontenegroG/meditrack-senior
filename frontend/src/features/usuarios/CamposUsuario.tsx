// Campos del formulario de usuario (crear/editar) con etiquetas accesibles,
// mensajes de error por campo (aria-describedby) y autocompletado adecuado.
// La contrasena solo se muestra en la creacion y nunca se registra.

import { OPCIONES_ROL } from '@/features/usuarios/permisos'
import type { RolUsuario } from '@/features/usuarios/types'

/** Errores por campo del formulario de usuario. */
export interface ErroresUsuario {
  nombreCompleto?: string
  correo?: string
  contrasena?: string
  telefono?: string
  rol?: string
}

interface PropsCamposUsuario {
  esEdicion: boolean
  nombreCompleto: string
  correo: string
  contrasena: string
  telefono: string
  rol: RolUsuario
  errores: ErroresUsuario
  contrasenaVisible: boolean
  alAlternarContrasena: () => void
  alCambiar: (campo: 'nombreCompleto' | 'correo' | 'contrasena' | 'telefono', valor: string) => void
  alCambiarRol: (rol: RolUsuario) => void
}

/** Campos compartidos del alta y la edicion de usuarios. */
export function CamposUsuario({
  esEdicion,
  nombreCompleto,
  correo,
  contrasena,
  telefono,
  rol,
  errores,
  contrasenaVisible,
  alAlternarContrasena,
  alCambiar,
  alCambiarRol,
}: PropsCamposUsuario) {
  return (
    <>
      <label>
        Nombre completo
        <input
          value={nombreCompleto}
          onChange={(e) => alCambiar('nombreCompleto', e.target.value)}
          autoComplete="name"
          maxLength={150}
          aria-invalid={Boolean(errores.nombreCompleto)}
          aria-describedby={errores.nombreCompleto ? 'error-nombre' : undefined}
        />
        {errores.nombreCompleto && (
          <span className="campo-error" id="error-nombre" role="alert">
            {errores.nombreCompleto}
          </span>
        )}
      </label>

      <label>
        Correo
        <input
          type="email"
          value={correo}
          onChange={(e) => alCambiar('correo', e.target.value)}
          autoComplete="email"
          disabled={esEdicion}
          maxLength={150}
          aria-invalid={Boolean(errores.correo)}
          aria-describedby={errores.correo ? 'error-correo' : undefined}
        />
        {esEdicion && <small className="muted">El correo no se puede modificar.</small>}
        {errores.correo && (
          <span className="campo-error" id="error-correo" role="alert">
            {errores.correo}
          </span>
        )}
      </label>

      {!esEdicion && (
        <label>
          Contraseña
          <span className="password-field">
            <input
              type={contrasenaVisible ? 'text' : 'password'}
              value={contrasena}
              onChange={(e) => alCambiar('contrasena', e.target.value)}
              autoComplete="new-password"
              minLength={8}
              aria-invalid={Boolean(errores.contrasena)}
              aria-describedby={errores.contrasena ? 'error-contrasena' : 'ayuda-contrasena'}
            />
            <button
              type="button"
              onClick={alAlternarContrasena}
              aria-pressed={contrasenaVisible}
            >
              {contrasenaVisible ? 'Ocultar' : 'Mostrar'}
            </button>
          </span>
          <small className="muted" id="ayuda-contrasena">Mínimo 8 caracteres.</small>
          {errores.contrasena && (
            <span className="campo-error" id="error-contrasena" role="alert">
              {errores.contrasena}
            </span>
          )}
        </label>
      )}

      <label>
        Teléfono (opcional)
        <input
          value={telefono}
          onChange={(e) => alCambiar('telefono', e.target.value)}
          inputMode="tel"
          autoComplete="tel"
          maxLength={30}
          aria-invalid={Boolean(errores.telefono)}
          aria-describedby={errores.telefono ? 'error-telefono' : undefined}
        />
        {errores.telefono && (
          <span className="campo-error" id="error-telefono" role="alert">
            {errores.telefono}
          </span>
        )}
      </label>

      <label>
        Rol
        <select
          value={rol}
          onChange={(e) => alCambiarRol(e.target.value as RolUsuario)}
          aria-invalid={Boolean(errores.rol)}
          aria-describedby={errores.rol ? 'error-rol' : undefined}
        >
          {OPCIONES_ROL.map((opcion) => (
            <option key={opcion.valor} value={opcion.valor}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
        {errores.rol && (
          <span className="campo-error" id="error-rol" role="alert">
            {errores.rol}
          </span>
        )}
      </label>
    </>
  )
}
