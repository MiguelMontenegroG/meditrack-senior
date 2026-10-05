import type { SignoVital, TareaJornada } from '@/features/cuidador/types'

// Datos simulados de la jornada del cuidador (Paso 2). Sin API todavia.

export const tareasJornada: TareaJornada[] = [
  { hora: '08:00', titulo: 'Administrar Losartan 50 mg', residente: 'Rosa Elena Gutierrez · Hab. 204', tipo: 'Medicamento', tono: 'danger', urgente: true },
  { hora: '10:00', titulo: 'Registrar bitacora diaria', residente: 'Luis Alberto Cardenas · Hab. 112', tipo: 'Signos vitales', tono: 'warning', urgente: false },
  { hora: '11:30', titulo: 'Cita — Control de cardiologia', residente: 'Beatriz Amparo Rios · Hab. 308', tipo: 'Cita', tono: 'info', urgente: false },
  { hora: '14:00', titulo: 'Administrar Metformina 850 mg', residente: 'Rosa Elena Gutierrez · Hab. 204', tipo: 'Medicamento', tono: 'neutral', urgente: false },
]

export const signosVitales: SignoVital[] = [
  { clave: 'presion', valor: '128/78', unidad: 'mmHg' },
  { clave: 'glucosa', valor: '102', unidad: 'mg/dL' },
  { clave: 'temperatura', valor: '36,5°', unidad: '°C' },
]

export const ETIQUETAS_SIGNO: Record<SignoVital['clave'], string> = {
  presion: 'Presion arterial',
  glucosa: 'Glucosa',
  temperatura: 'Temperatura',
}
