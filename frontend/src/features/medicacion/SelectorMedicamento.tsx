// Selector reutilizable de medicamentos activos del catalogo. Se usa en el
// formulario de esquema. Solo ADMINISTRADOR puede crear esquemas, pero el
// listado de medicamentos tambien lo ve el CUIDADOR_ENFERMERO.

import { useEffect, useState } from 'react'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarMedicamentosActivos } from '@/features/medicacion/services/medicamentoService'
import type { Medicamento } from '@/features/medicacion/types'

interface PropsSelectorMedicamento {
  valor: string
  alCambiar: (valor: string) => void
  /** Deshabilita el control (p. ej. al editar un esquema ya creado). */
  deshabilitado?: boolean
}

/** Desplegable de medicamentos activos del catalogo. */
export function SelectorMedicamento({
  valor,
  alCambiar,
  deshabilitado = false,
}: PropsSelectorMedicamento) {
  const [medicamentos, setMedicamentos] = useState<Medicamento[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    listarMedicamentosActivos()
      .then((lista) => {
        if (vigente) setMedicamentos(lista)
      })
      .catch((fallo) => {
        if (vigente) setError(mensajeDeFallo(fallo))
      })
    return () => {
      vigente = false
    }
  }, [])

  if (error) {
    return (
      <p className="form-error" role="alert">
        {error}
      </p>
    )
  }
  if (!medicamentos) {
    return <EstadoCarga mensaje="Cargando medicamentos..." />
  }
  if (medicamentos.length === 0) {
    return <p className="muted">No hay medicamentos activos en el catalogo.</p>
  }

  return (
    <label>
      Medicamento
      <select
        value={valor}
        onChange={(e) => alCambiar(e.target.value)}
        disabled={deshabilitado}
      >
        <option value="">Selecciona un medicamento</option>
        {medicamentos.map((medicamento) => (
          <option key={medicamento.id} value={medicamento.id}>
            {medicamento.nombre}
            {medicamento.presentacion ? ` (${medicamento.presentacion})` : ''}
          </option>
        ))}
      </select>
    </label>
  )
}
