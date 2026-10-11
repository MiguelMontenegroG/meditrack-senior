import type { SignoVital, TareaJornada } from '@/features/cuidador/types'

// DATOS DE EJEMPLO (pendientes de su endpoint).
// Las tareas y signos vitales mostrados son ilustrativos para maquetar la vista
// del cuidador; NO provienen del backend. Cuando existan sus endpoints reales,
// este archivo se reemplaza por una llamada a la capa de servicios.

export const tareasJornada: TareaJornada[] = [
  { hora: '08:00', titulo: 'Administrar Losartan 50 mg', residente: 'Rosa Elena Gutiérrez · Hab. 204', tipo: 'Medicamento', tono: 'danger', urgente: true },
  { hora: '10:00', titulo: 'Registrar bitácora diaria', residente: 'Luis Alberto Cárdenas · Hab. 112', tipo: 'Signos vitales', tono: 'warning', urgente: false },
  { hora: '11:30', titulo: 'Cita — Control de cardiología', residente: 'Beatriz Amparo Ríos · Hab. 308', tipo: 'Cita', tono: 'info', urgente: false },
  { hora: '14:00', titulo: 'Administrar Metformina 850 mg', residente: 'Rosa Elena Gutiérrez · Hab. 204', tipo: 'Medicamento', tono: 'neutral', urgente: false },
]

export const signosVitales: SignoVital[] = [
  { clave: 'presion', valor: '128/78', unidad: 'mmHg' },
  { clave: 'glucosa', valor: '102', unidad: 'mg/dL' },
  { clave: 'temperatura', valor: '36,5°', unidad: '°C' },
]

export const ETIQUETAS_SIGNO: Record<SignoVital['clave'], string> = {
  presion: 'Presión arterial',
  glucosa: 'Glucosa',
  temperatura: 'Temperatura',
}
