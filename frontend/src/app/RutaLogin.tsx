import { Navigate, useNavigate } from 'react-router-dom'
import { EstadoCarga } from '@/components/ui/EstadoCarga'
import { InicioSesion } from '@/features/auth/InicioSesion'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { RUTA_INICIAL_ROL, type UsuarioAutenticado } from '@/features/auth/types'

// Envoltura del login: si el usuario ya tiene sesion, lo envia a la ruta
// inicial de su rol; mientras se restaura la sesion muestra un estado de carga.

/** Ruta publica de inicio de sesion con redireccion por rol. */
export function RutaLogin() {
  const { usuario, cargando } = useAutenticacion()
  const navegar = useNavigate()

  if (cargando) {
    return <EstadoCarga mensaje="Verificando tu sesion..." />
  }
  if (usuario) {
    return <Navigate to={RUTA_INICIAL_ROL[usuario.rol]} replace />
  }
  const alIngresar = (autenticado: UsuarioAutenticado) =>
    navegar(RUTA_INICIAL_ROL[autenticado.rol], { replace: true })
  return <InicioSesion alIngresar={alIngresar} />
}