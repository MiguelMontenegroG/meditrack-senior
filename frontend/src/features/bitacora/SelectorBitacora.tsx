// Pantalla /bitacora: selector de residente para abrir su bitacora de signos
// vitales. Los tres roles la ven; el backend filtra GET /pacientes por alcance,
// asi que el ADMINISTRADOR ve todo el centro, el CUIDADOR sus asignados y el
// FAMILIAR sus vinculados.
//
// Decision de diseno: NO se pide el "ultimo registro" de cada residente. Ese
// dato solo existe por residente (GET .../signos-vitales/ultimo), de modo que
// mostrarlo aqui exigiria una llamada por fila (N + 1). En su lugar la pantalla
// hace UNA sola consulta paginada y cada fila enlaza a su bitacora, donde se
// registran los signos.

import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Activity, ChevronRight } from 'lucide-react'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import { Paginacion } from '@/components/ui/Paginacion'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeRegistrarSignosVitales } from '@/features/bitacora/permisos'
import { listarPacientes } from '@/features/pacientes/services/pacienteService'
import type { PaginaPacientes } from '@/features/pacientes/types'

const TAMANO_PAGINA = 10

/** Selector de residente para abrir su bitacora de signos vitales. */
export function SelectorBitacora() {
  const { usuario } = useAutenticacion()
  const puedeRegistrar = puedeRegistrarSignosVitales(usuario?.rol)

  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaPacientes | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    // Una sola consulta para toda la pantalla: el listado ya viene filtrado por
    // el alcance del rol en el backend.
    listarPacientes({ activo: true, pagina, tamano: TAMANO_PAGINA })
      .then((resultado) => {
        if (vigente) setDatos(resultado)
      })
      .catch((fallo) => {
        if (vigente) setError(mensajeDeFallo(fallo))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [pagina, reintento])

  const residentes = useMemo(() => datos?.contenido ?? [], [datos])

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Bitacora</p>
          <h2>Signos vitales por residente</h2>
          <p className="muted">
            {puedeRegistrar
              ? 'Elige un residente para registrar signos vitales o consultar su historial.'
              : 'Elige un residente para consultar su historial de signos vitales.'}
          </p>
        </div>
      </div>

      {cargando && <EstadoCarga mensaje="Cargando residentes..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <button
              type="button"
              className="btn btn-outline"
              onClick={() => setReintento((n) => n + 1)}
            >
              Reintentar
            </button>
          }
        />
      )}
      {!cargando && !error && residentes.length === 0 && (
        <EstadoVacio
          titulo="Aun no hay residentes"
          descripcion="Cuando haya residentes activos en tu alcance podras abrir su bitacora."
        />
      )}
      {!cargando && !error && residentes.length > 0 && (
        <>
          <div className="listado-escritorio">
            <div className="table-wrap">
              <table className="tabla-signos">
                <caption className="sr-only">Residentes con bitacora de signos vitales</caption>
                <thead>
                  <tr>
                    <th scope="col">Residente</th>
                    <th scope="col">Habitacion</th>
                    <th scope="col">Estado</th>
                    <th scope="col">
                      <span className="sr-only">Acciones</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {residentes.map((residente) => (
                    <tr key={residente.id}>
                      <td>
                        <div className="resident-cell">
                          <strong>{residente.nombreCompleto}</strong>
                        </div>
                      </td>
                      <td>{residente.habitacion ?? 'Sin asignar'}</td>
                      <td>
                        <EtiquetaEstado tono={residente.activo ? 'success' : 'neutral'}>
                          {residente.activo ? 'Activo' : 'Inactivo'}
                        </EtiquetaEstado>
                      </td>
                      <td>
                        <Link
                          className="enlace-bitacora"
                          to={`/residentes/${residente.id}/signos-vitales`}
                          aria-label={`Abrir la bitacora de ${residente.nombreCompleto}`}
                        >
                          <Activity size={16} aria-hidden="true" /> Bitacora
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="listado-movil">
            <ul className="tarjetas-residentes">
              {residentes.map((residente) => (
                <li key={residente.id}>
                  <Link
                    className="tarjeta-residente"
                    to={`/residentes/${residente.id}/signos-vitales`}
                  >
                    <span className="tarjeta-residente-info">
                      <strong>{residente.nombreCompleto}</strong>
                      <small>
                        {residente.edad} años - {residente.habitacion ?? 'Sin habitacion'}
                      </small>
                      <EtiquetaEstado tono={residente.activo ? 'success' : 'neutral'}>
                        {residente.activo ? 'Activo' : 'Inactivo'}
                      </EtiquetaEstado>
                    </span>
                    <ChevronRight size={20} aria-hidden="true" />
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'residente', plural: 'residentes' }}
              etiqueta="Paginacion de residentes con bitacora"
              alCambiarPagina={setPagina}
            />
          )}
        </>
      )}
    </div>
  )
}
