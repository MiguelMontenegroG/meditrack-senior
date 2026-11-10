// Hook de generacion/descarga de un PDF de reporte. Valida el rango en cliente
// (evita una llamada inutil), llama al servicio, dispara la descarga del Blob y
// traduce los errores con el contexto adecuado (rango, residente fuera de
// alcance, conflictos). Al exito avisa para recargar el historial.

import { useCallback, useState } from 'react'
import { descargarBlob } from '@/lib/descargar-archivo'
import { mensajeDeErrorContexto, mensajeDeFallo, type ContextoError } from '@/lib/mensajes-error'
import { validarRangoReporte } from '@/features/reportes/fechas'
import { descargarReportePdf } from '@/features/reportes/services/reporteService'
import type { TipoReporte } from '@/features/reportes/types'

interface EstadoGenerarReporte {
  generando: boolean
  error: string | null
  /** Genera y descarga el PDF; al exito invoca `alExito`. */
  generar: (pacienteId: number, tipo: TipoReporte, desde: string, hasta: string) => Promise<void>
  limpiarError: () => void
}

/** Nombre de archivo de respaldo SIN datos personales si el backend no lo envía. */
function nombreRespaldo(tipo: TipoReporte): string {
  return `reporte-${tipo.toLowerCase()}.pdf`
}

/** Contexto de mensaje segun el status del fallo. */
function contextoPorStatus(status: number | undefined): ContextoError {
  if (status === 404) return 'reporte-no-encontrado'
  if (status === 409) return 'reporte-rango-excedido'
  return 'reporte-rango-invalido'
}

/** Devuelve el handler y el estado para generar reportes. */
export function useGenerarReporte(alExito: () => void): EstadoGenerarReporte {
  const [generando, setGenerando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const generar = useCallback(
    async (pacienteId: number, tipo: TipoReporte, desde: string, hasta: string) => {
      const validacion = validarRangoReporte(desde, hasta)
      if (!validacion.valido || !validacion.rangoUtc) {
        setError(validacion.mensaje)
        return
      }
      setGenerando(true)
      setError(null)
      try {
        const { blob, nombreArchivo } = await descargarReportePdf(
          pacienteId,
          tipo,
          validacion.rangoUtc,
        )
        descargarBlob(blob, nombreArchivo ?? nombreRespaldo(tipo))
        alExito()
      } catch (fallo) {
        const status =
          fallo && typeof fallo === 'object' && 'status' in fallo
            ? (fallo as { status: number }).status
            : undefined
        if (status === 404 || status === 409 || status === 400) {
          const codigo = status === 409 ? 'CONFLICTO' : status === 404 ? 'ERROR_INESPERADO' : 'VALIDACION'
          setError(mensajeDeErrorContexto(codigo, contextoPorStatus(status)))
        } else {
          setError(mensajeDeFallo(fallo))
        }
      } finally {
        setGenerando(false)
      }
    },
    [alExito],
  )

  const limpiarError = useCallback(() => setError(null), [])

  return { generando, error, generar, limpiarError }
}
