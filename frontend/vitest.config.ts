import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config'

// Configuracion de pruebas. Reutiliza la config de Vite (alias @ -> src)
// y agrega entorno jsdom + setup de Testing Library.
export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/test/setup.ts'],
      css: false,
    },
  }),
)