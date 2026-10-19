// Hook del catalogo de especialidades (solo lectura, los tres roles). Se usa en
// el formulario de agendar/editar. Una cita puede no tener especialidad (visita
// general).

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarEspecialidades } from '@/features/citas/services/especialidadService'
import type { Especialidad } from '@/features/citas/types'

interface EstadoEspecialidades {
  especialidades: Especialidad[]
  cargando: boolean
  error: string | null
}

/** Catalogo de especialidades medicas. */
export function useEspecialidades(): EstadoEspecialidades {
  const [especialidades, setEspecialidades] = useState<Especialidad[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    listarEspecialidades()
      .then((lista) => {
        if (vigente) setEspecialidades(lista)
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
  }, [])

  return { especialidades, cargando, error }
}