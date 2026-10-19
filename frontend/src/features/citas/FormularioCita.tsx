// Formulario de agendar y editar citas (solo ADMINISTRADOR). En alta se elige
// residente activo; en edicion el residente queda fijo (el contrato edita por
// pacienteId). La fecha y hora se interpretan en hora local de Bogota y se
// convierten a instante UTC. Los 400 (fecha pasada, cita duplicada) y 409
// (estado final) se traducen en lib/mensajes-error.

import { useMemo, useState } from 'react'
import { useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { CamposCita, type ErroresCita, type ValoresCita } from '@/features/citas/CamposCita'
import { aInstanteUtc, fechaBogota, horaBogota, hoyBogota, yaPaso } from '@/features/citas/fechas'
import { useEspecialidades } from '@/features/citas/useEspecialidades'
import { useResidentesAlcance } from '@/features/citas/useResidentesAlcance'
import { agendarCita, editarCita } from '@/features/citas/services/citaService'
import type { CitaDetalle, DatosCita } from '@/features/citas/types'

const LARGO_TEXTO = 150

/** Localiza la cita a editar en el estado de navegacion. */
interface EstadoNavegacion {
  cita?: CitaDetalle
}

/** Pantalla de crear/editar una cita. */
export function FormularioCita() {
  const { citaId } = useParams()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [params] = useSearchParams()
  const esEdicion = citaId !== undefined
  const citaInicial = (ubicacion.state as EstadoNavegacion | null)?.cita
  // Al agendar desde el detalle de un residente, el pacienteId llega por query.
  const pacienteDesdeQuery = params.get('pacienteId') ?? ''

  const { residentes, cargando: cargandoResidentes, error: errorResidentes } =
    useResidentesAlcance()
  const { especialidades } = useEspecialidades()

  const [valores, setValores] = useState<ValoresCita>(() => ({
    pacienteId: citaInicial ? String(citaInicial.pacienteId) : pacienteDesdeQuery,
    especialidadId: citaInicial?.especialidadId ? String(citaInicial.especialidadId) : '',
    profesional: citaInicial?.profesional ?? '',
    lugar: citaInicial?.lugar ?? '',
    motivo: citaInicial?.motivo ?? '',
    fecha: citaInicial ? fechaBogota(citaInicial.fechaHora) : hoyBogota(),
    hora: citaInicial ? horaBogota(citaInicial.fechaHora) : '08:00',
  }))
  const [errores, setErrores] = useState<ErroresCita>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const nombreResidente = useMemo(() => {
    if (!esEdicion) return ''
    const encontrado = residentes.find((residente) => String(residente.id) === valores.pacienteId)
    return encontrado?.nombreCompleto ?? `Residente #${valores.pacienteId}`
  }, [esEdicion, residentes, valores.pacienteId])

  const cambiar = (campo: keyof ValoresCita, valor: string) =>
    setValores((actual) => ({ ...actual, [campo]: valor }))

  const validar = (): ErroresCita => {
    const nuevos: ErroresCita = {}
    if (!esEdicion && !valores.pacienteId) {
      nuevos.pacienteId = 'Selecciona un residente.'
    }
    if (valores.profesional.length > LARGO_TEXTO) {
      nuevos.profesional = `El profesional no puede superar ${LARGO_TEXTO} caracteres.`
    }
    if (valores.lugar.length > LARGO_TEXTO) {
      nuevos.lugar = `El lugar no puede superar ${LARGO_TEXTO} caracteres.`
    }
    if (!valores.fecha) {
      nuevos.fecha = 'La fecha es obligatoria.'
    }
    if (!valores.hora) {
      nuevos.hora = 'La hora es obligatoria.'
    }
    if (valores.fecha && valores.hora && !aInstanteUtc(valores.fecha, valores.hora)) {
      nuevos.fecha = 'La fecha u hora no es valida.'
    } else if (valores.fecha && valores.hora && yaPaso(valores.fecha, valores.hora)) {
      nuevos.fecha = 'La fecha y hora de la cita no puede estar en el pasado.'
    }
    return nuevos
  }

  const guardar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validar()
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return

    const fechaHora = aInstanteUtc(valores.fecha, valores.hora)
    if (!fechaHora) return

    const datos: DatosCita = {
      especialidadId: valores.especialidadId ? Number(valores.especialidadId) : null,
      profesional: valores.profesional.trim() || null,
      fechaHora,
      lugar: valores.lugar.trim() || null,
      motivo: valores.motivo.trim() || null,
    }
    setEnviando(true)
    try {
      if (esEdicion) {
        await editarCita(Number(valores.pacienteId), Number(citaId), datos)
      } else {
        await agendarCita(Number(valores.pacienteId), datos)
      }
      navegar('/calendario')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        setErrorGeneral(mensajeDeErrorContexto('CONFLICTO', 'cita-estado-final'))
      } else {
        setErrorGeneral(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  if (esEdicion && !citaInicial) {
    return (
      <div className="page-content">
        <div className="acciones-detalle">
          <BotonAccion variant="ghost" onClick={() => navegar('/calendario')}>
            <ArrowLeft size={16} /> Volver
          </BotonAccion>
        </div>
        <p className="form-error" role="alert">
          No pudimos abrir la cita. Vuelve al calendario y selecciona de nuevo.
        </p>
      </div>
    )
  }

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={() => navegar('/calendario')}>
          <ArrowLeft size={16} /> Volver
        </BotonAccion>
      </div>
      <form className="formulario-residente" onSubmit={guardar} noValidate>
        <h2>{esEdicion ? 'Editar cita' : 'Agendar cita'}</h2>
        <CamposCita
          valores={valores}
          errores={errores}
          esEdicion={esEdicion}
          residentes={residentes}
          cargandoResidentes={cargandoResidentes}
          errorResidentes={errorResidentes}
          especialidades={especialidades}
          nombreResidente={nombreResidente}
          fechaMinima={hoyBogota()}
          alCambiar={cambiar}
        />
        {errorGeneral && (
          <p className="form-error" role="alert">
            {errorGeneral}
          </p>
        )}
        <div className="form-acciones">
          <BotonAccion type="submit" disabled={enviando}>
            {enviando ? 'Guardando...' : esEdicion ? 'Guardar cita' : 'Agendar cita'}
          </BotonAccion>
          <BotonAccion variant="outline" onClick={() => navegar('/calendario')} disabled={enviando}>
            Cancelar
          </BotonAccion>
        </div>
      </form>
    </div>
  )
}