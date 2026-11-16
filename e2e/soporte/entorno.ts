// Lectura de la configuracion de las pruebas end-to-end.
//
// No se usa dotenv: las variables se leen de process.env y, si existe
// e2e/.env, se cargan con process.loadEnvFile dentro de un try/catch. El .env
// de la raiz del repositorio NUNCA se lee ni se imprime.
//
// Solo se exponen valores seguros de imprimir; las credenciales del
// administrador se validan aqui y nunca se registran en la salida.

import { existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

/** Raiz de la carpeta e2e/ (este archivo vive en e2e/soporte/). */
export const DIRECTORIO_E2E = join(dirname(fileURLToPath(import.meta.url)), '..')
/** Archivo .env opcional dentro de e2e/. */
const ARCHIVO_ENV = join(DIRECTORIO_E2E, '.env')

try {
  if (existsSync(ARCHIVO_ENV)) {
    process.loadEnvFile(ARCHIVO_ENV)
  }
} catch {
  // Un .env ilegible no debe romper el arranque: se sigue con process.env.
}

const BASE_URL_POR_DEFECTO = 'http://localhost:5173'
const API_URL_POR_DEFECTO = 'http://localhost:8080/api'

function texto(valor: string | undefined, defecto = ''): string {
  return (valor ?? '').trim() || defecto
}

/** Mensaje claro cuando faltan credenciales: nunca muestra valores. */
function exigirCredenciales(): { correo: string; contrasena: string } {
  const correo = texto(process.env.E2E_ADMIN_EMAIL)
  const contrasena = (process.env.E2E_ADMIN_PASSWORD ?? '').trim()
  const faltantes: string[] = []
  if (!correo) faltantes.push('E2E_ADMIN_EMAIL')
  if (!contrasena) faltantes.push('E2E_ADMIN_PASSWORD')
  if (faltantes.length > 0) {
    throw new Error(
      `Faltan variables de entorno obligatorias: ${faltantes.join(', ')}. ` +
        'Definelas en la sesion o en e2e/.env (ver e2e/.env.example).',
    )
  }
  return { correo, contrasena }
}

/** Configuracion resuelta una sola vez por proceso de Playwright. */
export const ENTORNO = (() => {
  const slowmoCrudo = Number(texto(process.env.E2E_SLOWMO, '0'))
  const slowmo = Number.isFinite(slowmoCrudo) && slowmoCrudo > 0 ? Math.trunc(slowmoCrudo) : 0
  return {
    baseUrl: texto(process.env.E2E_BASE_URL, BASE_URL_POR_DEFECTO).replace(/\/+$/, ''),
    apiUrl: texto(process.env.E2E_API_URL, API_URL_POR_DEFECTO).replace(/\/+$/, ''),
    slowmo,
  }
})()

/** Credenciales del administrador; se validan al usarlas, no al importar. */
export function credencialesAdmin(): { correo: string; contrasena: string } {
  return exigirCredenciales()
}
