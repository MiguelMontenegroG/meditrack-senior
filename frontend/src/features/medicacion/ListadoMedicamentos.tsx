// Pantalla del catalogo de medicamentos. La gestion (alta/edicion/estado) es
// solo ADMINISTRADOR; el CUIDADOR_ENFERMERO solo consulta. Filtros por texto y
// estado, paginacion.

import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { EstadoError } from '@/components/ui/EstadoError'
import { EstadoVacio } from '@/components/ui/EstadoVacio'
import { Paginacion } from '@/components/ui/Paginacion'
import { mensajeDeFallo } from '@/lib/mensajes-error'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { puedeGestionarMedicamentos } from '@/features/medicacion/permisos'
import {
  BuscadorMedicamentos,
  FiltroEstadoMedicamentos,
} from '@/features/medicacion/ControlesMedicamentos'
import { listarMedicamentos } from '@/features/medicacion/services/medicamentoService'
import type { Medicamento, PaginaMedicamentos } from '@/features/medicacion/types'
import { TablaMedicamentos } from '@/features/medicacion/TablaMedicamentos'
import { TarjetasMedicamentos } from '@/features/medicacion/TarjetasMedicamentos'
import { useValorDiferido } from '@/features/pacientes/useValorDiferido'

const TAMANO_PAGINA = 10

/** Catalogo de medicamentos con busqueda, filtro de estado y paginacion. */
export function ListadoMedicamentosCatalogo() {
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()
  const puedeGestionar = puedeGestionarMedicamentos(usuario?.rol)

  const [texto, setTexto] = useState('')
  const [estado, setEstado] = useState<'activos' | 'inactivos' | 'todos'>('activos')
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaMedicamentos | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  const textoDiferido = useValorDiferido(texto, 300)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    const activo = estado === 'todos' ? undefined : estado === 'activos'
    listarMedicamentos({ texto: textoDiferido, activo, pagina, tamano: TAMANO_PAGINA })
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
  }, [textoDiferido, estado, pagina, reintento])

  // Al cambiar un filtro se vuelve a la primera pagina.
  useEffect(() => {
    setPagina(0)
  }, [textoDiferido, estado])

  const medicamentos = useMemo(() => datos?.contenido ?? [], [datos])
  const abrirEdicion = (medicamento: Medicamento) =>
    navegar(`/medicamentos/${medicamento.id}/editar`, { state: { medicamento } })

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Farmacia</p>
          <h2>Catalogo de medicamentos</h2>
        </div>
        {puedeGestionar && (
          <BotonAccion onClick={() => navegar('/medicamentos/nuevo')}>
            <Plus size={17} /> Nuevo medicamento
          </BotonAccion>
        )}
      </div>

      <div className="barra-listado">
        <BuscadorMedicamentos valor={texto} alCambiar={setTexto} />
        <FiltroEstadoMedicamentos filtro={estado} alCambiar={setEstado} />
      </div>

      {cargando && <EstadoCarga mensaje="Cargando medicamentos..." />}
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
      {!cargando && !error && medicamentos.length === 0 && (
        <EstadoVacio
          titulo="No hay medicamentos"
          descripcion="No se encontraron medicamentos con los criterios actuales."
        />
      )}
      {!cargando && !error && medicamentos.length > 0 && (
        <>
          <div className="listado-escritorio">
            <TablaMedicamentos
              medicamentos={medicamentos}
              puedeGestionar={puedeGestionar}
              alEditar={abrirEdicion}
            />
          </div>
          <div className="listado-movil">
            <TarjetasMedicamentos
              medicamentos={medicamentos}
              puedeGestionar={puedeGestionar}
              alEditar={abrirEdicion}
            />
          </div>
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'medicamento', plural: 'medicamentos' }}
              etiqueta="Paginacion de medicamentos"
              alCambiarPagina={setPagina}
            />
          )}
        </>
      )}
    </div>
  )
}
