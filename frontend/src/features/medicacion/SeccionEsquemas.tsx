// Seccion de esquemas de medicacion del residente, embebida en su detalle.
// Lectura para cualquier rol con alcance; al ADMINISTRADOR se le ofrecen los
// controles de crear, editar y suspender/activar.

import { useNavigate } from 'react-router-dom'
import { CalendarDays, Pencil, Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { ETIQUETA_VIA } from '@/features/medicacion/etiquetas'
import { BotonEstadoEsquema } from '@/features/medicacion/BotonEstadoEsquema'
import type { EsquemaMedicacion } from '@/features/medicacion/types'

interface PropsSeccionEsquemas {
  pacienteId: number
  esquemas: EsquemaMedicacion[]
  /** Muestra las acciones de escritura (solo ADMINISTRADOR). */
  puedeGestionar: boolean
  /** Recarga el detalle del residente tras un cambio. */
  alCambiar: () => Promise<void>
}

/** Lista de esquemas de medicacion del residente con sus horarios. */
export function SeccionEsquemas({
  pacienteId,
  esquemas,
  puedeGestionar,
  alCambiar,
}: PropsSeccionEsquemas) {
  const navegar = useNavigate()

  return (
    <section className="panel" aria-label="Esquemas de medicacion">
      <div className="panel-header">
        <div>
          <h3>Esquemas de medicacion</h3>
          <p className="muted">Tratamientos activos con dosis y horarios.</p>
        </div>
        <div className="panel-acciones">
          <BotonAccion variant="ghost" onClick={() => navegar(`/residentes/${pacienteId}/tomas`)}>
            <CalendarDays size={16} /> Agenda
          </BotonAccion>
          {puedeGestionar && (
            <BotonAccion onClick={() => navegar(`/residentes/${pacienteId}/esquemas/nuevo`)}>
              <Plus size={16} /> Nuevo esquema
            </BotonAccion>
          )}
        </div>
      </div>

      {esquemas.length === 0 && (
        <p className="muted panel-vacio">Sin esquemas de medicacion registrados.</p>
      )}

      {esquemas.length > 0 && (
        <ul className="lista-esquemas">
          {esquemas.map((esquema) => (
            <li key={esquema.id} className="esquema-item">
              <div className="esquema-cabecera">
                <span className="contacto-datos">
                  <strong>
                    {esquema.medicamentoNombre ?? `Medicamento #${esquema.medicamentoId}`}
                  </strong>
                  <small>
                    {esquema.dosis} · cada {esquema.frecuenciaHoras} h · {ETIQUETA_VIA[esquema.via]}
                  </small>
                </span>
                <EtiquetaEstado
                  tono={!esquema.activo ? 'neutral' : esquema.vigente ? 'success' : 'warning'}
                >
                  {!esquema.activo ? 'Suspendido' : esquema.vigente ? 'Vigente' : 'Fuera de vigencia'}
                </EtiquetaEstado>
              </div>
              <div className="esquema-detalle">
                <span>
                  <small>Horarios</small>
                  <strong>{esquema.horarios.join(' · ') || 'Sin horarios'}</strong>
                </span>
                <span>
                  <small>Vigencia</small>
                  <strong>
                    {esquema.fechaInicio}
                    {esquema.fechaFin ? ` — ${esquema.fechaFin}` : ' — abierta'}
                  </strong>
                </span>
              </div>
              {esquema.indicaciones && (
                <p className="muted">{esquema.indicaciones}</p>
              )}
              {puedeGestionar && (
                <div className="esquema-acciones">
                  <BotonAccion
                    variant="outline"
                    onClick={() => navegar(`/residentes/${pacienteId}/esquemas/${esquema.id}/editar`)}
                  >
                    <Pencil size={16} /> Editar
                  </BotonAccion>
                  <BotonEstadoEsquema
                    pacienteId={pacienteId}
                    esquemaId={esquema.id}
                    activo={esquema.activo}
                    alCambiar={alCambiar}
                  />
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
