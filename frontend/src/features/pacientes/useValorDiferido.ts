// Hook reutilizable de "valor con retardo" (debounce). Se usa en la busqueda
// del listado para no disparar una peticion por cada tecla; el retardo
// (~300 ms) mantiene la interfaz receptiva sin saturar el backend.

import { useEffect, useState } from 'react'

/** Devuelve el valor tras esperar `retardoMs` sin cambios. */
export function useValorDiferido<T>(valor: T, retardoMs = 300): T {
  const [diferido, setDiferido] = useState(valor)

  useEffect(() => {
    const temporizador = setTimeout(() => setDiferido(valor), retardoMs)
    return () => clearTimeout(temporizador)
  }, [valor, retardoMs])

  return diferido
}
