// Seccion de contactos de emergencia dentro del detalle del residente.
// Lectura para todos; alta, edicion y borrado solo para el ADMINISTRADOR.

import { useState } from 'react'
import { Plus, Star, Trash2, Pencil } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import type { ContactoEmergencia, DatosContacto } from '@/features/pacientes/types'
import { FormularioContacto } from '@/features/pacientes/FormularioContacto'
import {
  actualizarContacto,
  crearContacto,
  eliminarContacto,
} from '@/features/pacientes/services/contactoService'

interface PropsSeccionContactos {
  pacienteId: number
  contactos: ContactoEmergencia[]
  puedeGestionar: boolean
  /** Recarga el detalle tras un cambio (el backend resuelve el principal). */
  alCambiar: () => Promise<void>
}

/** Lista de contactos con gestion (ADMIN) y confirmacion de borrado. */
export function SeccionContactos({
  pacienteId,
  contactos,
  puedeGestionar,
  alCambiar,
}: PropsSeccionContactos) {
  const [editando, setEditando] = useState<ContactoEmergencia | 'nuevo' | null>(null)
  const [porBorrar, setPorBorrar] = useState<ContactoEmergencia | null>(null)
  const [error, setError] = useState<string | null>(null)

  const guardar = async (datos: DatosContacto) => {
    if (editando === 'nuevo') {
      await crearContacto(pacienteId, datos)
    } else if (editando) {
      await actualizarContacto(pacienteId, editando.id, datos)
    }
    setEditando(null)
    await alCambiar()
  }

  const confirmarBorrado = async () => {
    if (!porBorrar) return
    try {
      await eliminarContacto(pacienteId, porBorrar.id)
      setPorBorrar(null)
      await alCambiar()
    } catch (fallo) {
      setError(mensajeDeFallo(fallo))
    }
  }

  return (
    <section className="panel" aria-label="Contactos de emergencia">
      <div className="panel-header">
        <h3>Contactos de emergencia</h3>
        {puedeGestionar && (
          <BotonAccion variant="ghost" onClick={() => setEditando('nuevo')}>
            <Plus size={16} /> Añadir
          </BotonAccion>
        )}
      </div>

      {contactos.length === 0 && (
        <p className="muted panel-vacio">Sin contactos registrados.</p>
      )}

      <ul className="lista-contactos">
        {contactos.map((contacto) => (
          <li key={contacto.id} className="contacto-item">
            <span className="contacto-principal">
              <strong>{contacto.nombre}</strong>
              {contacto.parentesco && <span className="contacto-parentesco">{contacto.parentesco}</span>}
              <span className="contacto-telefono">{contacto.telefono}</span>
            </span>
            {contacto.principal && (
              <EtiquetaEstado tono="info">
                <Star size={12} /> Principal
              </EtiquetaEstado>
            )}
            {puedeGestionar && (
              <span className="contacto-acciones">
                <button
                  type="button"
                  className="row-action"
                  aria-label={`Editar contacto ${contacto.nombre}`}
                  onClick={() => setEditando(contacto)}
                >
                  <Pencil size={17} />
                </button>
                <button
                  type="button"
                  className="row-action"
                  aria-label={`Eliminar contacto ${contacto.nombre}`}
                  onClick={() => setPorBorrar(contacto)}
                >
                  <Trash2 size={17} />
                </button>
              </span>
            )}
          </li>
        ))}
      </ul>

      {editando && (
        <div className="subformulario">
          <h4>{editando === 'nuevo' ? 'Nuevo contacto' : 'Editar contacto'}</h4>
          <FormularioContacto
            contacto={editando === 'nuevo' ? undefined : editando}
            alGuardar={guardar}
            alCancelar={() => setEditando(null)}
          />
        </div>
      )}

      {porBorrar && (
        <div className="confirmacion" role="alertdialog" aria-label="Confirmar borrado">
          <p>¿Eliminar el contacto "{porBorrar.nombre}"?</p>
          <div className="form-acciones">
            <BotonAccion variant="danger" onClick={confirmarBorrado}>
              Sí, eliminar
            </BotonAccion>
            <BotonAccion variant="outline" onClick={() => setPorBorrar(null)}>
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
