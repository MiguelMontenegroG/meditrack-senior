// Pantalla /alertas: listado paginado de alertas para los tres roles. Filtros
// de estado y tipo, tarjetas en movil y tabla en escritorio, estados de carga,
// error y vacio con componentes de ui. El boton "Atender" solo aparece para
// ADMINISTRADOR y CUIDADOR_ENFERMERO y exige confirmacion (no doble clic).

import { useMemo, useState } from 'react'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { Paginacion } from '@/components/ui/Paginacion'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { ConfirmacionAtender } from '@/features/alertas/ConfirmacionAtender'
import { FiltrosAlertas } from '@/features/alertas/FiltrosAlertas'
import { TablaAlertas } from '@/features/alertas/TablaAlertas'
import { TarjetaAlerta } from '@/features/alertas/TarjetaAlerta'
import { puedeAtenderAlertas } from '@/features/alertas/permisos'
import { useAtenderAlerta } from '@/features/alertas/useAtenderAlerta'
import { useListadoAlertas } from '@/features/alertas/useListadoAlertas'
import type { EstadoAlerta, TipoAlerta } from '@/features/alertas/types'

const TAMANO_PAGINA = 10

/** Pantalla de alertas. */
export function ListadoAlertas() {
  const { usuario } = useAutenticacion()
  const puedeAtender = puedeAtenderAlertas(usuario?.rol)

  const [estado, setEstado] = useState<EstadoAlerta | ''>('')
  const [tipo, setTipo] = useState<TipoAlerta | ''>('')
  const [pagina, setPagina] = useState(0)
  const [porConfirmar, setPorConfirmar] = useState<number | null>(null)

  const { datos, cargando, error, recargar } = useListadoAlertas({
    estado: estado || undefined,
    tipo: tipo || undefined,
    pagina,
    tamano: TAMANO_PAGINA,
  })
  const { atendiendoId, error: errorAtender, atender, limpiarError } = useAtenderAlerta(recargar)

  const alertas = useMemo(() => datos?.contenido ?? [], [datos])

  const cambiarEstado = (valor: EstadoAlerta | '') => {
    setEstado(valor)
    setPagina(0)
  }
  const cambiarTipo = (valor: TipoAlerta | '') => {
    setTipo(valor)
    setPagina(0)
  }

  const confirmar = async () => {
    if (porConfirmar === null) return
    const id = porConfirmar
    setPorConfirmar(null)
    await atender(id)
  }

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Seguimiento</p>
          <h2>Alertas</h2>
          <p className="muted">
            Alertas automaticas de tomas atrasadas, signos fuera de rango y citas vencidas de tu alcance.
          </p>
        </div>
      </div>

      <FiltrosAlertas
        estado={estado}
        tipo={tipo}
        alCambiarEstado={cambiarEstado}
        alCambiarTipo={cambiarTipo}
      />

      {errorAtender && (
        <div className="form-error" role="alert">
          {errorAtender}{' '}
          <button type="button" className="outline-link" onClick={limpiarError}>
            Ocultar
          </button>
        </div>
      )}

      {porConfirmar !== null && (
        <ConfirmacionAtender
          enviando={atendiendoId === porConfirmar}
          alConfirmar={() => void confirmar()}
          alCancelar={() => setPorConfirmar(null)}
        />
      )}

      {cargando && <EstadoCarga mensaje="Cargando alertas..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <button type="button" className="btn btn-outline" onClick={recargar}>
              Reintentar
            </button>
          }
        />
      )}
      {!cargando && !error && alertas.length === 0 && (
        <EstadoVacio
          titulo="Sin alertas"
          descripcion="No hay alertas que coincidan con los filtros seleccionados."
        />
      )}
      {!cargando && !error && alertas.length > 0 && (
        <>
          <div className="listado-escritorio">
            <TablaAlertas
              alertas={alertas}
              puedeAtender={puedeAtender}
              alAtender={(id) => setPorConfirmar(id)}
              atendiendoId={atendiendoId}
            />
          </div>
          <div className="listado-movil">
            <ul className="lista-tarjetas-alertas">
              {alertas.map((alerta) => (
                <li key={alerta.id}>
                  <TarjetaAlerta
                    alerta={alerta}
                    puedeAtender={puedeAtender}
                    alAtender={(id) => setPorConfirmar(id)}
                    atendiendo={atendiendoId === alerta.id}
                  />
                </li>
              ))}
            </ul>
          </div>
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'alerta', plural: 'alertas' }}
              etiqueta="Paginacion de alertas"
              alCambiarPagina={setPagina}
            />
          )}
        </>
      )}
    </div>
  )
}
