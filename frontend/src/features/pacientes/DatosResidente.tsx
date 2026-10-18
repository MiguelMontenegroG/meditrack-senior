// Datos del residente dentro de su detalle: identificativos, ubicacion y
// perfil clinico. Solo presenta; los permisos y los datos llegan resueltos.

import { CircleAlert } from 'lucide-react'
import { EtiquetaEstado } from '@/components/ui/EtiquetaEstado'
import type { PacienteDetalle } from '@/features/pacientes/types'

/** Etiqueta legible del sexo del residente. */
const ETIQUETA_SEXO: Record<PacienteDetalle['sexo'], string> = {
  MASCULINO: 'Masculino',
  FEMENINO: 'Femenino',
  OTRO: 'Otro',
  NO_ESPECIFICA: 'No especifica',
}

/** Formatea una fecha ISO (yyyy-mm-dd) como dd/mm/yyyy sin zona horaria. */
function formatearFecha(iso: string | null): string {
  if (!iso) return 'Sin registrar'
  const [anio, mes, dia] = iso.split('-')
  return dia && mes && anio ? `${dia}/${mes}/${anio}` : iso
}

/** Campos clinicos que faltan para completar el perfil del residente. */
function datosClinicosPendientes(residente: PacienteDetalle): string[] {
  const faltantes: string[] = []
  // La habitacion es opcional: el producto incluye centros de dia sin habitaciones.
  if (!residente.situacionClinica?.trim()) faltantes.push('la situación clínica')
  return faltantes
}

interface PropsDatosResidente {
  residente: PacienteDetalle
}

/** Bloque de datos basicos y clinicos del residente. */
export function DatosResidente({ residente }: PropsDatosResidente) {
  const pendientes = residente.activo ? datosClinicosPendientes(residente) : []
  return (
    <section className="panel" aria-label="Datos del residente">
      <div className="panel-header">
        <div>
          <h3>Datos del residente</h3>
          <p className="muted">Documento {residente.documento}</p>
        </div>
        <EtiquetaEstado tono={residente.activo ? 'success' : 'neutral'}>
          {residente.activo ? 'Activo' : 'Inactivo'}
        </EtiquetaEstado>
      </div>
      <dl className="datos-residente">
        <div>
          <dt>Edad</dt>
          <dd>{residente.edad} años</dd>
        </div>
        <div>
          <dt>Fecha de nacimiento</dt>
          <dd>{formatearFecha(residente.fechaNacimiento)}</dd>
        </div>
        <div>
          <dt>Sexo</dt>
          <dd>{ETIQUETA_SEXO[residente.sexo]}</dd>
        </div>
        <div>
          <dt>Habitación</dt>
          <dd>{residente.habitacion ?? 'Sin asignar'}</dd>
        </div>
        <div>
          <dt>Fecha de ingreso</dt>
          <dd>{formatearFecha(residente.fechaIngreso)}</dd>
        </div>
      </dl>
      <div className="bloque-clinico">
        <h4>Situación clínica</h4>
        <p>{residente.situacionClinica?.trim() || 'Sin registrar'}</p>
      </div>
      {pendientes.length > 0 && (
        <p className="form-error" role="alert">
          <CircleAlert size={16} /> Perfil clínico incompleto: falta {pendientes.join(' y ')}.
          Complétalo desde "Editar" para que el residente tenga toda la información clínica.
        </p>
      )}
    </section>
  )
}
