// Formulario de alta/edicion de un contacto de emergencia. Solo lo usa el
// ADMINISTRADOR. Valida en cliente y muestra errores por campo.

import { useState } from 'react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import type { ContactoEmergencia, DatosContacto } from '@/features/pacientes/types'

interface PropsFormularioContacto {
  /** Contacto a editar; si se omite, el formulario crea uno nuevo. */
  contacto?: ContactoEmergencia
  alGuardar: (datos: DatosContacto) => Promise<void>
  alCancelar: () => void
}

interface ErroresContacto {
  nombre?: string
  telefono?: string
}

/** Alta y edicion de un contacto de emergencia. */
export function FormularioContacto({ contacto, alGuardar, alCancelar }: PropsFormularioContacto) {
  const [datos, setDatos] = useState<DatosContacto>({
    nombre: contacto?.nombre ?? '',
    parentesco: contacto?.parentesco ?? '',
    telefono: contacto?.telefono ?? '',
    principal: contacto?.principal ?? false,
  })
  const [errores, setErrores] = useState<ErroresContacto>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const validar = (): ErroresContacto => {
    const nuevos: ErroresContacto = {}
    if (!datos.nombre.trim()) nuevos.nombre = 'El nombre es obligatorio.'
    if (!datos.telefono.trim()) nuevos.telefono = 'El teléfono es obligatorio.'
    return nuevos
  }

  const enviar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validar()
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return
    setEnviando(true)
    try {
      await alGuardar(datos)
    } catch (fallo) {
      const api = fallo instanceof ErrorApi ? fallo : new ErrorApi(fallo as never)
      setErrorGeneral(api.mensajeUsuario)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="formulario-contacto" onSubmit={enviar} noValidate>
      <label>
        Nombre
        <input
          value={datos.nombre}
          onChange={(e) => setDatos({ ...datos, nombre: e.target.value })}
          autoComplete="name"
          aria-invalid={Boolean(errores.nombre)}
          aria-describedby={errores.nombre ? 'error-contacto-nombre' : undefined}
        />
        {errores.nombre && (
          <span className="campo-error" id="error-contacto-nombre" role="alert">
            {errores.nombre}
          </span>
        )}
      </label>
      <label>
        Parentesco
        <input
          value={datos.parentesco ?? ''}
          onChange={(e) => setDatos({ ...datos, parentesco: e.target.value })}
          autoComplete="off"
        />
      </label>
      <label>
        Teléfono
        <input
          value={datos.telefono}
          onChange={(e) => setDatos({ ...datos, telefono: e.target.value })}
          inputMode="tel"
          autoComplete="tel"
          aria-invalid={Boolean(errores.telefono)}
          aria-describedby={errores.telefono ? 'error-contacto-telefono' : undefined}
        />
        {errores.telefono && (
          <span className="campo-error" id="error-contacto-telefono" role="alert">
            {errores.telefono}
          </span>
        )}
      </label>
      <label className="check-label">
        <input
          type="checkbox"
          checked={datos.principal}
          onChange={(e) => setDatos({ ...datos, principal: e.target.checked })}
        />
        Contacto principal
      </label>
      {errorGeneral && (
        <p className="form-error" role="alert">
          {errorGeneral}
        </p>
      )}
      <div className="form-acciones">
        <BotonAccion type="submit" disabled={enviando}>
          {enviando ? 'Guardando...' : 'Guardar contacto'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={alCancelar} disabled={enviando}>
          Cancelar
        </BotonAccion>
      </div>
    </form>
  )
}
