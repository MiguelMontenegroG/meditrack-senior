// Pantalla de detalle de un residente. Todos los roles autenticados pueden
// verla segun su alcance. Un 404 (inexistente o fuera de alcance) muestra un
// estado "Residente no encontrado" sin sugerir que existe.

import { useNavigate, useParams } from 'react-router-dom'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeEscribir } from '@/features/pacientes/permisos'
import { useDetalleResidente } from '@/features/pacientes/useDetalleResidente'
import { CabeceraDetalle } from '@/features/pacientes/CabeceraDetalle'
import { DatosResidente } from '@/features/pacientes/DatosResidente'
import { SeccionContactos } from '@/features/pacientes/SeccionContactos'
import { SeccionFamiliares } from '@/features/pacientes/SeccionFamiliares'
import { SeccionCuidadores } from '@/features/pacientes/SeccionCuidadores'

/** Detalle del residente con contactos y gestion (ADMIN) de vinculos. */
export function DetalleResidente() {
  const { id } = useParams()
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const puedeGestionar = puedeEscribir(usuario?.rol)

  const {
    paciente,
    familiares,
    cuidadores,
    nombresPorUsuario,
    cargando,
    noEncontrado,
    error,
    recargar,
  } = useDetalleResidente(Number(id), puedeGestionar)

  if (cargando) return <EstadoCarga mensaje="Cargando residente..." />

  if (noEncontrado) {
    return (
      <div className="page-content">
        <EstadoError mensaje="Residente no encontrado." />
        <div className="acciones-detalle">
          <BotonAccion variant="outline" onClick={() => navegar('/residentes')}>
            Volver al listado
          </BotonAccion>
        </div>
      </div>
    )
  }

  if (error || !paciente) {
    return (
      <div className="page-content">
        <EstadoError
          mensaje={error ?? 'No pudimos cargar el residente.'}
          accion={
            <BotonAccion variant="outline" onClick={() => void recargar()}>
              Reintentar
            </BotonAccion>
          }
        />
      </div>
    )
  }

  return (
    <div className="page-content">
      <CabeceraDetalle
        residente={paciente}
        puedeGestionar={puedeGestionar}
        alVolver={() => navegar('/residentes')}
        alEditar={() => navegar(`/residentes/${paciente.id}/editar`)}
        alCambiarEstado={recargar}
      />

      <DatosResidente residente={paciente} />

      <SeccionContactos
            pacienteId={paciente.id}
        contactos={paciente.contactos}
        puedeGestionar={puedeGestionar}
            alCambiar={recargar}
          />

      {puedeGestionar && (
        <div className="grid-detalle">
          <SeccionFamiliares
            pacienteId={paciente.id}
            familiares={familiares}
            nombresPorUsuario={nombresPorUsuario}
            alCambiar={recargar}
          />
          <SeccionCuidadores
            pacienteId={paciente.id}
            cuidadores={cuidadores}
            nombresPorUsuario={nombresPorUsuario}
            alCambiar={recargar}
          />
    </div>
      )}
    </div>
  )
}
