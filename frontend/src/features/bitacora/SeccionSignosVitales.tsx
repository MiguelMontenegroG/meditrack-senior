// Seccion "Ultimos signos vitales" del detalle de un residente. Muestra el
// registro mas reciente (lectura segun alcance) y un enlace a la bitacora
// completa. Si el residente no tiene registros, el backend responde 404 y aqui
// se presenta como estado vacio, no como error; un fallo real si permite
// reintentar sin romper el detalle.

import { Link } from 'react-router-dom'
import { Activity, ChevronRight } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { fechaHoraBogota } from '@/lib/fechas-bogota'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeRegistrarSignosVitales } from '@/features/bitacora/permisos'
import {
  textoGlucosa,
  textoPresion,
  textoTemperatura,
} from '@/features/bitacora/signosVitales'
import { useUltimoRegistro } from '@/features/bitacora/useUltimoRegistro'
import type { SignosVitales } from '@/features/bitacora/types'

interface PropsSeccionSignosVitales {
  pacienteId: number
}

/** Ficha con fecha, presion, glucosa y temperatura de un registro. */
function UltimoRegistro({ registro }: { registro: SignosVitales }) {
  return (
    <>
      <p className="muted">{fechaHoraBogota(registro.fechaHora)}</p>
      <div className="vitals-grid">
        <div>
          <Activity size={20} />
          <strong>{textoPresion(registro) ?? '—'}</strong>
          <span>Presion</span>
        </div>
        <div>
          <Activity size={20} />
          <strong>{textoGlucosa(registro) ?? '—'}</strong>
          <span>Glucosa</span>
        </div>
        <div>
          <Activity size={20} />
          <strong>{textoTemperatura(registro) ?? '—'}</strong>
          <span>Temperatura</span>
        </div>
      </div>
      {registro.observaciones && (
        <p className="muted seccion-observaciones">{registro.observaciones}</p>
      )}
    </>
  )
}

/** Ultimo registro de signos vitales del residente con acceso a la bitacora. */
export function SeccionSignosVitales({ pacienteId }: PropsSeccionSignosVitales) {
  const { usuario } = useAutenticacion()
  const { registro, cargando, error, recargar } = useUltimoRegistro(pacienteId)
  const puedeRegistrar = puedeRegistrarSignosVitales(usuario?.rol)

  return (
    <section className="panel seccion-signos-vitales">
      <div className="panel-header">
        <div>
          <h3>Ultimos signos vitales</h3>
          <p className="muted">La medicion mas reciente registrada para el residente.</p>
        </div>
        <span className="kpi-icon kpi-green">
          <Activity size={18} />
        </span>
      </div>

      {cargando && <EstadoCarga mensaje="Cargando signos vitales..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <BotonAccion variant="outline" onClick={recargar}>
              Reintentar
            </BotonAccion>
          }
        />
      )}
      {!cargando && !error && !registro && (
        <p className="muted seccion-vacia">Aun no hay signos vitales registrados.</p>
      )}
      {!cargando && !error && registro && <UltimoRegistro registro={registro} />}

      <div className="panel-footer acciones-proximas-citas">
        <Link className="text-link" to={`/residentes/${pacienteId}/signos-vitales`}>
          Ver bitacora completa <ChevronRight size={15} />
        </Link>
        {puedeRegistrar && (
          <span className="muted">Puedes registrar nuevos signos desde la bitacora.</span>
        )}
      </div>
    </section>
  )
}
