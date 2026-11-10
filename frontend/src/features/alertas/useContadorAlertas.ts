// Hook del contador de alertas ACTIVAS. Hace UNA sola llamada a GET /api/alertas
// con tamano=1 y estado=ACTIVA, y usa el campo `total` de la pagina (que cuenta
// todas las filas del servidor, no solo la devuelta). Reutilizable por la campana
// del encabezado y por el indicador del panel de administrador.
//
// Si la llamada falla, no rompe la interfaz: devuelve total 0 y un error que el
// componente puede ignorar (la campana no muestra nada) o presentar aparte.

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { listarAlertas } from '@/features/alertas/services/alertaService'

interface EstadoContadorAlertas {
  /** Numero de alertas ACTIVAS del alcance (0 mientras carga o si falla). */
  total: number
  cargando: boolean
  /** true si la consulta fallo; la interfaz debe seguir funcionando igual. */
  error: boolean
  /** Mensaje en espanol del fallo (o null). */
  mensajeError: string | null
}

/**
 * Cuenta las alertas activas del alcance con una unica llamada.
 * @param habilitado permite no consultar si el rol no puede ver alertas.
 */
export function useContadorAlertas(habilitado = true): EstadoContadorAlertas {
  const [total, setTotal] = useState(0)
  const [cargando, setCargando] = useState(habilitado)
  const [mensajeError, setMensajeError] = useState<string | null>(null)

  useEffect(() => {
    if (!habilitado) {
      setCargando(false)
      return
    }
    let vigente = true
    setCargando(true)
    setMensajeError(null)
    listarAlertas({ estado: 'ACTIVA', pagina: 0, tamano: 1 })
      .then((pagina) => {
        if (vigente) setTotal(pagina.total)
      })
      .catch((fallo) => {
        if (vigente) {
          setTotal(0)
          setMensajeError(mensajeDeFallo(fallo))
        }
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [habilitado])

  return { total, cargando, error: mensajeError !== null, mensajeError }
}
