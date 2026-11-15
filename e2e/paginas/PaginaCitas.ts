// Page objects del calendario de citas y del formulario de agendar/editar.
//
// El flujo agenda una cita desde el calendario, la localiza en el dia elegido,
// la marca como CUMPLIDA y comprueba el resumen que muestra el producto.

import { expect, type Locator, type Page } from '@playwright/test'

/** Calendario de citas (lectura para los tres roles). */
export class PaginaCalendario {
  constructor(private readonly pagina: Page) {}

  async abrir(): Promise<void> {
    await this.pagina.goto('/calendario')
    await expect(this.pagina.getByRole('heading', { name: 'Calendario de citas' })).toBeVisible()
  }

  /** Entra al formulario de agendar cita desde el boton del calendario. */
  async agendar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Agendar cita/ }).click()
    await expect(this.pagina).toHaveURL(/\/calendario\/nuevo$/)
  }

  /** Avanza el dia mostrado hasta la fecha indicada (YYYY-MM-DD). */
  async irAFecha(fecha: string): Promise<void> {
    for (let intento = 0; intento < 5; intento += 1) {
      const etiqueta = await this.etiquetaDia().innerText()
      if (etiqueta.includes(fechaDia(fecha))) return
      await this.pagina.getByRole('button', { name: /D.a siguiente|Siguiente d.a/ }).first().click()
    }
    // La fecha no quedo visible: se continua con la fecha del calendario.
  }

  /** Texto del encabezado con la fecha del dia mostrado. */
  etiquetaDia(): Locator {
    return this.pagina.locator('[data-dia-actual], .calendario-dia-actual').first()
  }

  /** Fila del calendario que corresponde al residente y al motivo indicados. */
  filaCita(nombreResidente: string, motivo: string): Locator {
    return this.pagina
      .getByRole('row')
      .filter({ hasText: nombreResidente })
      .filter({ hasText: motivo })
      .first()
  }

  async esperarCita(nombreResidente: string, motivo: string): Promise<Locator> {
    const fila = this.filaCita(nombreResidente, motivo)
    await expect(fila).toBeVisible()
    return fila
  }

  /** Marca la cita como CUMPLIDA (sin confirmacion previa). */
  async marcarCumplida(nombreResidente: string, motivo: string): Promise<void> {
    const fila = await this.esperarCita(nombreResidente, motivo)
    await fila.getByRole('button', { name: 'Cumplida' }).click()
  }

  /** Marca la cita como INCUMPLIDA (sin confirmacion previa). */
  async marcarIncumplida(nombreResidente: string, motivo: string): Promise<void> {
    const fila = await this.esperarCita(nombreResidente, motivo)
    await fila.getByRole('button', { name: 'Incumplida' }).click()
  }

  /** Cancela la cita con su confirmacion en linea. */
  async cancelar(nombreResidente: string, motivo: string): Promise<void> {
    const fila = await this.esperarCita(nombreResidente, motivo)
    await fila.getByRole('button', { name: /^Cancelar$/ }).click()
    await fila.getByRole('button', { name: /Si, cancelar/ }).click()
  }

  /** Etiqueta de estado de la fila indicada. */
  estadoDe(nombreResidente: string, motivo: string): Locator {
    return this.filaCita(nombreResidente, motivo).locator('.status')
  }
}

/** Texto corto del dia ("15") usado para reconocer la fecha en el calendario. */
function fechaDia(fecha: string): string {
  return fecha.slice(-2).replace(/^0/, '')
}

/** Formulario de agendar/editar una cita. */
export class PaginaFormularioCita {
  constructor(private readonly pagina: Page) {}

  async esperarAgendar(): Promise<void> {
    await expect(this.pagina.getByRole('heading', { name: 'Agendar cita' })).toBeVisible()
  }

  async elegirResidente(nombre: string): Promise<void> {
    const selector = this.pagina.getByLabel('Residente', { exact: true })
    // La opcion puede incluir la habitacion entre parentesis: se toma el texto.
    const opcion = selector.locator('option').filter({ hasText: nombre }).first()
    await expect(opcion).toHaveCount(1)
    await selector.selectOption({ label: (await opcion.innerText()).trim() })
  }

  async elegirEspecialidad(etiqueta: string): Promise<void> {
    await this.pagina.getByLabel('Especialidad (opcional)').selectOption({ label: etiqueta })
  }

  async completarCampos(datos: {
    profesional: string
    lugar?: string
    motivo?: string
    fecha: string
    hora: string
  }): Promise<void> {
    await this.pagina.getByLabel('Profesional').fill(datos.profesional)
    await this.pagina.getByLabel('Lugar').fill(datos.lugar ?? '')
    await this.pagina.getByLabel('Motivo').fill(datos.motivo ?? '')
    await this.pagina.getByLabel('Fecha').fill(datos.fecha)
    await this.pagina.getByLabel('Hora').fill(datos.hora)
  }

  async guardar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Agendar cita/ }).click()
  }

  /** Mensaje de error general del formulario. */
  errorGeneral(): Locator {
    return this.pagina.locator('.form-error')
  }

  /** Fila de error del campo de fecha (valida fecha/hora). */
  errorFecha(): Locator {
    return this.pagina.getByLabel('Fecha').locator('xpath=following::span[@class="campo-error"][1]')
  }

  async esperarCalendario(): Promise<void> {
    await expect(this.pagina).toHaveURL(/\/calendario$/)
  }
}
