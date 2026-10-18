// Formulario de alta y edicion de un esquema de medicacion de un residente
// (solo ADMINISTRADOR). Valida en cliente (frecuencia 1-24, fecha de fin no
// anterior al inicio, horarios presentes) y traduce los conflictos del backend
// (solapamiento de vigencia, residente inactivo).

import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { CamposEsquema, type ErroresEsquema } from '@/features/medicacion/CamposEsquema'
import {
  esHoraValida,
  frecuenciaPermiteHorariosExactos,
  hoyIso,
} from '@/features/medicacion/fechas'
import { BotonEstadoEsquema } from '@/features/medicacion/BotonEstadoEsquema'
import {
  actualizarEsquema,
  crearEsquema,
  obtenerEsquema,
} from '@/features/medicacion/services/esquemaService'
import type { DatosEsquema, ViaAdministracion } from '@/features/medicacion/types'

/** Pantalla de crear/editar un esquema de medicacion de un residente. */
export function FormularioEsquema() {
  const { id, esquemaId } = useParams()
  const pacienteId = Number(id)
  const navegar = useNavigate()
  const esEdicion = esquemaId !== undefined

  const [medicamentoId, setMedicamentoId] = useState('')
  const [dosis, setDosis] = useState('')
  const [frecuenciaHoras, setFrecuencia] = useState('8')
  const [via, setVia] = useState<ViaAdministracion>('ORAL')
  const [indicaciones, setIndicaciones] = useState('')
  const [fechaInicio, setFechaInicio] = useState(hoyIso())
  const [fechaFin, setFechaFin] = useState('')
  const [horarios, setHorarios] = useState<string[]>([])
  const [activo, setActivo] = useState(true)
  const [errores, setErrores] = useState<ErroresEsquema>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [cargando, setCargando] = useState(esEdicion)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const volver = () => navegar(`/residentes/${pacienteId}`)

  useEffect(() => {
    if (!esEdicion) return
    let vigente = true
    setCargando(true)
    obtenerEsquema(pacienteId, Number(esquemaId))
      .then((esquema) => {
        if (!vigente) return
        setMedicamentoId(String(esquema.medicamentoId))
        setDosis(esquema.dosis)
        setFrecuencia(String(esquema.frecuenciaHoras))
        setVia(esquema.via)
        setIndicaciones(esquema.indicaciones ?? '')
        setFechaInicio(esquema.fechaInicio)
        setFechaFin(esquema.fechaFin ?? '')
        setHorarios(esquema.horarios)
        setActivo(esquema.activo)
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
  }, [pacienteId, esquemaId, esEdicion])

  const recargar = async () => {
    if (!esEdicion) return
    const esquema = await obtenerEsquema(pacienteId, Number(esquemaId))
    setActivo(esquema.activo)
  }

  const validar = useMemo(() => {
    return (): ErroresEsquema => {
      const nuevos: ErroresEsquema = {}
      if (!medicamentoId) nuevos.medicamentoId = 'Selecciona un medicamento.'
      if (!dosis.trim()) nuevos.dosis = 'La dosis es obligatoria.'
      const frecuencia = Number(frecuenciaHoras)
      if (!Number.isInteger(frecuencia) || frecuencia < 1 || frecuencia > 24) {
        nuevos.frecuenciaHoras = 'La frecuencia debe estar entre 1 y 24 horas.'
      }
      if (!fechaInicio) nuevos.fechaInicio = 'La fecha de inicio es obligatoria.'
      if (fechaFin && fechaInicio && fechaFin < fechaInicio) {
        nuevos.fechaFin = 'La fecha de fin no puede ser anterior al inicio.'
      }
      const limpias = horarios.filter((h) => h.trim() !== '')
      if (limpias.length === 0) {
        nuevos.horarios = 'Agrega al menos un horario.'
      } else if (!limpias.every(esHoraValida)) {
        nuevos.horarios = 'Cada horario debe tener el formato HH:mm.'
      } else if (
        Number.isInteger(frecuencia) &&
        frecuencia >= 1 &&
        frecuencia <= 24 &&
        frecuenciaPermiteHorariosExactos(frecuencia) &&
        limpias.length !== 24 / frecuencia
      ) {
        nuevos.horarios = `Con frecuencia de ${frecuencia} horas se esperan ${24 / frecuencia} horarios.`
      }
      return nuevos
    }
  }, [medicamentoId, dosis, frecuenciaHoras, fechaInicio, fechaFin, horarios])

  const guardar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validar()
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return
    setEnviando(true)
    try {
      const datos: DatosEsquema = {
        medicamentoId: Number(medicamentoId),
        dosis: dosis.trim(),
        frecuenciaHoras: Number(frecuenciaHoras),
        via,
        indicaciones: indicaciones.trim() || null,
        fechaInicio,
        fechaFin: fechaFin || null,
        horarios: horarios.filter((h) => h.trim() !== ''),
      }
      if (esEdicion) {
        await actualizarEsquema(pacienteId, Number(esquemaId), datos)
      } else {
        await crearEsquema(pacienteId, datos)
      }
      volver()
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        setErrorGeneral(mensajeDeErrorContexto('CONFLICTO', 'esquema-solapado'))
      } else {
        setErrorGeneral(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  if (cargando) return <EstadoCarga mensaje="Cargando esquema..." />
  if (errorCarga) return <EstadoError mensaje={errorCarga} />

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={volver}>
          <ArrowLeft size={16} /> Volver al residente
        </BotonAccion>
      </div>
      <form className="formulario-residente" onSubmit={guardar} noValidate>
        <h2>{esEdicion ? 'Editar esquema de medicacion' : 'Nuevo esquema de medicacion'}</h2>
        <CamposEsquema
          esEdicion={esEdicion}
          medicamentoId={medicamentoId}
          dosis={dosis}
          frecuenciaHoras={frecuenciaHoras}
          via={via}
          indicaciones={indicaciones}
          fechaInicio={fechaInicio}
          fechaFin={fechaFin}
          horarios={horarios}
          errores={errores}
          alCambiar={(campo, valor) => {
            if (campo === 'dosis') setDosis(valor)
            else if (campo === 'frecuenciaHoras') setFrecuencia(valor)
            else if (campo === 'indicaciones') setIndicaciones(valor)
            else if (campo === 'fechaInicio') setFechaInicio(valor)
            else setFechaFin(valor)
          }}
          alCambiarMedicamento={setMedicamentoId}
          alCambiarVia={setVia}
          alCambiarHorarios={setHorarios}
        />
        {errorGeneral && (
          <p className="form-error" role="alert">
            {errorGeneral}
          </p>
        )}
        <div className="form-acciones">
          <BotonAccion type="submit" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar esquema'}
          </BotonAccion>
          <BotonAccion variant="outline" onClick={volver} disabled={enviando}>
            Cancelar
          </BotonAccion>
          {esEdicion && (
            <BotonEstadoEsquema
              pacienteId={pacienteId}
              esquemaId={Number(esquemaId)}
              activo={activo}
              alCambiar={recargar}
            />
          )}
        </div>
      </form>
    </div>
  )
}
