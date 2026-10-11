// Encabezado del detalle del residente. Presenta el nombre, un par de datos
// clave y las acciones disponibles (volver, editar y activar/desactivar).
// Solo presenta; los permisos y el estado de los datos llegan ya resueltos.

import { ArrowLeft, Pencil } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import type { PacienteDetalle } from '@/features/pacientes/types'
import { BotonEstadoResidente } from '@/features/pacientes/BotonEstadoResidente'

interface PropsCabeceraDetalle {
  residente: PacienteDetalle
  /** Muestra las acciones de escritura (editar y activar/desactivar). */
  puedeGestionar: boolean
  alVolver: () => void
  alEditar: () => void
  alCambiarEstado: () => Promise<void>
}

/** Tarjeta de encabezado del residente con sus acciones principales. */
export function CabeceraDetalle({
  residente,
  puedeGestionar,
  alVolver,
  alEditar,
  alCambiarEstado,
}: PropsCabeceraDetalle) {
  return (
    <header className="cabecera-detalle">
      <div className="cabecera-detalle-info">
        <h2>{residente.nombreCompleto}</h2>
        <div className="cabecera-detalle-meta">
          <span className="dato">
            <span>Documento</span>
            <strong>{residente.documento}</strong>
          </span>
          <span className="dato">
            <span>Edad</span>
            <strong>{residente.edad} años</strong>
          </span>
          <span className="dato">
            <span>Habitación</span>
            <strong>{residente.habitacion ?? 'Sin asignar'}</strong>
          </span>
          <EtiquetaEstado tono={residente.activo ? 'success' : 'neutral'}>
            {residente.activo ? 'Activo' : 'Inactivo'}
          </EtiquetaEstado>
        </div>
      </div>
      <div className="cabecera-detalle-acciones">
        <BotonAccion variant="ghost" onClick={alVolver}>
          <ArrowLeft size={16} /> Volver
        </BotonAccion>
        {puedeGestionar && (
          <BotonAccion onClick={alEditar}>
            <Pencil size={16} /> Editar
          </BotonAccion>
        )}
        {puedeGestionar && (
          <BotonEstadoResidente
            pacienteId={residente.id}
            activo={residente.activo}
            alCambiar={alCambiarEstado}
          />
        )}
      </div>
    </header>
  )
}
