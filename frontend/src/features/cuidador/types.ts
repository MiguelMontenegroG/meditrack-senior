import type { TonoEstado } from '@/components/ui/EtiquetaEstado'

// Types de la vista del cuidador. Los datos de ejemplo viven en mock-data.ts.

/** Tarea de la jornada del cuidador. */
export interface TareaJornada {
  hora: string
  titulo: string
  residente: string
  tipo: string
  tono: TonoEstado
  urgente: boolean
}

/** Signo vital mostrado en el resumen rapido. */
export interface SignoVital {
  clave: 'presion' | 'glucosa' | 'temperatura'
  valor: string
  unidad: string
}
