// Cabecera del detalle de un residente: datos identificativos y estado.
// Solo presenta; no decide permisos ni cargar datos.

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

interface PropsDatosResidente {
  residente: PacienteDetalle
}

/** Bloque de datos basicos del residente. */
export function DatosResidente({ residente }: PropsDatosResidente) {
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
        <div>
          <dt>Situación clínica</dt>
          <dd>{residente.situacionClinica ?? 'Sin registrar'}</dd>
        </div>
      </dl>
    </section>
  )
}
