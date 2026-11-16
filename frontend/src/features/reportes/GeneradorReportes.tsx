// Pantalla /reportes: generador de PDF por residente (de su alcance) y
// historial de reportes generados. Los tres roles pueden generar; el backend
// aplica el alcance y responde 404 si el residente no es visible. El PDF se
// genera al momento y se descarga como blob (el token viaja en cabecera).

import { useState } from 'react'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { Paginacion } from '@/components/ui/Paginacion'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { useResidentesAlcance } from '@/features/citas/useResidentesAlcance'
import { puedeGenerarReportes } from '@/features/reportes/permisos'
import { FormularioReporte } from '@/features/reportes/FormularioReporte'
import { TablaReportes } from '@/features/reportes/TablaReportes'
import { useGenerarReporte } from '@/features/reportes/useGenerarReporte'
import { useHistorialReportes } from '@/features/reportes/useHistorialReportes'
import type { TipoReporte } from '@/features/reportes/types'
import { hoyBogota, sumarDias } from '@/lib/fechas-bogota'

const TAMANO_HISTORIAL = 10
const DIAS_POR_DEFECTO = 30

/** Pantalla de generacion de reportes y historial. */
export function GeneradorReportes() {
  const { usuario } = useAutenticacion()
  const puedeGenerar = puedeGenerarReportes(usuario?.rol)
  const { residentes, cargando: cargandoResidentes, error: errorResidentes } = useResidentesAlcance()

  const hoy = hoyBogota()
  const [residenteId, setResidenteId] = useState<number | ''>('')
  const [tipo, setTipo] = useState<TipoReporte>('HISTORIA_CLINICA')
  const [desde, setDesde] = useState(sumarDias(hoy, -DIAS_POR_DEFECTO))
  const [hasta, setHasta] = useState(hoy)
  const [pagina, setPagina] = useState(0)

  const historialId = typeof residenteId === 'number' ? residenteId : 0
  const {
    datos,
    cargando: cargandoHistorial,
    error: errorHistorial,
    recargar,
  } = useHistorialReportes(historialId, pagina, TAMANO_HISTORIAL)

  // Al descargar con exito se recarga el historial para reflejar el nuevo reporte.
  const { generando, error: errorGenerar, generar } = useGenerarReporte(recargar)
  // El historial solo se muestra si hay un residente elegido.
  const hayResidente = typeof residenteId === 'number'

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Documentos</p>
          <h2>Reportes clinicos</h2>
          <p className="muted">
            Genera el reporte en PDF de un residente para el periodo que elijas.
          </p>
        </div>
      </div>

      {errorResidentes && (
        <EstadoError mensaje={errorResidentes} />
      )}
      {!errorResidentes && cargandoResidentes && (
        <EstadoCarga mensaje="Cargando residentes..." />
      )}
      {!errorResidentes && !cargandoResidentes && residentes.length === 0 && (
        <EstadoVacio
          titulo="Sin residentes en tu alcance"
          descripcion="Cuando haya residentes en tu alcance podras generar sus reportes."
        />
      )}

      {!errorResidentes && !cargandoResidentes && residentes.length > 0 && puedeGenerar && (
        <section className="panel">
          <div className="panel-header">
            <div>
              <h3>Generar reporte</h3>
              <p className="muted">Selecciona el residente, el tipo y el periodo.</p>
            </div>
          </div>
          <div className="panel-body">
            <FormularioReporte
              residentes={residentes}
              cargandoResidentes={cargandoResidentes}
              residenteId={residenteId}
              tipo={tipo}
              desde={desde}
              hasta={hasta}
              generando={generando}
              error={errorGenerar}
              alCambiarResidente={(valor) => {
                setResidenteId(valor)
                setPagina(0)
              }}
              alCambiarTipo={setTipo}
              alCambiarDesde={setDesde}
              alCambiarHasta={setHasta}
              alGenerar={() => {
                if (typeof residenteId === 'number') {
                  void generar(residenteId, tipo, desde, hasta)
                }
              }}
            />
          </div>
        </section>
      )}

      {hayResidente && (
        <section className="panel">
          <div className="panel-header">
            <div>
              <h3>Historial de reportes</h3>
              <p className="muted">Reportes generados para este residente.</p>
            </div>
          </div>
          {cargandoHistorial && <EstadoCarga mensaje="Cargando historial..." />}
          {!cargandoHistorial && errorHistorial && (
            <EstadoError
              mensaje={errorHistorial}
              accion={
                <button type="button" className="btn btn-outline" onClick={recargar}>
                  Reintentar
                </button>
              }
            />
          )}
          {!cargandoHistorial && !errorHistorial && (datos?.contenido.length ?? 0) === 0 && (
            <EstadoVacio
              titulo="Aun no hay reportes"
              descripcion="Genera el primer reporte de este residente con el formulario de arriba."
            />
          )}
          {!cargandoHistorial && !errorHistorial && (datos?.contenido.length ?? 0) > 0 && (
            <TablaReportes reportes={datos?.contenido ?? []} />
          )}
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'reporte', plural: 'reportes' }}
              etiqueta="Paginacion del historial de reportes"
              alCambiarPagina={setPagina}
            />
          )}
        </section>
      )}
    </div>
  )
}
