// Selector reutilizable de usuarios activos de un rol concreto. Se usa para
// vincular familiares (FAMILIAR_AUTORIZADO) y asignar cuidadores
// (CUIDADOR_ENFERMERO). Solo ADMINISTRADOR puede listar usuarios.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import type { RolUsuario } from '@/features/pacientes/types'
import {
  listarUsuariosPorRol,
} from '@/features/usuarios/services/usuarioService'
import type { Usuario } from '@/features/usuarios/types'

interface PropsSelectorUsuarios {
  rol: RolUsuario
  /** Ids ya vinculados/asignados, para excluirlos del selector. */
  excluirIds: number[]
  valor: string
  alCambiar: (valor: string) => void
}

/** Desplegable de usuarios activos de un rol, excluyendo los ya vinculados. */
export function SelectorUsuarios({ rol, excluirIds, valor, alCambiar }: PropsSelectorUsuarios) {
  const [usuarios, setUsuarios] = useState<Usuario[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    listarUsuariosPorRol(rol)
      .then((lista) => {
        if (vigente) setUsuarios(lista)
      })
      .catch((fallo) => {
        if (vigente) setError(mensajeDeFallo(fallo))
      })
    return () => {
      vigente = false
    }
  }, [rol])

  if (error) {
    return (
      <p className="form-error" role="alert">
        {error}
      </p>
    )
  }
  if (!usuarios) {
    return <EstadoCarga mensaje="Cargando usuarios..." />
  }

  const disponibles = usuarios.filter((u) => !excluirIds.includes(u.id))
  if (disponibles.length === 0) {
    return <p className="muted">No hay usuarios disponibles para vincular.</p>
  }

  return (
    <label>
      Usuario
      <select value={valor} onChange={(e) => alCambiar(e.target.value)}>
        <option value="">Selecciona un usuario</option>
        {disponibles.map((u) => (
          <option key={u.id} value={u.id}>
            {u.nombreCompleto}
          </option>
        ))}
      </select>
    </label>
  )
}
