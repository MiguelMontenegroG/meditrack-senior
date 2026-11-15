// Page objects del catalogo de medicamentos, de los esquemas de medicacion y
// de la agenda de tomas.

import { expect, type Locator, type Page } from '@playwright/test'
import { hoyEnBogota } from '../soporte/datos'

/** Catalogo de medicamentos (lectura ADMINISTRADOR y CUIDADOR_ENFERMERO). */
export class PaginaMedicamentos {
  constructor(private readonly pagina: Page) {}

  async abrir(): Promise<void> {
    await this.pagina.goto('/medicamentos')
    await expect(
      this.pagina.getByRole('heading', { name: 'Catalogo de medicamentos' }),
    ).toBeVisible()
  }

  async abrirNuevo(): Promise<void> {
    await this.pagina.goto('/medicamentos/nuevo')
    await expect(this.pagina.getByRole('heading', { name: 'Nuevo medicamento' })).toBeVisible()
  }

  async completarCampos(datos: { nombre: string; presentacion?: string }): Promise<void> {
    await this.pagina.getByLabel(/^Nombre$/).fill(datos.nombre)
    await this.pagina.getByLabel('Presentacion (opcional)').fill(datos.presentacion ?? '')
  }

  async guardar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Guardar medicamento/ }).click()
  }

  /** Error del campo nombre (para probar el 409 de duplicado). */
  errorNombre(): Locator {
    return this.pagina.locator('#error-nombre-med')
  }

  /** Error general del formulario de medicamento. */
  errorGeneral(): Locator {
    return this.pagina.locator('.form-error')
  }

  buscarPorTexto(texto: string): Promise<void> {
    return this.pagina.getByRole('searchbox', { name: 'Buscar medicamento por nombre' }).fill(texto)
  }

  fila(nombre: string): Locator {
    return this.pagina.getByRole('row').filter({ hasText: nombre }).first()
  }

  /** Filas del listado (incluye la cabecera de la tabla). */
  filas(): Locator {
    return this.pagina.getByRole('row')
  }

  /** Espera la fila del medicamento (el filtro tiene debounce). */
  async esperarFila(nombre: string): Promise<Locator> {
    const fila = this.fila(nombre)
    await expect(fila).toBeVisible()
    return fila
  }

  /** Espera a que el filtro deje exactamente `total` filas de datos. */
  async esperarFilas(total: number): Promise<void> {
    if (total === 0) {
      // Sin resultados el producto muestra el estado vacio (sin tabla).
      await expect(this.pagina.getByRole('row')).toHaveCount(0)
      return
    }
    await expect(this.filas()).toHaveCount(total + 1)
  }

  /** Abre la edicion del medicamento buscado. */
  async abrirEdicion(nombre: string): Promise<void> {
    await this.pagina.getByRole('button', { name: `Editar ${nombre}` }).click()
    await expect(this.pagina.getByRole('heading', { name: 'Editar medicamento' })).toBeVisible()
  }
}

/** Formulario de esquema de medicacion de un residente. */
export class PaginaFormularioEsquema {
  constructor(private readonly pagina: Page) {}

  async abrirNuevo(pacienteId: number): Promise<void> {
    await this.pagina.goto(`/residentes/${pacienteId}/esquemas/nuevo`)
    await expect(
      this.pagina.getByRole('heading', { name: 'Nuevo esquema de medicacion' }),
    ).toBeVisible()
  }

  /** Selecciona el medicamento por su nombre (la opcion agrega la presentacion). */
  async elegirMedicamento(nombreMedicamento: string): Promise<void> {
    const selector = this.pagina.getByLabel(/^Medicamento/)
    // El catalogo carga por red: se espera a que aparezca la opcion.
    const opcion = selector.locator('option', { hasText: nombreMedicamento }).first()
    await expect(opcion).toBeAttached()
    await selector.selectOption({ label: await opcionTexto(selector, nombreMedicamento) })
  }

  async completarDatos(datos: {
    dosis: string
    frecuenciaHoras: number
    horarios: string[]
    via?: string
    fechaInicio?: string
  }): Promise<void> {
    await this.pagina.getByLabel(/^Dosis/).fill(datos.dosis)
    await this.pagina.getByLabel(/^Frecuencia/).fill(String(datos.frecuenciaHoras))
    // La fecha de inicio es obligatoria y es un campo date: AAAA-MM-DD.
    await this.pagina.getByLabel(/^Fecha de inicio/).fill(datos.fechaInicio ?? hoyEnBogota())
    await this.pagina
      .getByLabel(/^Via de administracion/)
      .selectOption({ value: datos.via?.toUpperCase() ?? 'ORAL' })
    // El editor empieza con la lista de horarios vacia: se agrega un campo
    // por cada hora y luego se rellena.
    for (const [indice, hora] of datos.horarios.entries()) {
      await this.pagina.getByRole('button', { name: /Agregar horario/ }).click()
      await this.pagina
        .locator('.lista-horarios li input[type="time"]')
        .nth(indice)
        .fill(hora)
    }
  }

  async guardar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Guardar esquema/ }).click()
  }

  /** Espera la redireccion al detalle del residente tras guardar. */
  async esperarDetalle(pacienteId: number): Promise<void> {
    await expect(this.pagina).toHaveURL(new RegExp(`/residentes/${pacienteId}$`))
  }

  errorGeneral(): Locator {
    return this.pagina.locator('.form-error')
  }

  errorDeDosis(): Locator {
    return this.pagina.locator('#error-dosis')
  }
}

/** Texto exacto de la opcion del selector que contiene el nombre indicado. */
async function opcionTexto(selector: Locator, nombre: string): Promise<string> {
  const opcion = selector.locator('option', { hasText: nombre }).first()
  return ((await opcion.textContent()) ?? nombre).trim()
}

/** Seccion de esquemas del detalle del residente. */
export class SeccionEsquemas {
  constructor(private readonly pagina: Page) {}

  panel(): Locator {
    return this.pagina.getByRole('region', { name: 'Esquemas de medicacion' })
  }

  /** Item de la lista que corresponde al medicamento creado. */
  item(medicamentoNombre: string): Locator {
    return this.panel().getByRole('listitem').filter({ hasText: medicamentoNombre })
  }

  async esperarItem(medicamentoNombre: string): Promise<Locator> {
    const item = this.item(medicamentoNombre)
    await expect(item).toBeVisible()
    return item
  }

  async abrirAgenda(): Promise<void> {
    await this.panel().getByRole('button', { name: /Agenda/ }).click()
    await expect(this.pagina).toHaveURL(/\/residentes\/\d+\/tomas$/)
  }

  /** Boton de suspender/activar del item del medicamento. */
  botonEstado(medicamentoNombre: string): Locator {
    return this.item(medicamentoNombre).getByRole('button', { name: /Suspender|Activar/ })
  }
}

/** Agenda de tomas del residente (pestana "Agenda"). */
export class PaginaTomas {
  constructor(private readonly pagina: Page) {}

  async abrir(pacienteId: number): Promise<void> {
    await this.pagina.goto(`/residentes/${pacienteId}/tomas`)
  }

  async esperarCargada(medicamentoNombre: string): Promise<void> {
    await expect(
      this.pagina.getByRole('region', { name: `Agenda de ${medicamentoNombre}` }),
    ).toBeVisible()
  }

  /** Envoltura de la toma con la hora indicada (HH:mm). */
  slotDe(hora: string): Locator {
    return this.pagina
      .locator('.slot-toma')
      .filter({ has: this.pagina.locator('.slot-hora', { hasText: hora }) })
      .first()
  }

  /** Boton "Registrar" (exacto) de la toma con la hora indicada. */
  botonRegistrarDe(hora: string): Locator {
    return this.slotDe(hora).getByRole('button', { name: 'Registrar', exact: true })
  }

  /** Formulario en linea para registrar la toma de un horario. */
  formularioDe(hora: string): Locator {
    return this.pagina.getByRole('group', { name: `Registrar toma de las ${hora}` })
  }

  /** Pestana de historial. */
  async abrirHistorial(): Promise<void> {
    await this.pagina.getByRole('tab', { name: 'Historial' }).click()
  }

  /** Registra una toma como ADMINISTRADA para el horario indicado. */
  async registrarAdministrada(hora: string): Promise<void> {
    const formulario = this.formularioDe(hora)
    if (!(await formulario.isVisible())) {
      await this.botonRegistrarDe(hora).click()
      await expect(formulario).toBeVisible()
    }
    await formulario.getByRole('button', { name: /Administrada/ }).click()
    await formulario.getByRole('button', { name: /^Registrar toma$/ }).click()
    await expect(formulario).toBeHidden()
  }

  /** Registra una toma como OMITIDA con su motivo (obligatorio). */
  async registrarOmitida(hora: string, motivo: string): Promise<void> {
    const formulario = this.formularioDe(hora)
    if (!(await formulario.isVisible())) {
      await this.botonRegistrarDe(hora).click()
      await expect(formulario).toBeVisible()
    }
    await formulario.getByRole('button', { name: /Omitida/ }).click()
    await formulario.getByLabel(/Motivo de la omisi.n/).fill(motivo)
    await formulario.getByRole('button', { name: /^Registrar toma$/ }).click()
    await expect(formulario).toBeHidden()
  }

  /** Verifica que el formulario exige el motivo antes de enviar. */
  async errorDeOmitida(hora: string): Promise<Locator> {
    return this.formularioDe(hora).locator('.form-error')
  }

  /** Tabla del historial de tomas. */
  tablaHistorial(): Locator {
    return this.pagina.locator('.tabla-historial-tomas')
  }

  /** Fila del historial con el estado indicado (por texto). */
  filaHistorial(estado: RegExp): Locator {
    return this.tablaHistorial().locator('tbody tr').filter({ hasText: estado })
  }
}
