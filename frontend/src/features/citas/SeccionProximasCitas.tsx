// Seccion "Proximas citas" del detalle de un residente. Muestra hasta 5 citas
// PROGRAMADAS futuras (lectura segun alcance) y un enlace al calendario filtrado
// por ese residente. Un fallo no rompe el detalle: se muestra el error y se
// permite reintentar.

import { Link } from 'react-router-dom'
import { CalendarPlus, ChevronRight } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeAgendarCitas } from '@/features/citas/permisos'
import { EtiquetaEstadoCita } from '@/features/citas/EtiquetaEstadoCita'
import { fechaHoraBogota } from '@/features/citas/fechas'
import { useProximasCitas } from '@/features/citas/useProximasCitas'

interface PropsSeccionProximasCitas {
  pacienteId: number
}

/** Proximas citas del residente con enlace al calendario. */
export function SeccionProximasCitas({ pacienteId }: PropsSeccionProximasCitas) {
  const { usuario } = useAutenticacion()
  const { citas, cargando, error, recargar } = useProximasCitas(pacienteId)
  const puedeAgendar = puedeAgendarCitas(usuario?.rol)

  return (
    <section className="panel seccion-proximas-citas">
      <div className="panel-header">
        <div>
          <h3>Proximas citas</h3>
          <p className="muted">Las siguientes citas programadas en los proximos 30 dias.</p>
        </div>
        <span className="kpi-icon kpi-blue">
          <CalendarPlus size={18} />
        </span>
      </div>

      {cargando && <EstadoCarga mensaje="Cargando citas..." />}
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
      {!cargando && !error && citas.length === 0 && (
        <p className="muted seccion-vacia">No hay citas programadas proximas.</p>
      )}
      {!cargando && !error && citas.length > 0 && (
        <ul className="lista-proximas-citas">
          {citas.map((cita) => (
            <li key={cita.id} className="fila-proxima-cita">
              <div>
                <strong>{fechaHoraBogota(cita.fechaHora)}</strong>
                <span>
                  {cita.especialidadNombre ?? 'Visita general'}
                  {cita.profesional ? ` · ${cita.profesional}` : ''}
                </span>
              </div>
              <EtiquetaEstadoCita estado={cita.estado} />
            </li>
          ))}
        </ul>
      )}

      <div className="panel-footer acciones-proximas-citas">
        <Link className="text-link" to={`/calendario?pacienteId=${pacienteId}`}>
          Ver en el calendario <ChevronRight size={15} />
        </Link>
        {puedeAgendar && (
          <Link className="text-link" to={`/calendario/nuevo?pacienteId=${pacienteId}`}>
            Agendar cita <CalendarPlus size={15} />
          </Link>
        )}
      </div>
    </section>
  )
}