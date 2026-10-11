// Hook de soporte para formularios de alta/edicion. Si recibe un id, carga
// los datos existentes y los vuelca sobre el formulario; si es null, trabaja
// con los valores iniciales (alta).

import { useEffect, useState } from 'react'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import type { DatosPaciente } from '@/features/pacientes/types'

/** Convierte el detalle del backend en los campos del formulario. */
function aDatosFormulario(detalle: {
  documento: string
  nombres: string
  apellidos: string
  fechaNacimiento: string
  sexo: DatosPaciente['sexo']
  habitacion: string | null
  situacionClinica: string | null
  fechaIngreso: string | null
}): DatosPaciente {
  return {
    documento: detalle.documento,
    nombres: detalle.nombres,
    apellidos: detalle.apellidos,
    fechaNacimiento: detalle.fechaNacimiento,
    sexo: detalle.sexo,
    habitacion: detalle.habitacion ?? '',
    situacionClinica: detalle.situacionClinica ?? '',
    fechaIngreso: detalle.fechaIngreso ?? '',
  }
}

/** Gestiona los datos del formulario de residente en modo alta o edicion. */
export function useCargaEdicion(
  id: number | null,
  inicial: DatosPaciente,
  cargarPorId: (id: number) => Promise<Parameters<typeof aDatosFormulario>[0]>,
) {
  const [datos, setDatos] = useState<DatosPaciente>(inicial)
  const [cargando, setCargando] = useState(id !== null)
  const [errorCarga, setErrorCarga] = useState<string | null>(null)

  useEffect(() => {
    if (id === null) return
    let vigente = true
    setCargando(true)
    cargarPorId(id)
      .then((detalle) => {
        if (vigente) setDatos(aDatosFormulario(detalle))
      })
      .catch((fallo) => {
        if (vigente) setErrorCarga(mensajeDeFallo(fallo))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [id, cargarPorId])

  return { datos, setDatos, cargando, errorCarga }
}
