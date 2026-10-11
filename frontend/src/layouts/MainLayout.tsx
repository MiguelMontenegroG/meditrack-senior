import { Outlet } from 'react-router-dom'
import { BarraLateral } from '@/components/layout/BarraLateral'
import { EncabezadoApp } from '@/components/layout/EncabezadoApp'
import { NavegacionInferior } from '@/components/layout/NavegacionInferior'

// Layout de las rutas autenticadas: barra lateral (escritorio), encabezado,
// contenido de la ruta y navegacion inferior (movil). La navegacion resuelve
// sus propias rutas por rol con NavLink; el layout solo aporta la estructura.
interface PropsMainLayout {
  /** Titulo mostrado en el encabezado para esta seccion. */
  titulo: string
}

/** Estructura visual comun a las pantallas internas de la aplicacion. */
export function MainLayout({ titulo }: PropsMainLayout) {
  return (
    <div className="app-shell">
      <BarraLateral />
      <div className="main-area">
        <EncabezadoApp titulo={titulo} />
        <main>
          <Outlet />
        </main>
      </div>
      <NavegacionInferior />
    </div>
  )
}
