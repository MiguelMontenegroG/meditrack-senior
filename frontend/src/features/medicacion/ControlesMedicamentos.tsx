// Controles del catalogo de medicamentos: busqueda por texto y filtro de
// estado. Componentes sin estado; el estado vive en ListadoMedicamentos.

import { Search } from 'lucide-react'

interface PropsBuscadorMedicamentos {
  valor: string
  alCambiar: (texto: string) => void
}

/** Campo de busqueda por nombre o presentacion del medicamento. */
export function BuscadorMedicamentos({ valor, alCambiar }: PropsBuscadorMedicamentos) {
  return (
    <label className="buscador-residentes">
      <Search size={18} aria-hidden="true" />
      <span className="sr-only">Buscar medicamento</span>
      <input
        type="search"
        value={valor}
        onChange={(evento) => alCambiar(evento.target.value)}
        placeholder="Buscar por nombre"
        autoComplete="off"
        aria-label="Buscar medicamento por nombre"
      />
    </label>
  )
}

interface PropsFiltroEstadoMedicamentos {
  filtro: 'activos' | 'inactivos' | 'todos'
  alCambiar: (filtro: 'activos' | 'inactivos' | 'todos') => void
}

/** Filtro de estado del catalogo: activos, inactivos o todos. */
export function FiltroEstadoMedicamentos({
  filtro,
  alCambiar,
}: PropsFiltroEstadoMedicamentos) {
  return (
    <fieldset className="filtro-estado">
      <legend className="sr-only">Filtrar por estado</legend>
      <button
        type="button"
        className={filtro === 'activos' ? 'selected' : ''}
        aria-pressed={filtro === 'activos'}
        onClick={() => alCambiar('activos')}
      >
        Activos
      </button>
      <button
        type="button"
        className={filtro === 'inactivos' ? 'selected' : ''}
        aria-pressed={filtro === 'inactivos'}
        onClick={() => alCambiar('inactivos')}
      >
        Inactivos
      </button>
      <button
        type="button"
        className={filtro === 'todos' ? 'selected' : ''}
        aria-pressed={filtro === 'todos'}
        onClick={() => alCambiar('todos')}
      >
        Todos
      </button>
    </fieldset>
  )
}
