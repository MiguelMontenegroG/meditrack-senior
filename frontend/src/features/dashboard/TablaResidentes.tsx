import { MoreHorizontal, Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { residentesAtencion } from '@/features/dashboard/mock-data'

// Subpanel con la tabla de residentes que requieren atencion hoy.

/** Tabla de residentes priorizados por alertas y tareas pendientes. */
export function TablaResidentes() {
  return (
    <section className="panel residents-table">
      <div className="panel-header">
        <div>
          <h3>Residentes que requieren atencion hoy</h3>
          <p className="muted">Priorizados por alertas y tareas pendientes</p>
        </div>
        <BotonAccion variant="outline" className="desktop-only">
          <Plus size={16} /> Registrar residente
        </BotonAccion>
      </div>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Residente</th>
              <th>Habitacion</th>
              <th>Situacion</th>
              <th>Estado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {residentesAtencion.map((residente) => (
              <tr key={residente.nombre}>
                <td>
                  <div className="resident-cell">
                    <span className="avatar avatar-soft">
                      {residente.nombre
                        .split(' ')
                        .map((parte) => parte[0])
                        .slice(0, 2)
                        .join('')}
                    </span>
                    <span>
                      <strong>{residente.nombre}</strong>
                      <small>{residente.edad}</small>
                    </span>
                  </div>
                </td>
                <td>{residente.habitacion}</td>
                <td>{residente.situacion}</td>
                <td>
                  <EtiquetaEstado tono={residente.tono}>{residente.estado}</EtiquetaEstado>
                </td>
                <td>
                  <button className="row-action" aria-label={`Opciones de ${residente.nombre}`}>
                    <MoreHorizontal size={18} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
