// Hook de carga del detalle de un residente: datos basicos, contactos y
// (solo ADMIN) familiares, cuidadores y el mapa de nombres de usuarios.
// Concentra la logica de estados para que la pantalla solo presente.

import { useCallback, useEffect, useState } from 'react'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { obtenerPaciente } from '@/features/pacientes/services/pacienteService'
import { listarFamiliares } from '@/features/pacientes/services/familiarService'
import { listarCuidadores } from '@/features/pacientes/services/cuidadorService'
import { listarUsuariosPorRol } from '@/features/pacientes/services/usuarioService'
import type {
  AsignacionCuidador,
  PacienteDetalle,
  VinculacionFamiliar,
} from '@/features/pacientes/types'

interface EstadoDetalle {
  paciente: PacienteDetalle | null
  familiares: VinculacionFamiliar[]
  cuidadores: AsignacionCuidador[]
  nombresPorUsuario: Record<number, string>
  cargando: boolean
  noEncontrado: boolean
  error: string | null
  recargar: () => Promise<void>
}

/** Carga el detalle del residente segun el alcance del usuario. */
export function useDetalleResidente(pacienteId: number, puedeGestionar: boolean): EstadoDetalle {
  const [paciente, setPaciente] = useState<PacienteDetalle | null>(null)
  const [familiares, setFamiliares] = useState<VinculacionFamiliar[]>([])
  const [cuidadores, setCuidadores] = useState<AsignacionCuidador[]>([])
  const [nombresPorUsuario, setNombres] = useState<Record<number, string>>({})
  const [cargando, setCargando] = useState(true)
  const [noEncontrado, setNoEncontrado] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const recargar = useCallback(async () => {
    setError(null)
    try {
      const detalle = await obtenerPaciente(pacienteId)
      setPaciente(detalle)
      if (puedeGestionar) {
        const [fam, cui] = await Promise.all([
          listarFamiliares(pacienteId),
          listarCuidadores(pacienteId),
        ])
        setFamiliares(fam)
        setCuidadores(cui)
        const [famUsuarios, cuiUsuarios] = await Promise.all([
          listarUsuariosPorRol('FAMILIAR_AUTORIZADO', true),
          listarUsuariosPorRol('CUIDADOR_ENFERMERO', true),
        ])
        const mapa: Record<number, string> = {}
        for (const u of [...famUsuarios, ...cuiUsuarios]) mapa[u.id] = u.nombreCompleto
        setNombres(mapa)
      }
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.status === 404) {
        setNoEncontrado(true)
      } else {
        setError(mensajeDeFallo(fallo))
      }
    } finally {
      setCargando(false)
    }
  }, [pacienteId, puedeGestionar])

  useEffect(() => {
    setCargando(true)
    setNoEncontrado(false)
    void recargar()
  }, [recargar])

  return { paciente, familiares, cuidadores, nombresPorUsuario, cargando, noEncontrado, error, recargar }
}
