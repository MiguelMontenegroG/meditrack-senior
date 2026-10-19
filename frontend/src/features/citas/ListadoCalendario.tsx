// Listado de citas agrupado por dia (en espanol). En escritorio usa tabla; en
// movil, tarjetas. La agrupacion se calcula en hora de Bogota.

import { TablaCitas } from '@/features/citas/TablaCitas'
import { TarjetaCita } from '@/features/citas/TarjetaCita'
import { agruparPorDia, diaEnEspanol } from '@/features/citas/fechas'
import type { CitaDetalle } from '@/features/citas/types'
import type { RolUsuario } from '@/features/auth/types'

interface PropsListadoCalendario {
  citas: CitaDetalle[]
  nombrePorResidente: (pacienteId: number) => string
  rol: RolUsuario | undefined
  alEditar: (cita: CitaDetalle) => void
  alCambiarEstado: () => void
}

/** Citas agrupadas por dia, con tabla en escritorio y tarjetas en movil. */
export function ListadoCalendario({
  citas,
  nombrePorResidente,
  rol,
  alEditar,
  alCambiarEstado,
}: PropsListadoCalendario) {
  const grupos = agruparPorDia(citas)
  return (
    <>
      {grupos.map((grupo) => (
        <section key={grupo.fecha} className="grupo-dia" aria-label={diaEnEspanol(grupo.fecha)}>
          <h3 className="grupo-dia-titulo">{diaEnEspanol(grupo.fecha)}</h3>
          <div className="listado-escritorio">
            <TablaCitas
              citas={grupo.citas}
              nombrePorResidente={nombrePorResidente}
              rol={rol}
              alEditar={alEditar}
              alCambiarEstado={alCambiarEstado}
            />
          </div>
          <div className="listado-movil">
            {grupo.citas.map((cita) => (
              <TarjetaCita
                key={cita.id}
                cita={cita}
                nombreResidente={nombrePorResidente(cita.pacienteId)}
                rol={rol}
                alEditar={alEditar}
                alCambiarEstado={alCambiarEstado}
              />
            ))}
          </div>
        </section>
      ))}
    </>
  )
}