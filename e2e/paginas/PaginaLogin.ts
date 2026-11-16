// Page object de la pantalla de inicio de sesion.
//
// Hace login por formulario y verifica que el usuario quede fuera de /login
// (la redireccion depende del rol).

import { expect, type Page } from '@playwright/test'

export class PaginaLogin {
  constructor(private readonly pagina: Page) {}

  async abrir(): Promise<void> {
    await this.pagina.goto('/login')
    await expect(this.pagina.getByRole('heading', { name: 'Bienvenido de nuevo' })).toBeVisible()
  }

  async ingresar(correo: string, contrasena: string): Promise<void> {
    await this.abrir()
    const fallosRed: string[] = []
    const alFallar = (peticion: import('@playwright/test').Request) => {
      if (peticion.url().includes('/auth/')) fallosRed.push(`${peticion.method()} ${peticion.url()}`)
    }
    this.pagina.on('requestfailed', alFallar)
    try {
      await this.pagina.getByLabel('Correo electronico').fill(correo)
      // El label de la contrasena envuelve tambien el boton Mostrar/Ocultar.
      await this.pagina.locator('input[name="contrasena"]').fill(contrasena)
      await this.pagina.getByRole('button', { name: 'Ingresar al sistema' }).click()
      await expect(this.pagina).not.toHaveURL(/\/login$/)
      // Espera a que la sesion quede persistida antes de seguir.
      await expect
        .poll(() => this.pagina.evaluate(() => sessionStorage.getItem('meditrack.token')))
        .not.toBeNull()
    } catch (error) {
      if (fallosRed.length > 0) {
        throw new Error(`Fallo de red durante el login: ${fallosRed.join(', ')}`)
      }
      throw error
    } finally {
      this.pagina.off('requestfailed', alFallar)
    }
  }
}
