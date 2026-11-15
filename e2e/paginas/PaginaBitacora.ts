// Page object de la bitacora de signos vitales de un residente.
//
// Permite registrar un registro (fecha/hora actuales de Bogota) y comprobar
// que aparece en el historial.

import { expect, type Locator, type Page } from '@playwright/test'
import { horaActualEnBogota, hoyEnBogota } from '../soporte/datos'

/** Pantalla de bitacora (signos vitales) de un residente. */
export class PaginaBitacora {
  constructor(private readonly pagina: Page) {}

  async abrir(pacienteId: number, nombreResidente: string): Promise<void> {
    await this.pagina.goto(`/residentes/${pacienteId}/signos-vitales`)
    await expect(
      this.pagina.getByRole('heading', { name: 'Signos vitales', level: 2 }),
    ).toBeVisible()
    await expect(this.pagina.getByText(nombreResidente, { exact: false }).first()).toBeVisible()
  }

  async abrirFormulario(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Registrar signos$/ }).click()
    await expect(
      this.pagina.getByRole('heading', { name: 'Registrar signos vitales' }),
    ).toBeVisible()
  }

  async completarSignos(datos: {
    sistolica: string
    diastolica: string
    glucosa: string
    temperatura: string
    observaciones?: string
  }): Promise<void> {
    const form = this.pagina.locator('form.formulario-signos')
    // Se registra con una hora reciente pero ya pasada (la del formulario
    // es "ahora" y el historial filtra por fecha_hora <= hasta, asi que un
    // registro de este mismo instante puede quedar fuera del rango).
    const { fecha, hora } = instantePasadoBogota(5)
    await form.getByLabel('Fecha').fill(fecha)
    await form.getByLabel('Hora').fill(hora)
    await form.getByLabel('Presion sistolica (mmHg)').fill(datos.sistolica)
    await form.getByLabel('Presion diastolica (mmHg)').fill(datos.diastolica)
    await form.getByLabel('Glucosa (mg/dL)').fill(datos.glucosa)
    await form.getByLabel('Temperatura (grados C)').fill(datos.temperatura)
    if (datos.observaciones !== undefined) {
      await form.getByLabel('Observaciones (opcional)').fill(datos.observaciones)
    }
  }

  async guardar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /^Registrar signos$/ }).last().click()
  }

  /** Tabla del historial: primer registro (los mas recientes van primero). */
  primerRegistro(): Locator {
    return this.pagina.getByRole('row').nth(1)
  }

  /** Verifica que el registro con la presion indicada aparece en el historial. */
  async esperarRegistroConPresion(presion: string): Promise<void> {
    await expect(
      this.pagina.getByRole('row').filter({ hasText: presion }).first(),
    ).toBeVisible()
  }
}

/** Fecha (AAAA-MM-DD) y hora (HH:mm) de hace `minutos` minutos en Bogota. */
function instantePasadoBogota(minutos: number): { fecha: string; hora: string } {
  const horaTexto = horaActualEnBogota()
  const [hh, mm] = horaTexto.split(':').map(Number)
  const totalDia = hh * 60 + mm - minutos
  const mismoDia = totalDia >= 0
  const acotado = mismoDia ? totalDia : 0
  const h = String(Math.floor(acotado / 60)).padStart(2, '0')
  const m = String(acotado % 60).padStart(2, '0')
  // Si restar cruza la medianoche, se usa la medianoche de hoy (sigue siendo
  // un instante pasado del mismo dia).
  return { fecha: hoyEnBogota(), hora: `${h}:${m}` }
}
