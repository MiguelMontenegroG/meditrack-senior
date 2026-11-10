// Elemento visual de una alerta (tarjeta en movil). Muestra tipo, mensaje,
// severidad, estado y fecha, y el boton "Atender" solo si el rol puede hacerlo
// y la alerta sigue ACTIVA. No conoce el servicio: recibe callbacks.

import { AlertTriangle, Bell } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import {
  ETIQUETA_ESTADO_ALERTA,
  ETIQUETA_TIPO_ALERTA,
  TONO_ESTADO_ALERTA,
  TONO_SEVERIDAD,
} from '@/features/alertas/etiquetas'
import type { Alerta } from '@/features/alertas/types'
import { fechaHoraBogota } from '@/lib/fechas-bogota'

interface PropsTarjetaAlerta {
  alerta: Alerta
  /** Si es true, muestra el boton "Atender" (rol con permiso). */
  puedeAtender: boolean
  /** Handler al confirmar la atencion; recibe el id de la alerta. */
  alAtender: (id: number) => void
  /** true mientras esa alerta se esta enviando (bloquea el boton). */
  atendiendo: boolean
}

/** Icono de cabecera segun la severidad. */
function IconoSeveridad({ severidad }: { severidad: Alerta['severidad'] }) {
  if (severidad === 'INFO') return <Bell size={17} />
  return <AlertTriangle size={17} />
}

/** Tarjeta de una alerta para la vista movil. */
export function TarjetaAlerta({ alerta, puedeAtender, alAtender, atendiendo }: PropsTarjetaAlerta) {
  const mostrarBoton = puedeAtender && alerta.estado === 'ACTIVA'
  return (
    <article className="tarjeta-alerta">
      <div className="tarjeta-alerta-cabecera">
        <span className={`alert-symbol ${TONO_SEVERIDAD[alerta.severidad]}`}>
          <IconoSeveridad severidad={alerta.severidad} />
        </span>
        <div className="alert-copy">
          <strong>{ETIQUETA_TIPO_ALERTA[alerta.tipo]}</strong>
          <span>{alerta.mensaje}</span>
        </div>
      </div>
      <div className="tarjeta-alerta-meta">
        <EtiquetaEstado tono={TONO_SEVERIDAD[alerta.severidad]}>
          {alerta.severidad === 'ALTA' ? 'Severidad alta' : `Severidad ${alerta.severidad.toLowerCase()}`}
        </EtiquetaEstado>
        <EtiquetaEstado tono={TONO_ESTADO_ALERTA[alerta.estado]}>
          {ETIQUETA_ESTADO_ALERTA[alerta.estado]}
        </EtiquetaEstado>
        <time dateTime={alerta.creadoEn}>{fechaHoraBogota(alerta.creadoEn)}</time>
      </div>
      {mostrarBoton && (
        <button
          type="button"
          className="btn btn-primary tarjeta-alerta-accion"
          onClick={() => alAtender(alerta.id)}
          disabled={atendiendo}
        >
          {atendiendo ? 'Atendiendo...' : 'Atender'}
        </button>
      )}
    </article>
  )
}
