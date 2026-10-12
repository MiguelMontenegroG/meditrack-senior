// Formulario de alta y edicion de usuarios (solo ADMINISTRADOR). Valida en
// cliente; los errores del backend (400 validacion, 409 correo duplicado y 409
// reglas de administrador) se muestran por campo o en el formulario.

import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { CamposUsuario, type ErroresUsuario } from '@/features/usuarios/CamposUsuario'
import { esUsuarioPropio } from '@/features/usuarios/permisos'
import {
  actualizarUsuario,
  crearUsuario,
  obtenerUsuario,
} from '@/features/usuarios/services/usuarioService'
import type { RolUsuario } from '@/features/usuarios/types'

const LONGITUD_MINIMA_CONTRASENA = 8
const LARGO_TELEFONO = 30

/** Valida los campos del formulario de usuario en el cliente. */
function validar(
  esEdicion: boolean,
  campos: { nombreCompleto: string; correo: string; contrasena: string; telefono: string },
): ErroresUsuario {
  const errores: ErroresUsuario = {}
  if (!campos.nombreCompleto.trim()) errores.nombreCompleto = 'El nombre es obligatorio.'
  if (!esEdicion) {
    if (!campos.correo.trim()) {
      errores.correo = 'El correo es obligatorio.'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(campos.correo.trim())) {
      errores.correo = 'Ingresa un correo válido.'
    }
    if (!campos.contrasena) {
      errores.contrasena = 'La contraseña es obligatoria.'
    } else if (campos.contrasena.length < LONGITUD_MINIMA_CONTRASENA) {
      errores.contrasena = `La contraseña debe tener al menos ${LONGITUD_MINIMA_CONTRASENA} caracteres.`
    }
  }
  if (campos.telefono.length > LARGO_TELEFONO) {
    errores.telefono = `El teléfono no puede superar ${LARGO_TELEFONO} caracteres.`
  }
  return errores
}

/** Pantalla de crear/editar usuario. */
export function FormularioUsuario() {
  const { id } = useParams()
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const esEdicion = id !== undefined

  const [nombreCompleto, setNombre] = useState('')
  const [correo, setCorreo] = useState('')
  const [contrasena, setContrasena] = useState('')
  const [telefono, setTelefono] = useState('')
  const [rol, setRol] = useState<RolUsuario>('CUIDADOR_ENFERMERO')
  const [contrasenaVisible, setContrasenaVisible] = useState(false)
  const [errores, setErrores] = useState<ErroresUsuario>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [cargando, setCargando] = useState(esEdicion)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!esEdicion) return
    let vigente = true
    setCargando(true)
    obtenerUsuario(Number(id))
      .then((u) => {
        if (!vigente) return
        setNombre(u.nombreCompleto)
        setCorreo(u.correo)
        setTelefono(u.telefono ?? '')
        setRol(u.rol)
      })
      .catch((fallo) => {
        if (vigente) setErrorCarga(mensajeDeFallo(fallo))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [id, esEdicion])

  const guardar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validar(esEdicion, { nombreCompleto, correo, contrasena, telefono })
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return
    setEnviando(true)
    try {
      if (esEdicion) {
        await actualizarUsuario(Number(id), {
          nombreCompleto: nombreCompleto.trim(),
          telefono: telefono.trim() || null,
          rol,
        })
      } else {
        await crearUsuario({
          nombreCompleto: nombreCompleto.trim(),
          correo: correo.trim(),
          contrasena,
          rol,
          telefono: telefono.trim() || null,
        })
      }
      navegar('/usuarios')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        if (!esEdicion) {
          setErrores((previos) => ({
            ...previos,
            correo: mensajeDeErrorContexto('CONFLICTO', 'correo-duplicado'),
          }))
        } else {
          setErrorGeneral(mensajeDeErrorContexto('CONFLICTO', 'usuario-propio'))
        }
      } else {
        setErrorGeneral(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  if (cargando) return <EstadoCarga mensaje="Cargando usuario..." />
  if (errorCarga) return <EstadoError mensaje={errorCarga} />

  const editandoPropiaCuenta = esEdicion && esUsuarioPropio(usuario?.id, Number(id))

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={() => navegar('/usuarios')}>
          <ArrowLeft size={16} /> Volver
        </BotonAccion>
      </div>
      <form className="formulario-residente" onSubmit={guardar} noValidate>
        <h2>{esEdicion ? 'Editar usuario' : 'Nuevo usuario'}</h2>
        {editandoPropiaCuenta && (
          <p className="muted" role="status">
            Estás editando tu propia cuenta: no puedes cambiar tu rol.
          </p>
        )}
        <CamposUsuario
          esEdicion={esEdicion}
          nombreCompleto={nombreCompleto}
          correo={correo}
          contrasena={contrasena}
          telefono={telefono}
          rol={rol}
          errores={errores}
          contrasenaVisible={contrasenaVisible}
          alAlternarContrasena={() => setContrasenaVisible((v) => !v)}
          alCambiar={(campo, valor) => {
            if (campo === 'nombreCompleto') setNombre(valor)
            else if (campo === 'correo') setCorreo(valor)
            else if (campo === 'contrasena') setContrasena(valor)
            else setTelefono(valor)
          }}
          alCambiarRol={setRol}
        />
        {errorGeneral && (
          <p className="form-error" role="alert">
            {errorGeneral}
          </p>
        )}
        <div className="form-acciones">
          <BotonAccion type="submit" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar usuario'}
          </BotonAccion>
          <BotonAccion variant="outline" onClick={() => navegar('/usuarios')} disabled={enviando}>
            Cancelar
          </BotonAccion>
        </div>
      </form>
    </div>
  )
}
