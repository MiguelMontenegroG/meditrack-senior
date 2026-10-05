import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { BarraLateral } from '@/components/layout/BarraLateral'
import { EncabezadoApp } from '@/components/layout/EncabezadoApp'
import { NavegacionInferior } from '@/components/layout/NavegacionInferior'

// Layout de las rutas autenticadas: barra lateral (escritorio), encabezado,
// contenido de la ruta y navegacion inferior (movil). El estado de navegacion
// (elemento activo) vive aqui y se comparte con las barras.

interface PropsMainLayout {
  /** Titulo mostrado en el encabezado para esta seccion. */
  titulo: string
}

/** Estructura visual comun a las pantallas internas de la aplicacion. */
export function MainLayout({ titulo }: PropsMainLayout) {
  const [activo, setActivo] = useState('Panel')

  return (
    <div className="app-shell">
      <BarraLateral activo={activo} alSeleccionar={setActivo} />
      <div className="main-area">
        <EncabezadoApp titulo={titulo} />
        <main>
          <Outlet />
        </main>
      </div>
      <NavegacionInferior activo={activo} alSeleccionar={setActivo} />
    </div>
  )
}
