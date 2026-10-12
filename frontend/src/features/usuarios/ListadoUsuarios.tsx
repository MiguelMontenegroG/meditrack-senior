// Pantalla de gestion de usuarios. Solo ADMINISTRADOR (la ruta ya esta
// protegida). Filtros por rol y estado, paginacion y acciones por fila.

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
import { FiltroRol, FiltroEstadoUsuarios } from '@/features/usuarios/ControlesUsuarios'
import { listarUsuarios } from '@/features/usuarios/services/usuarioService'
import type { PaginaUsuarios, RolUsuario } from '@/features/usuarios/types'
import { TablaUsuarios } from '@/features/usuarios/TablaUsuarios'
import { TarjetasUsuarios } from '@/features/usuarios/TarjetasUsuarios'

const TAMANO_PAGINA = 10

/** Listado de usuarios con filtros por rol y estado, y paginacion. */
export function ListadoUsuarios() {
  const navegar = useNavigate()
  const { usuario } = useAutenticacion()

  const [rol, setRol] = useState<RolUsuario | ''>('')
  const [estado, setEstado] = useState<'activos' | 'inactivos' | 'todos'>('activos')
  const [pagina, setPagina] = useState(0)
  const [datos, setDatos] = useState<PaginaUsuarios | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reintento, setReintento] = useState(0)

  useEffect(() => {
    let vigente = true
    setCargando(true)
    setError(null)
    const activo = estado === 'todos' ? undefined : estado === 'activos'
    listarUsuarios({ rol: rol || undefined, activo, pagina, tamano: TAMANO_PAGINA })
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
  }, [rol, estado, pagina, reintento])

  // Al cambiar un filtro se vuelve a la primera pagina.
  useEffect(() => {
    setPagina(0)
  }, [rol, estado])

  const usuarios = useMemo(() => datos?.contenido ?? [], [datos])
  const abrirEdicion = (id: number) => navegar(`/usuarios/${id}/editar`)

  return (
    <div className="page-content">
      <div className="page-intro">
        <div>
          <p className="eyebrow">Administracion</p>
          <h2>Gestion de usuarios</h2>
        </div>
        <BotonAccion onClick={() => navegar('/usuarios/nuevo')}>
          <Plus size={17} /> Nuevo usuario
        </BotonAccion>
      </div>

      <div className="barra-listado">
        <FiltroRol rol={rol} alCambiar={setRol} />
        <FiltroEstadoUsuarios filtro={estado} alCambiar={setEstado} />
      </div>

      {cargando && <EstadoCarga mensaje="Cargando usuarios..." />}
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
      {!cargando && !error && usuarios.length === 0 && (
        <EstadoVacio
          titulo="No hay usuarios"
          descripcion="No se encontraron usuarios con los criterios actuales."
        />
      )}
      {!cargando && !error && usuarios.length > 0 && (
        <>
          <div className="listado-escritorio">
            <TablaUsuarios
              usuarios={usuarios}
              usuarioActualId={usuario?.id}
              alEditar={abrirEdicion}
            />
          </div>
          <div className="listado-movil">
            <TarjetasUsuarios
              usuarios={usuarios}
              usuarioActualId={usuario?.id}
              alEditar={abrirEdicion}
            />
          </div>
          {datos && datos.totalPaginas > 1 && (
            <Paginacion
              pagina={datos.pagina}
              totalPaginas={datos.totalPaginas}
              total={datos.total}
              sustantivo={{ singular: 'usuario', plural: 'usuarios' }}
              etiqueta="Paginacion de usuarios"
              alCambiarPagina={setPagina}
            />
          )}
        </>
      )}
    </div>
  )
}
