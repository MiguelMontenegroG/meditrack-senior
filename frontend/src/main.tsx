import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

// Fuente Atkinson Hyperlegible, disenada para baja vision: mejora la
// legibilidad en adultos mayores. Se importan solo los pesos usados en el CSS.
import '@fontsource/atkinson-hyperlegible/400.css'
import '@fontsource/atkinson-hyperlegible/700.css'

import '@/styles/globals.css'
import App from '@/App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
