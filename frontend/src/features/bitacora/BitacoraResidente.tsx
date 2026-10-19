// Pantalla de la bitacora de signos vitales de un residente. Los tres roles con
// alcance leen el historial; ADMINISTRADOR y CUIDADOR_ENFERMERO pueden
// registrar. Los registros son inmutables, por eso no hay edicion ni borrado.
//
// Se carga el residente para mostrar su nombre en el encabezado; un 404 se
// presenta como "Residente no encontrado" sin sugerir que existe.

import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Activity } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeRegistrarSignosVitales } from '@/features/bitacora/permisos'
import { FormularioSignosVitales } from '@/features/bitacora/FormularioSignosVitales'
import { FiltroRangoSignos, HistorialSignos } from '@/features/bitacora/HistorialSignos'
import { useHistorialSignos } from '@/features/bitacora/useHistorialSignos'
import { obtenerPaciente } from '@/features/pacientes/services/pacienteService'
import type { PacienteDetalle } from '@/features/pacientes/types'

const TAMANO_HISTORIAL = 10

interface EstadoResidente {
  residente: PacienteDetalle | null
  cargando: boolean
  noEncontrado: boolean
  error: string | null
}

/** Carga el residente para el encabezado; distingue 404 del resto de fallos. */
function useResidente(pacienteId: number): EstadoResidente {
  const [residente, setResidente] = useState<PacienteDetalle | null>(null)
  const [cargando, setCargando] = useState(true)
  const [noEncontrado, setNoEncontrado] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    setNoEncontrado(false)
    obtenerPaciente(pacienteId)
      .then((resultado) => {
        if (vigente) setResidente(resultado)
      })
      .catch((fallo: unknown) => {
        if (!vigente) return
        const status = (fallo as { status?: number }).status
        if (status === 404) {
          setNoEncontrado(true)
        } else {
          setError(mensajeDeFallo(fallo))
        }
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [pacienteId])

  return { residente, cargando, noEncontrado, error }
}

/** Bitacora de signos vitales de un residente. */
export function BitacoraResidente() {
  const { id } = useParams()
  const pacienteId = Number(id)
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const puedeRegistrar = puedeRegistrarSignosVitales(usuario?.rol)

  const residente = useResidente(pacienteId)

  const [mostrarFormulario, setMostrarFormulario] = useState(false)
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [pagina, setPagina] = useState(0)

  const historial = useHistorialSignos(pacienteId, {
    desde,
    hasta,
    pagina,
    tamano: TAMANO_HISTORIAL,
  })

  // Cambiar el rango vuelve a la primera pagina del historial.
  const cambiarDesde = (valor: string) => {
    setDesde(valor)
    setPagina(0)
  }
  const cambiarHasta = (valor: string) => {
    setHasta(valor)
    setPagina(0)
  }
  const limpiarRango = () => {
    setDesde('')
    setHasta('')
    setPagina(0)
  }

  if (residente.cargando) return <EstadoCarga mensaje="Cargando residente..." />

  if (residente.noEncontrado) {
    return (
      <div className="page-content">
        <EstadoError mensaje="Residente no encontrado." />
        <div className="acciones-detalle">
          <BotonAccion variant="outline" onClick={() => navegar('/residentes')}>
            Volver al listado
          </BotonAccion>
        </div>
      </div>
    )
  }

  if (residente.error || !residente.residente) {
    return (
      <div className="page-content">
        <EstadoError mensaje={residente.error ?? 'No pudimos cargar el residente.'} />
        <div className="acciones-detalle">
          <BotonAccion variant="outline" onClick={() => navegar('/residentes')}>
            Volver al listado
          </BotonAccion>
        </div>
      </div>
    )
  }

  const datos = historial.datos

  const alRegistrar = () => {
    setMostrarFormulario(false)
    setPagina(0)
    historial.recargar()
  }

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={() => navegar(`/residentes/${pacienteId}`)}>
          <ArrowLeft size={16} /> Volver al residente
        </BotonAccion>
      </div>

      <div className="page-intro">
        <div>
          <p className="eyebrow">Bitacora</p>
          <h2>Signos vitales</h2>
          <p className="muted">
            {residente.residente.nombreCompleto}
            {residente.residente.habitacion ? ` · Hab. ${residente.residente.habitacion}` : ''}
          </p>
        </div>
        {puedeRegistrar && !mostrarFormulario && (
          <BotonAccion onClick={() => setMostrarFormulario(true)}>
            <Activity size={17} /> Registrar signos
          </BotonAccion>
        )}
      </div>

      {mostrarFormulario && puedeRegistrar && (
        <section className="panel">
          <FormularioSignosVitales
            pacienteId={pacienteId}
            alRegistrar={alRegistrar}
            alCancelar={() => setMostrarFormulario(false)}
          />
        </section>
      )}

      <FiltroRangoSignos
        desde={desde}
        hasta={hasta}
        alCambiarDesde={cambiarDesde}
        alCambiarHasta={cambiarHasta}
        alLimpiar={limpiarRango}
      />

      <section className="panel">
        <div className="panel-header">
          <div>
            <h3>Historial de mediciones</h3>
            <p className="muted">
              Los registros mas recientes primero. No se editan ni se borran.
            </p>
          </div>
        </div>
        <HistorialSignos
          registros={datos?.contenido ?? []}
          total={datos?.total ?? 0}
          pagina={datos?.pagina ?? pagina}
          totalPaginas={datos?.totalPaginas ?? 0}
          cargando={historial.cargando}
          error={historial.error}
          alCambiarPagina={setPagina}
          alReintentar={historial.recargar}
        />
      </section>
    </div>
  )
}
