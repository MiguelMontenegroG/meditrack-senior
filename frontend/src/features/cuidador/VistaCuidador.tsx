import { Check, ChevronRight, Droplets, HeartPulse, Pill, Plus, Stethoscope, Thermometer } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import type { SignoVital } from '@/features/cuidador/types'
import { ETIQUETAS_SIGNO, signosVitales, tareasJornada } from '@/features/cuidador/mock-data'

// Vista del cuidador enfermero: resumen de la jornada, tareas pendientes y
// ultima bitacora registrada. Sin conexion a la API (Paso 2).

const ICONOS_SIGNO: Record<SignoVital['clave'], LucideIcon> = {
  presion: HeartPulse,
  glucosa: Droplets,
  temperatura: Thermometer,
}

/** Pantalla principal del rol CUIDADOR_ENFERMERO. */
export function VistaCuidador() {
  return (
    <div className="page-content caregiver-page">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Sabado, 27 de septiembre</p>
          <h2>Tu jornada de hoy</h2>
          <p className="muted">Hola, Andres. Tienes 4 tareas pendientes.</p>
        </div>
        <BotonAccion>
          <Plus size={18} /> Registrar bitacora
        </BotonAccion>
      </div>

      <ResumenJornada />
      <ListaTareas />
      <UltimaBitacora />
    </div>
  )
}

/** Resumen compacto del avance de la jornada. */
function ResumenJornada() {
  return (
    <div className="care-summary">
      <div>
        <span className="progress-ring">75%</span>
        <span>
          <strong>3 de 4 tareas</strong>
          <small>completadas</small>
        </span>
      </div>
      <div className="summary-divider" />
      <div>
        <strong>38 / 42</strong>
        <small>bitacoras registradas</small>
      </div>
      <div>
        <strong>2</strong>
        <small>alertas activas</small>
      </div>
    </div>
  )
}

/** Lista de tareas pendientes ordenadas por hora. */
function ListaTareas() {
  return (
    <section className="panel task-panel">
      <div className="panel-header">
        <div>
          <h3>Tareas pendientes</h3>
          <p className="muted">Ordenadas por hora</p>
        </div>
        <button className="text-link">
          Ver completadas <ChevronRight size={15} />
        </button>
      </div>
      <div className="task-list">
        {tareasJornada.map((tarea) => {
          const Icono = tarea.tipo === 'Medicamento' ? Pill : tarea.tipo === 'Cita' ? Stethoscope : HeartPulse
          return (
            <div className={`task-row ${tarea.urgente ? 'task-urgent' : ''}`} key={tarea.titulo}>
              <time>{tarea.hora}</time>
              <span className={`task-icon ${tarea.tono}`}>
                <Icono size={18} />
              </span>
              <div className="task-copy">
                <strong>{tarea.titulo}</strong>
                <span>{tarea.residente}</span>
                <small>{tarea.tipo}</small>
              </div>
              <button className="task-check" aria-label={`Completar ${tarea.titulo}`}>
                <Check size={18} />
              </button>
            </div>
          )
        })}
      </div>
    </section>
  )
}

/** Resumen de la ultima bitacora de signos vitales. */
function UltimaBitacora() {
  return (
    <section className="panel vitals-quick">
      <div className="panel-header">
        <div>
          <h3>Ultima bitacora registrada</h3>
          <p className="muted">Rosa Elena Gutierrez · Hoy, 08:10 a. m.</p>
        </div>
        <EtiquetaEstado tono="success">Dentro de rango</EtiquetaEstado>
      </div>
      <div className="vitals-grid">
        {signosVitales.map((signo) => {
          const Icono = ICONOS_SIGNO[signo.clave]
          return (
            <div key={signo.clave}>
              <Icono size={18} />
              <strong>{signo.valor}</strong>
              <span>
                {ETIQUETAS_SIGNO[signo.clave]} <small>{signo.unidad}</small>
              </span>
            </div>
          )
        })}
      </div>
    </section>
  )
}
