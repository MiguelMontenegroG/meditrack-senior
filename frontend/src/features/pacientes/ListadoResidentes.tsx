// Pantalla de listado de residentes. Todos los roles autenticados la ven;
// el backend ya filtra por alcance. Aqui solo se reflejan permisos y estados.

import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { useValorDiferido } from '@/features/pacientes/useValorDiferido'
import { puedeEscribir } from '@/features/pacientes/permisos'
import { listarPacientes } from '@/features/pacientes/services/pacienteService'
import type { PaginaPacientes } from '@/features/pacientes/types'
import { BuscadorResidentes, FiltroEstado } from '@/features/pacientes/ControlesListado'
import { Paginacion } from '@/features/pacientes/Paginacion'
import { TablaListado } from '@/features/pacientes/TablaListado'
import { TarjetasListado } from '@/features/pacientes/TarjetasListado'

const TAMANO_PAGINA = 10

/** Listado de residentes con busqueda, filtro (ADMIN) y paginacion. */
export function ListadoResidentes() {
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const puedeGestionar = puedeEscribir(usuario?.rol)

  const [texto, setTexto] = useState('')
  const [soloActivos, setSoloActivos] = useState(true)
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaPacientes | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  const textoDiferido = useValorDiferido(texto, 300)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    const filtros = {
      texto: textoDiferido,
      activo: puedeGestionar ? soloActivos : true,
      pagina,
      tamano: TAMANO_PAGINA,
    }
    listarPacientes(filtros)
      .then((resultado) => {
        if (vigente) setDatos(resultado)
      })
      .catch((fallo) => {
        if (vigente) setError(mensajeDeFallo(fallo))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })
    return () => {
      vigente = false
    }
  }, [textoDiferido, soloActivos, pagina, puedeGestionar, reintento])

  // Al cambiar la busqueda o el filtro se vuelve a la primera pagina.
  useEffect(() => {
    setPagina(0)
  }, [textoDiferido, soloActivos])

  const residentes = useMemo(() => datos?.contenido ?? [], [datos])
  const abrirDetalle = (id: number) => navegar(`/residentes/${id}`)

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Residentes</p>
          <h2>Cuidado de los residentes</h2>
        </div>
        {puedeGestionar && (
          <BotonAccion onClick={() => navegar('/residentes/nuevo')}>
            <Plus size={17} /> Nuevo residente
          </BotonAccion>
        )}
      </div>

      <div className="barra-listado">
        <BuscadorResidentes valor={texto} alCambiar={setTexto} />
        {puedeGestionar && (
          <FiltroEstado soloActivos={soloActivos} alCambiar={setSoloActivos} />
        )}
      </div>

      {cargando && <EstadoCarga mensaje="Cargando residentes..." />}
      {!cargando && error && (
        <EstadoError
          mensaje={error}
          accion={
            <BotonAccion variant="outline" onClick={() => setReintento((n) => n + 1)}>
              Reintentar
            </BotonAccion>
          }
        />
      )}
      {!cargando && !error && residentes.length === 0 && (
        <EstadoVacio
          titulo="No hay residentes"
          descripcion="No se encontraron residentes con los criterios actuales."
        />
      )}
      {!cargando && !error && residentes.length > 0 && (
        <>
          <div className="listado-escritorio">
            <TablaListado residentes={residentes} alAbrir={abrirDetalle} />
          </div>
          <div className="listado-movil">
            <TarjetasListado residentes={residentes} alAbrir={abrirDetalle} />
          </div>
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              alCambiarPagina={setPagina}
            />
          )}
        </>
      )}
    </div>
  )
}
