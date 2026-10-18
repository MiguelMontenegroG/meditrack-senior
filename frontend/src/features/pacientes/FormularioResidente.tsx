 // Formulario de alta y edicion de un residente (solo ADMINISTRADOR).
// Valida en cliente (la situacion clinica es obligatoria; la habitacion es
// opcional porque el producto cubre centros de dia); los errores 400
// (validacion) y 409 (documento duplicado) del backend se muestran en el campo
// o en el formulario.

import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto } from '@/lib/mensajes-error'
import {
  actualizarPaciente,
  crearPaciente,
  obtenerPaciente,
} from '@/features/pacientes/services/pacienteService'
import type { DatosPaciente } from '@/features/pacientes/types'
import { CamposResidente, type ErroresResidente } from '@/features/pacientes/CamposResidente'
import { useCargaEdicion } from '@/features/pacientes/useCargaEdicion'

const DATOS_INICIALES: DatosPaciente = {
  documento: '',
  nombres: '',
  apellidos: '',
  fechaNacimiento: '',
  sexo: 'NO_ESPECIFICA',
  habitacion: '',
  situacionClinica: '',
  fechaIngreso: '',
}

/** Fecha de hoy en formato YYYY-MM-DD (zona local del navegador). */
function hoyIso(): string {
  const ahora = new Date()
  const mes = String(ahora.getMonth() + 1).padStart(2, '0')
  const dia = String(ahora.getDate()).padStart(2, '0')
  return `${ahora.getFullYear()}-${mes}-${dia}`
}

/** Valida los campos obligatorios y las reglas de fecha en el cliente. */
function validar(datos: DatosPaciente): ErroresResidente {
  const errores: ErroresResidente = {}
  const hoy = hoyIso()
  if (!datos.documento.trim()) errores.documento = 'El documento es obligatorio.'
  if (!datos.nombres.trim()) errores.nombres = 'Los nombres son obligatorios.'
  if (!datos.apellidos.trim()) errores.apellidos = 'Los apellidos son obligatorios.'
  if (!datos.fechaNacimiento) {
    errores.fechaNacimiento = 'La fecha de nacimiento es obligatoria.'
  } else if (datos.fechaNacimiento > hoy) {
    errores.fechaNacimiento = 'La fecha de nacimiento no puede ser futura.'
  }
  if (!datos.sexo) errores.sexo = 'Selecciona el sexo.'
  // La habitacion es opcional: el producto incluye centros de dia sin habitaciones.
  if (!datos.situacionClinica?.trim()) {
    errores.situacionClinica = 'La situacion clinica es obligatoria en un residente activo.'
  }
  if (datos.fechaIngreso && datos.fechaIngreso > hoy) {
    errores.fechaIngreso = 'La fecha de ingreso no puede ser futura.'
  }
  return errores
}

/** Pantalla de crear/editar residente. */
export function FormularioResidente() {
  const { id } = useParams()
  const navegar = useNavigate()
  const esEdicion = id !== undefined
  const { datos, setDatos, cargando, errorCarga } = useCargaEdicion(
    esEdicion ? Number(id) : null,
    DATOS_INICIALES,
    obtenerPaciente,
  )
  const [errores, setErrores] = useState<ErroresResidente>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const guardar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validar(datos)
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return
    setEnviando(true)
    try {
      const cuerpo: DatosPaciente = {
        ...datos,
        habitacion: datos.habitacion?.trim() ?? null,
        situacionClinica: datos.situacionClinica?.trim() ?? null,
        fechaIngreso: datos.fechaIngreso || null,
      }
      if (esEdicion) {
        await actualizarPaciente(Number(id), cuerpo)
      } else {
        await crearPaciente(cuerpo)
      }
      navegar('/residentes')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        setErrores((previos) => ({
          ...previos,
          documento: mensajeDeErrorContexto('CONFLICTO', 'documento-duplicado'),
        }))
      } else if (fallo instanceof ErrorApi && fallo.codigo === 'VALIDACION') {
        setErrorGeneral(
          mensajeDeErrorContexto('VALIDACION', 'perfil-clinico-incompleto'),
        )
      } else if (fallo instanceof ErrorApi) {
        setErrorGeneral(fallo.mensajeUsuario)
      } else {
        setErrorGeneral('Ocurrió un error inesperado. Inténtalo de nuevo.')
      }
    } finally {
      setEnviando(false)
    }
  }

  if (cargando) return <EstadoCarga mensaje="Cargando residente..." />
  if (errorCarga) return <EstadoError mensaje={errorCarga} />

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={() => navegar('/residentes')}>
          <ArrowLeft size={16} /> Volver
        </BotonAccion>
      </div>
      <form className="formulario-residente" onSubmit={guardar} noValidate>
        <h2>{esEdicion ? 'Editar residente' : 'Nuevo residente'}</h2>
        <CamposResidente
          datos={datos}
          errores={errores}
          alCambiar={(campo, valor) => setDatos({ ...datos, [campo]: valor })}
        />
        {errorGeneral && (
          <p className="form-error" role="alert">
            {errorGeneral}
          </p>
        )}
        <div className="form-acciones">
          <BotonAccion type="submit" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar residente'}
          </BotonAccion>
          <BotonAccion variant="outline" onClick={() => navegar('/residentes')} disabled={enviando}>
            Cancelar
          </BotonAccion>
        </div>
      </form>
    </div>
  )
}
