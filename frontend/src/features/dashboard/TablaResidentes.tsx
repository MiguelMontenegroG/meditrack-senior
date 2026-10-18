import { useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EnConstruccion } from '@/components/ui/EnConstruccion'
import { useResumenOperativo } from '@/features/dashboard/useResumenOperativo'

// Subpanel con los residentes que tienen tomas abiertas hoy. Antes mostraba
// una tabla fija con residentes inventados; ahora lista las dosis reales
// pendientes o atrasadas del dia (agenda del backend). El boton de alta lleva
// al formulario real en lugar de no hacer nada.

/** Residentes con tomas pendientes o atrasadas hoy. */
export function TablaResidentes() {
  const navegar = useNavigate()
  const { tomasAbiertas, cargando } = useResumenOperativo()

  // Una fila por residente, con cuantas tomas abiertas tiene hoy.
  const porResidente = new Map<number, { nombre: string; abiertas: number; atrasadas: number }>()
  for (const toma of tomasAbiertas) {
    const actual = porResidente.get(toma.pacienteId) ?? {
      nombre: toma.pacienteNombre,
      abiertas: 0,
      atrasadas: 0,
    }
    actual.abiertas += 1
    if (toma.estado === 'ATRASADA') actual.atrasadas += 1
    porResidente.set(toma.pacienteId, actual)
  }

  return (
    <section className="panel residents-table">
      <div className="panel-header">
        <div>
          <h3>Residentes con tomas abiertas hoy</h3>
          <p className="muted">Con dosis pendientes o atrasadas del dia</p>
        </div>
        <BotonAccion
          variant="outline"
          className="desktop-only"
          onClick={() => navegar('/residentes/nuevo')}
        >
          <Plus size={16} /> Registrar residente
        </BotonAccion>
      </div>

      {cargando && (
        <p className="muted panel-vacio">Cargando residentes con tomas abiertas...</p>
      )}

      {!cargando && porResidente.size === 0 && <EnConstruccion titulo="Sin tomas abiertas" />}

      {!cargando && porResidente.size > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Residente</th>
                <th>Tomas abiertas</th>
                <th>Atrasadas</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {[...porResidente.entries()].map(([pacienteId, datos]) => (
                <tr key={pacienteId}>
                  <td>
                    <strong>{datos.nombre}</strong>
                  </td>
                  <td>{datos.abiertas}</td>
                  <td>{datos.atrasadas}</td>
                  <td>
                    <BotonAccion
                      variant="outline"
                      onClick={() => navegar(`/residentes/${pacienteId}/tomas`)}
                    >
                      Ver agenda
                    </BotonAccion>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
