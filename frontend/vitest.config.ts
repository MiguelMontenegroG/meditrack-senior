import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config'

// Configuracion de pruebas. Reutiliza la config de Vite (alias @ -> src)
// y agrega entorno jsdom, setup de Testing Library y cobertura (v8).
export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/test/setup.ts'],
      css: false,
      coverage: {
        provider: 'v8',
        // Informes en frontend/coverage/: lcov.info lo consume Sonar y
        // text-summary deja las cifras globales en el log de CI.
        reporter: ['lcov', 'text-summary'],
        reportsDirectory: './coverage',
        include: ['src/**/*.{ts,tsx}'],
        // No se mide cobertura de lo que no es codigo productivo probable:
        // pruebas, el punto de entrada de React (main.tsx), los archivos de
        // tipos (*.d.ts) y la configuracion raiz del frontend.
        exclude: [
          'src/**/*.test.{ts,tsx}',
          'src/test/**',
          'src/main.tsx',
          'src/vite-env.d.ts',
          'src/**/types.ts',
          '*.config.ts',
          '*.config.js',
        ],
      },
    },
  }),
)