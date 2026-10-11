// Controles del listado: busqueda por texto y filtro de estado (solo ADMIN).
// Componentes sin estado; el estado vive en ListadoResidentes.

import { Search } from 'lucide-react'

interface PropsBuscadorResidentes {
  valor: string
  alCambiar: (texto: string) => void
}

/** Campo de busqueda por nombre, apellido o documento. */
export function BuscadorResidentes({ valor, alCambiar }: PropsBuscadorResidentes) {
  return (
    <label className="buscador-residentes">
      <Search size={18} aria-hidden="true" />
      <span className="sr-only">Buscar residente</span>
      <input
        type="search"
        value={valor}
        onChange={(evento) => alCambiar(evento.target.value)}
        placeholder="Buscar por nombre, apellido o documento"
        autoComplete="off"
        aria-label="Buscar residente por nombre, apellido o documento"
      />
    </label>
  )
}

interface PropsFiltroEstado {
  soloActivos: boolean
  alCambiar: (soloActivos: boolean) => void
}

/** Filtro activo/inactivo. Solo lo usa el ADMINISTRADOR. */
export function FiltroEstado({ soloActivos, alCambiar }: PropsFiltroEstado) {
  return (
    <fieldset className="filtro-estado">
      <legend className="sr-only">Filtrar por estado</legend>
      <button
        type="button"
        className={soloActivos ? 'selected' : ''}
        aria-pressed={soloActivos}
        onClick={() => alCambiar(true)}
      >
        Activos
      </button>
      <button
        type="button"
        className={!soloActivos ? 'selected' : ''}
        aria-pressed={!soloActivos}
        onClick={() => alCambiar(false)}
      >
        Todos
      </button>
    </fieldset>
  )
}
