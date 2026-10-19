// Hook que carga los residentes ACTIVOS dentro del alcance del usuario (el
// backend resuelve el alcance por rol en GET /pacientes). Se usa en el filtro
// del calendario y en el formulario de agendar/editar. Solo la primera pagina
// (hasta 100) alcanza para los desplegables del centro.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarPacientes } from '@/features/pacientes/services/pacienteService'
import type { PacienteResumen } from '@/features/pacientes/types'

const TAMANO = 100

interface EstadoResidentes {
  residentes: PacienteResumen[]
  cargando: boolean
  error: string | null
}

/** Residentes activos del alcance del usuario autenticado. */
export function useResidentesAlcance(): EstadoResidentes {
  const [residentes, setResidentes] = useState<PacienteResumen[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    listarPacientes({ activo: true, pagina: 0, tamano: TAMANO })
      .then((pagina) => {
        if (vigente) setResidentes(pagina.contenido)
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

  return { residentes, cargando, error }
}