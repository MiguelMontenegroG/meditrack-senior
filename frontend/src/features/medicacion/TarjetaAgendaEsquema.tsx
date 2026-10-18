// Tarjeta de un esquema dentro de la agenda del dia. Muestra sus slots con la
// hora y el estado calculado; si el usuario puede registrar y el slot esta
// pendiente/atrasado, ofrece registrar la toma.

import { useState } from 'react'
import { Clock3 } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstadoSlot } from '@/features/medicacion/EtiquetaEstadoToma'
import { RegistrarTomaForm } from '@/features/medicacion/RegistrarTomaForm'
import { horaLocal } from '@/features/medicacion/fechas'
import type { AgendaEsquema } from '@/features/medicacion/types'

interface PropsTarjetaAgendaEsquema {
  pacienteId: number
  agenda: AgendaEsquema
  /** Si puede registrar tomas (ADMINISTRADOR o CUIDADOR_ENFERMERO). */
  puedeRegistrar: boolean
  /** Recarga la agenda tras registrar una toma. */
  alRegistrar: () => Promise<void>
}

/** Agenda de un esquema con el estado de cada slot del dia. */
export function TarjetaAgendaEsquema({
  pacienteId,
  agenda,
  puedeRegistrar,
  alRegistrar,
}: PropsTarjetaAgendaEsquema) {
  const [registrando, setRegistrando] = useState<string | null>(null)

  const puedeRegistrarSlot = (estado: string) =>
    estado === 'PENDIENTE' || estado === 'ATRASADA'

  return (
    <section className="panel" aria-label={`Agenda de ${agenda.medicamentoNombre ?? 'medicamento'}`}>
      <div className="panel-header">
        <div>
          <h3>{agenda.medicamentoNombre ?? `Medicamento #${agenda.medicamentoId}`}</h3>
          <p className="muted">{agenda.dosis}</p>
        </div>
      </div>

      {agenda.slots.length === 0 && (
        <p className="muted panel-vacio">Sin tomas programadas para hoy.</p>
      )}

      <ul className="lista-slots">
        {agenda.slots.map((slot) => (
          <li key={slot.fechaHoraProgramada} className="slot-toma">
            <span className="slot-hora">
              <Clock3 size={16} aria-hidden="true" />
              <strong>{horaLocal(slot.fechaHoraProgramada)}</strong>
            </span>
            <EtiquetaEstadoSlot estado={slot.estado} />
            {puedeRegistrar && puedeRegistrarSlot(slot.estado) && (
              <BotonRegistrar
                activo={registrando === slot.fechaHoraProgramada}
                alAlternar={() =>
                  setRegistrando((prev) =>
                    prev === slot.fechaHoraProgramada ? null : slot.fechaHoraProgramada,
                  )
                }
              />
            )}
            {registrando === slot.fechaHoraProgramada && (
              <RegistrarTomaForm
                pacienteId={pacienteId}
                esquemaId={agenda.esquemaId}
                fechaHoraProgramada={slot.fechaHoraProgramada}
                etiquetaHora={horaLocal(slot.fechaHoraProgramada)}
                alCerrar={() => setRegistrando(null)}
                alRegistrar={async () => {
                  setRegistrando(null)
                  await alRegistrar()
                }}
              />
            )}
          </li>
        ))}
      </ul>
    </section>
  )
}

function BotonRegistrar({ activo, alAlternar }: { activo: boolean; alAlternar: () => void }) {
  return (
    <BotonAccion variant="outline" onClick={alAlternar}>
      {activo ? 'Cerrar' : 'Registrar'}
    </BotonAccion>
  )
}
