// Tabla de citas para escritorio. Muestra hora, residente, especialidad,
// profesional, lugar, motivo, estado y acciones segun el rol.

import { EtiquetaEstadoCita } from '@/features/citas/EtiquetaEstadoCita'
import { AccionesCita } from '@/features/citas/AccionesCita'
import { horaBogota } from '@/features/citas/fechas'
import type { CitaDetalle } from '@/features/citas/types'
import type { RolUsuario } from '@/features/auth/types'

interface PropsTablaCitas {
  citas: CitaDetalle[]
  nombrePorResidente: (pacienteId: number) => string
  rol: RolUsuario | undefined
  alEditar: (cita: CitaDetalle) => void
  alCambiarEstado: () => void
}

/** Tabla de citas (vista de escritorio). */
export function TablaCitas({
  citas,
  nombrePorResidente,
  rol,
  alEditar,
  alCambiarEstado,
}: PropsTablaCitas) {
  return (
    <table className="tabla-citas">
      <thead>
        <tr>
          <th scope="col">Hora</th>
          <th scope="col">Residente</th>
          <th scope="col">Especialidad</th>
          <th scope="col">Profesional</th>
          <th scope="col">Lugar</th>
          <th scope="col">Motivo</th>
          <th scope="col">Estado</th>
          <th scope="col">Acciones</th>
        </tr>
      </thead>
      <tbody>
        {citas.map((cita) => (
          <tr key={cita.id}>
            <td>{horaBogota(cita.fechaHora)}</td>
            <td>{nombrePorResidente(cita.pacienteId)}</td>
            <td>{cita.especialidadNombre ?? 'Visita general'}</td>
            <td>{cita.profesional ?? '—'}</td>
            <td>{cita.lugar ?? '—'}</td>
            <td className="celda-motivo">{cita.motivo ?? '—'}</td>
            <td>
              <EtiquetaEstadoCita estado={cita.estado} />
            </td>
            <td>
              <AccionesCita
                cita={cita}
                rol={rol}
                alEditar={alEditar}
                alCambiarEstado={alCambiarEstado}
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}