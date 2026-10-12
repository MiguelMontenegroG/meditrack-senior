// Controles del listado de usuarios: filtros por rol y por estado.
// Componentes sin estado; el estado vive en ListadoUsuarios.

import { OPCIONES_ROL } from '@/features/usuarios/permisos'
import type { RolUsuario } from '@/features/usuarios/types'

interface PropsFiltroRol {
  rol: RolUsuario | ''
  alCambiar: (rol: RolUsuario | '') => void
}

/** Desplegable para filtrar por rol (todos, o uno concreto). */
export function FiltroRol({ rol, alCambiar }: PropsFiltroRol) {
  return (
    <label className="filtro-rol">
      Rol
      <select
        value={rol}
        onChange={(e) => alCambiar(e.target.value as RolUsuario | '')}
      >
        <option value="">Todos los roles</option>
        {OPCIONES_ROL.map((opcion) => (
          <option key={opcion.valor} value={opcion.valor}>
            {opcion.etiqueta}
          </option>
        ))}
      </select>
    </label>
  )
}

interface PropsFiltroEstadoUsuarios {
  filtro: 'activos' | 'inactivos' | 'todos'
  alCambiar: (filtro: 'activos' | 'inactivos' | 'todos') => void
}

/** Filtro de estado: activos, inactivos o todos. */
export function FiltroEstadoUsuarios({ filtro, alCambiar }: PropsFiltroEstadoUsuarios) {
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
