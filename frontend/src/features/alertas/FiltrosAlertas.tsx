// Controles de filtro del listado de alertas: estado y tipo. Componentes sin
// estado; los valores viven en la pantalla.

import {
  ESTADOS_ALERTA_FILTRABLES,
  TIPOS_ALERTA_FILTRABLES,
} from '@/features/alertas/etiquetas'
import type { EstadoAlerta, TipoAlerta } from '@/features/alertas/types'

interface PropsFiltrosAlertas {
  estado: EstadoAlerta | ''
  tipo: TipoAlerta | ''
  alCambiarEstado: (valor: EstadoAlerta | '') => void
  alCambiarTipo: (valor: TipoAlerta | '') => void
}

/** Filtros de estado y tipo de alerta (vacio = todos). */
export function FiltrosAlertas({
  estado,
  tipo,
  alCambiarEstado,
  alCambiarTipo,
}: PropsFiltrosAlertas) {
  return (
    <div className="filtros-listado">
      <label>
        Estado
        <select
          value={estado}
          onChange={(evento) => alCambiarEstado(evento.target.value as EstadoAlerta | '')}
        >
          <option value="">Todos</option>
          {ESTADOS_ALERTA_FILTRABLES.map((opcion) => (
            <option key={opcion.valor} value={opcion.valor}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </label>
      <label>
        Tipo
        <select
          value={tipo}
          onChange={(evento) => alCambiarTipo(evento.target.value as TipoAlerta | '')}
        >
          <option value="">Todos</option>
          {TIPOS_ALERTA_FILTRABLES.map((opcion) => (
            <option key={opcion.valor} value={opcion.valor}>
              {opcion.etiqueta}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
