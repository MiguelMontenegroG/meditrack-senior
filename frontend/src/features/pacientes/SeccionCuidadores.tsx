// Seccion de cuidadores asignados al residente. Solo se muestra al
// ADMINISTRADOR. Asignar un cuidador; desasignar con confirmacion.

import { useState } from 'react'
import { Plus, UserMinus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import type { AsignacionCuidador } from '@/features/pacientes/types'
import { SelectorUsuarios } from '@/features/pacientes/SelectorUsuarios'
import {
  asignarCuidador,
  desasignarCuidador,
} from '@/features/pacientes/services/cuidadorService'

interface PropsSeccionCuidadores {
  pacienteId: number
  cuidadores: AsignacionCuidador[]
  /** Nombres por usuarioId para mostrar (los carga el detalle). */
  nombresPorUsuario: Record<number, string>
  alCambiar: () => Promise<void>
}

/** Gestion de cuidadores asignados (solo ADMINISTRADOR). */
export function SeccionCuidadores({
  pacienteId,
  cuidadores,
  nombresPorUsuario,
  alCambiar,
}: PropsSeccionCuidadores) {
  const [usuarioId, setUsuarioId] = useState('')
  const [porDesasignar, setPorDesasignar] = useState<AsignacionCuidador | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const asignar = async () => {
    if (!usuarioId || enviando) return
    setError(null)
    setEnviando(true)
    try {
      await asignarCuidador(pacienteId, Number(usuarioId))
      setUsuarioId('')
      await alCambiar()
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    } finally {
      setEnviando(false)
    }
  }

  const desasignar = async () => {
    if (!porDesasignar) return
    try {
      await desasignarCuidador(pacienteId, porDesasignar.usuarioId)
      setPorDesasignar(null)
      await alCambiar()
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    }
  }

  const nombreDe = (a: AsignacionCuidador) =>
    nombresPorUsuario[a.usuarioId] ?? `Usuario #${a.usuarioId}`

  return (
    <section className="panel" aria-label="Cuidadores asignados">
      <div className="panel-header">
        <h3>Cuidadores asignados</h3>
      </div>

      {cuidadores.length === 0 && (
        <p className="muted panel-vacio">Sin cuidadores asignados.</p>
      )}

      <ul className="lista-usuarios">
        {cuidadores.map((a) => (
          <li key={a.id} className="usuario-item">
            <span className="contacto-datos">
              <strong>{nombreDe(a)}</strong>
              <small>Desde {a.desde}</small>
            </span>
            <EtiquetaEstado tono={a.hasta ? 'neutral' : 'success'}>
              {a.hasta ? 'Cerrada' : 'Vigente'}
            </EtiquetaEstado>
            <button
              type="button"
              className="row-action"
              aria-label={`Desasignar a ${nombreDe(a)}`}
              onClick={() => setPorDesasignar(a)}
            >
              <UserMinus size={17} />
            </button>
          </li>
        ))}
      </ul>

      <div className="subformulario">
        <h4>Asignar cuidador</h4>
        <SelectorUsuarios
          rol="CUIDADOR_ENFERMERO"
          excluirIds={cuidadores.filter((a) => !a.hasta).map((a) => a.usuarioId)}
          valor={usuarioId}
          alCambiar={setUsuarioId}
        />
        <BotonAccion onClick={asignar} disabled={enviando || !usuarioId}>
          <Plus size={16} /> {enviando ? 'Asignando...' : 'Asignar'}
        </BotonAccion>
      </div>

      {porDesasignar && (
        <div className="confirmacion" role="alertdialog" aria-label="Confirmar desasignación">
          <p>¿Desasignar a "{nombreDe(porDesasignar)}"?</p>
          <div className="form-acciones">
            <BotonAccion variant="danger" onClick={desasignar}>
              Sí, desasignar
            </BotonAccion>
            <BotonAccion variant="outline" onClick={() => setPorDesasignar(null)}>
              Cancelar
            </BotonAccion>
          </div>
        </div>
      )}

      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
    </section>
  )
}
