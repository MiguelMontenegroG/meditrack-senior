// Page objects del modulo de residentes.
//
// Cubren el listado, el formulario de alta/edicion, el detalle (donde viven las
// acciones) y el boton de activar/desactivar con su dialogo de motivo.

import { expect, type Locator, type Page } from '@playwright/test'

/** Formulario de alta de residente (solo ADMINISTRADOR). */
export class PaginaFormularioResidente {
  constructor(private readonly pagina: Page) {}

  async abrirNuevo(): Promise<void> {
    await this.pagina.goto('/residentes/nuevo')
    await expect(
      this.pagina.getByRole('heading', { name: /^(Nuevo|Editar) residente$/ }),
    ).toBeVisible()
  }

  async completarCampos(datos: {
    documento: string
    nombres: string
    apellidos: string
    fechaNacimiento: string
    situacionClinica: string
    habitacion?: string
  }): Promise<void> {
    // Los campos van en orden fijo dentro del formulario: documento, nombres,
    // apellidos, fecha de nacimiento, sexo, habitacion, situacion clinica,
    // fecha de ingreso y acciones. Se usan posiciones para no depender de los
    // textos con tildes de las etiquetas.
    const formulario = this.pagina.locator('form')
    await formulario.getByLabel(/^Documento/).fill(datos.documento)
    await formulario.getByLabel(/^Nombres/).fill(datos.nombres)
    await formulario.getByLabel(/^Apellidos/).fill(datos.apellidos)
    await formulario.getByLabel(/^Fecha de nacimiento/).fill(datos.fechaNacimiento)
    if (datos.habitacion !== undefined) {
      await formulario.getByLabel(/^Habitaci.n \(opcional\)/).fill(datos.habitacion)
    }
    await formulario.getByLabel(/^Situaci.n cl.nica/).fill(datos.situacionClinica)
  }

  async guardar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Guardar residente/ }).click()
  }

  /** Error mostrado sobre un campo (span con role alert del producto). */
  errorDe(campo: string): Locator {
    return this.pagina.locator(`#error-${campo}`)
  }

  /** Espera la redireccion al listado tras guardar. */
  async esperarListado(): Promise<void> {
    await expect(this.pagina).toHaveURL(/\/residentes$/)
  }

  /** Texto de error general del formulario (validacion o red). */
  errorGeneral(): Locator {
    return this.pagina.locator('.form-error')
  }
}

/** Listado de residentes con filtro por texto y paginacion implicita. */
export class PaginaListadoResidentes {
  constructor(private readonly pagina: Page) {}

  async abrir(): Promise<void> {
    await this.pagina.goto('/residentes')
    await expect(
      this.pagina.getByRole('heading', { name: 'Cuidado de los residentes' }),
    ).toBeVisible()
  }

  /**
   * Escribe el texto en el buscador. El producto espera unos milisegundos
   * (debounce) antes de consultar, asi que los casos deben esperar el efecto
   * con `esperarFila`/`esperarFilas` en lugar de en cuanto se escribe.
   */
  buscarPorTexto(texto: string): Promise<void> {
    return this.pagina.getByRole('searchbox', { name: /Buscar residente/ }).fill(texto)
  }

  /** Fila de la tabla que corresponde al residente buscado. */
  filaResidente(nombre: string): Locator {
    return this.pagina.getByRole('row').filter({ hasText: nombre })
  }

  /** Filas del listado (incluye la cabecera). */
  filas(): Locator {
    return this.pagina.getByRole('row')
  }

  /** Espera a que el texto se aplique al filtro y aparezca la fila. */
  async esperarFila(nombre: string): Promise<Locator> {
    const fila = this.filaResidente(nombre)
    await expect(fila.first()).toBeVisible()
    return fila.first()
  }

  /** Espera a que el filtro deje exactamente `total` filas de datos. */
  async esperarFilas(total: number): Promise<void> {
    if (total === 0) {
      // Con cero resultados el producto muestra el estado vacio (sin tabla).
      await expect(this.pagina.getByRole('row')).toHaveCount(0)
      return
    }
    // Se resta la fila de cabecera de la tabla.
    await expect(this.filas()).toHaveCount(total + 1)
  }

  /** Abre el detalle del residente con la accion de su fila. */
  async abrirDetalle(nombre: string): Promise<void> {
    await this.pagina.getByRole('button', { name: `Ver detalle de ${nombre}` }).click()
    await expect(this.pagina).toHaveURL(/\/residentes\/\d+$/)
  }

  /** Quita el filtro de activos para encontrar residentes dados de baja. */
  async mostrarTodos(): Promise<void> {
    await this.pagina.getByRole('button', { name: 'Todos' }).click()
  }
}

/** Detalle del residente: datos, esquemas y acciones de estado. */
export class PaginaDetalleResidente {
  constructor(private readonly pagina: Page) {}

  async esperarCargado(nombre: string): Promise<void> {
    await expect(
      this.pagina.getByRole('heading', { name: nombre, exact: false }).first(),
    ).toBeVisible()
  }

  botonDesactivar(): Locator {
    return this.pagina.getByRole('button', { name: /^Desactivar$/ })
  }

  botonActivar(): Locator {
    return this.pagina.getByRole('button', { name: /^(Activar|Activando\.\.\.)$/ })
  }

  /** Completa el motivo y confirma la desactivacion. */
  async confirmarDesactivacion(motivo: string): Promise<void> {
    await this.botonDesactivar().click()
    const dialogo = this.pagina.getByRole('alertdialog', { name: /Confirmar desactivacion/ })
    await expect(dialogo).toBeVisible()
    await dialogo.getByLabel(/Motivo de la desactivacion/).fill(motivo)
    await dialogo.getByRole('button', { name: /Si, desactivar/ }).click()
  }

  /** Reactiva al residente (sin dialogo, aplica de inmediato). */
  async reactivar(): Promise<void> {
    await this.botonActivar().click()
  }

  /** Etiqueta Activo/Inactivo de la cabecera del detalle. */
  estado(): Locator {
    return this.pagina.locator('.cabecera-detalle .status').first()
  }

  /** Panel de esquemas de medicacion del residente. */
  panelEsquemas(): Locator {
    return this.pagina.getByRole('region', { name: 'Esquemas de medicacion' })
  }
}
