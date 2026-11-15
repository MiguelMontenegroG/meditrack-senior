// Configuracion de Playwright para las pruebas end-to-end de MediTrack Senior.
//
// - Solo Chromium.
// - Serie (un worker): los pasos del flujo comparten estado de negocio.
// - Evidencia lista para un video: video on, screenshot on, trace
//   retain-on-failure, reporte list + html.
// - Zona horaria y locale de Colombia para que fechas y textos coincidan con el
//   producto.

import { defineConfig, devices } from '@playwright/test'
import { ENTORNO } from './soporte/entorno'

export default defineConfig({
  testDir: './pruebas',
  // El flujo es secuencial y cada caso depende del anterior: un worker y sin
  // paralelismo entre archivos.
  fullyParallel: false,
  workers: 1,
  retries: 0,
  forbidOnly: Boolean(process.env.CI),
  timeout: 90_000,
  expect: { timeout: 15_000 },
  outputDir: './test-results',
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
  ],
  use: {
    baseURL: ENTORNO.baseUrl,
    timezoneId: 'America/Bogota',
    locale: 'es-CO',
    video: 'on',
    screenshot: 'on',
    trace: 'retain-on-failure',
    launchOptions: { slowMo: ENTORNO.slowmo },
    actionTimeout: 20_000,
    navigationTimeout: 30_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
