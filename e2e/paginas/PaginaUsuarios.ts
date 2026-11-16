// Page objects del modulo de usuarios (solo ADMINISTRADOR).
//
// Cubre el listado con sus filtros, el formulario de alta y el boton de
// activar/desactivar de cada fila. El backend no borra usuarios: el estado se
// cambia con activar/desactivar.

import { expect, type Locator, type Page } from '@playwright/test'

/** Texto del rol tal como lo muestra el producto. */
export type RolVisible = 'Administrador' | 'Cuidador enfermero' | 'Familiar autorizado'

/** Formulario de alta de usuario (solo ADMINISTRADOR). */
export class PaginaFormularioUsuario {
  constructor(private readonly pagina: Page) {}

  async abrirNuevo(): Promise<void> {
    await this.pagina.goto('/usuarios/nuevo')
    await expect(
      this.pagina.getByRole('heading', { name: /^(Nuevo|Editar) usuario$/ }),
    ).toBeVisible()
  }

  async completarCampos(datos: {
    nombreCompleto: string
    correo: string
    contrasena: string
    rol: RolVisible
    telefono?: string
  }): Promise<void> {
    const formulario = this.pagina.locator('form')
    await formulario.getByLabel(/^Nombre completo/).fill(datos.nombreCompleto)
    await formulario.getByLabel(/^Correo/).fill(datos.correo)
    await formulario.getByLabel(/^Contrase.a/).fill(datos.contrasena)
    if (datos.telefono !== undefined) {
      await formulario.getByLabel(/^Tel.fono \(opcional\)/).fill(datos.telefono)
    }
    await formulario.getByLabel(/^Rol/).selectOption({ label: datos.rol })
  }

  async guardar(): Promise<void> {
    await this.pagina.getByRole('button', { name: /Guardar usuario/ }).click()
  }

  /** Espera la redireccion al listado tras guardar. */
  async esperarListado(): Promise<void> {
    await expect(this.pagina).toHaveURL(/\/usuarios$/)
  }

  /** Error mostrado sobre un campo (por ejemplo, correo duplicado). */
  errorDe(campo: string): Locator {
    return this.pagina.locator(`#error-${campo}`)
  }
}

/** Listado de usuarios con filtro de estado y acciones por fila. */
export class PaginaListadoUsuarios {
  constructor(private readonly pagina: Page) {}

  async abrir(): Promise<void> {
    await this.pagina.goto('/usuarios')
    await expect(this.pagina.getByRole('heading', { name: 'Gestion de usuarios' })).toBeVisible()
  }

  /** Fila del listado del usuario indicado (por nombre o correo). */
  fila(nombreOCorreo: string): Locator {
    return this.pagina.getByRole('row').filter({ hasText: nombreOCorreo })
  }

  async esperarFila(nombreOCorreo: string): Promise<Locator> {
    const fila = this.fila(nombreOCorreo)
    await expect(fila.first()).toBeVisible()
    return fila.first()
  }

  /** Abre la edicion del usuario con la accion de su fila. */
  async abrirEdicion(nombre: string): Promise<void> {
    await this.pagina.getByRole('button', { name: `Editar a ${nombre}` }).click()
    await expect(this.pagina).toHaveURL(/\/usuarios\/\d+\/editar$/)
  }

  /** Filtra el listado por rol (por ejemplo, Administrador). */
  async filtrarPorRol(rol: 'Administrador' | 'Cuidador enfermero' | 'Familiar autorizado') {
    await this.pagina.getByLabel(/^Rol/).selectOption({ label: rol })
  }

  /** Muestra solo usuarios activos (filtro por defecto). */
  filtrarActivos(): Promise<void> {
    return this.pagina.getByRole('button', { name: 'Activos', exact: true }).click()
  }

  /** Muestra solo usuarios inactivos. */
  filtrarInactivos(): Promise<void> {
    return this.pagina.getByRole('button', { name: 'Inactivos', exact: true }).click()
  }

  /**
   * Desactiva al usuario con el boton de su fila y confirma el dialogo en
   * linea. La fila se localiza por el nombre completo (el boton "Desactivar"
   * de la fila se distingue del de otras filas).
   */
  async desactivarEnFila(nombreOCorreo: string): Promise<void> {
    const fila = this.fila(nombreOCorreo).first()
    await expect(fila).toBeVisible()
    await fila.getByRole('button', { name: /^Desactivar$/ }).click()
    const dialogo = fila.getByRole('alertdialog', { name: /Confirmar desactivaci.n/ })
    await expect(dialogo).toBeVisible()
    await dialogo.getByRole('button', { name: /S., desactivar/ }).click()
  }

  /** Reactiva al usuario con el boton de su fila (sin dialogo). */
  async reactivarEnFila(nombreOCorreo: string): Promise<void> {
    const fila = this.fila(nombreOCorreo).first()
    await expect(fila).toBeVisible()
    await fila.getByRole('button', { name: /^Activar$/ }).click()
  }

  /** Conteo de filas de datos del listado (sin cabecera). */
  filasDeDatos(): Locator {
    return this.pagina.locator('.tabla-listado-usuarios tbody tr')
  }
}
