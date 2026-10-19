// Tarjeta de una cita para movil. Muestra hora, residente, especialidad,
// profesional, lugar, motivo y estado, con las acciones segun el rol.

import { EtiquetaEstadoCita } from '@/features/citas/EtiquetaEstadoCita'
import { AccionesCita } from '@/features/citas/AccionesCita'
import { horaBogota } from '@/features/citas/fechas'
import type { CitaDetalle } from '@/features/citas/types'
import type { RolUsuario } from '@/features/auth/types'

interface PropsTarjetaCita {
  cita: CitaDetalle
  nombreResidente: string
  rol: RolUsuario | undefined
  alEditar: (cita: CitaDetalle) => void
  alCambiarEstado: () => void
}

/** Tarjeta de una cita (vista movil). */
export function TarjetaCita({
  cita,
  nombreResidente,
  rol,
  alEditar,
  alCambiarEstado,
}: PropsTarjetaCita) {
  return (
    <article className="tarjeta-cita">
      <header className="tarjeta-cita-cabecera">
        <time className="tarjeta-cita-hora">{horaBogota(cita.fechaHora)}</time>
        <EtiquetaEstadoCita estado={cita.estado} />
      </header>
      <p className="tarjeta-cita-residente">{nombreResidente}</p>
      <p className="muted tarjeta-cita-detalle">
        {cita.especialidadNombre ?? 'Visita general'}
        {cita.profesional ? ` · ${cita.profesional}` : ''}
        {cita.lugar ? ` · ${cita.lugar}` : ''}
      </p>
      {cita.motivo && <p className="tarjeta-cita-motivo">{cita.motivo}</p>}
      <AccionesCita
        cita={cita}
        rol={rol}
        alEditar={alEditar}
        alCambiarEstado={alCambiarEstado}
      />
    </article>
  )
}