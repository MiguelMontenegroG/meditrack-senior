// Seccion de familiares vinculados al residente. Solo se muestra al
// ADMINISTRADOR. Vincular con parentesco; revocar con confirmacion.

import { useState } from 'react'
import { Plus, UserMinus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import type { VinculacionFamiliar } from '@/features/pacientes/types'
import { SelectorUsuarios } from '@/features/pacientes/SelectorUsuarios'
import {
  revocarFamiliar,
  vincularFamiliar,
} from '@/features/pacientes/services/familiarService'

interface PropsSeccionFamiliares {
  pacienteId: number
  familiares: VinculacionFamiliar[]
  /** Nombres por usuarioId para mostrar (los carga el detalle). */
  nombresPorUsuario: Record<number, string>
  alCambiar: () => Promise<void>
}

/** Gestion de familiares autorizados (solo ADMINISTRADOR). */
export function SeccionFamiliares({
  pacienteId,
  familiares,
  nombresPorUsuario,
  alCambiar,
}: PropsSeccionFamiliares) {
  const [usuarioId, setUsuarioId] = useState('')
  const [parentesco, setParentesco] = useState('')
  const [porRevocar, setPorRevocar] = useState<VinculacionFamiliar | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const vincular = async () => {
    if (!usuarioId || enviando) return
    setError(null)
    setEnviando(true)
    try {
      await vincularFamiliar(pacienteId, Number(usuarioId), parentesco.trim() || null)
      setUsuarioId('')
      setParentesco('')
      await alCambiar()
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    } finally {
      setEnviando(false)
    }
  }

  const revocar = async () => {
    if (!porRevocar) return
    try {
      await revocarFamiliar(pacienteId, porRevocar.usuarioId)
      setPorRevocar(null)
      await alCambiar()
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    }
  }

  const nombreDe = (v: VinculacionFamiliar) =>
    nombresPorUsuario[v.usuarioId] ?? `Usuario #${v.usuarioId}`

  return (
    <section className="panel" aria-label="Familiares vinculados">
      <div className="panel-header">
        <h3>Familiares autorizados</h3>
      </div>

      {familiares.length === 0 && (
        <p className="muted panel-vacio">Sin familiares vinculados.</p>
      )}

      <ul className="lista-usuarios">
        {familiares.map((v) => (
          <li key={v.id} className="usuario-item">
            <span className="contacto-datos">
              <strong>{nombreDe(v)}</strong>
              <small>{v.parentesco ?? 'Sin parentesco'}</small>
            </span>
            <EtiquetaEstado tono={v.autorizado ? 'success' : 'neutral'}>
              {v.autorizado ? 'Autorizado' : 'Revocado'}
            </EtiquetaEstado>
            <button
              type="button"
              className="row-action"
              aria-label={`Revocar acceso a ${nombreDe(v)}`}
              onClick={() => setPorRevocar(v)}
            >
              <UserMinus size={17} />
            </button>
          </li>
        ))}
      </ul>

      <div className="subformulario">
        <h4>Vincular familiar</h4>
        <SelectorUsuarios
          rol="FAMILIAR_AUTORIZADO"
          excluirIds={familiares.filter((v) => v.autorizado).map((v) => v.usuarioId)}
          valor={usuarioId}
          alCambiar={setUsuarioId}
        />
        <label>
          Parentesco
          <input
            value={parentesco}
            onChange={(e) => setParentesco(e.target.value)}
            maxLength={60}
            autoComplete="off"
          />
        </label>
        <BotonAccion onClick={vincular} disabled={enviando || !usuarioId}>
          <Plus size={16} /> {enviando ? 'Vinculando...' : 'Vincular'}
        </BotonAccion>
      </div>

      {porRevocar && (
        <div className="confirmacion" role="alertdialog" aria-label="Confirmar revocación">
          <p>¿Revocar el acceso de "{nombreDe(porRevocar)}"?</p>
          <div className="form-acciones">
            <BotonAccion variant="danger" onClick={revocar}>
              Sí, revocar
            </BotonAccion>
            <BotonAccion variant="outline" onClick={() => setPorRevocar(null)}>
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
