// CP-30 a CP-32: modulo del familiar autorizado.
//
// Cubre el acceso de solo lectura: el familiar ve el residente autorizado, su
// bitacora y sus esquemas, pero no tiene formularios de registro ni rutas de
// administracion.
//
// El familiar se crea por API y se vincula al residente con su parentesco; el
// residente se reutiliza del cuidador (ya existe en la ejecucion).
import { test, expect } from '@playwright/test'
import { credencialesAdmin } from '../soporte/entorno'
import { clienteAdmin } from '../soporte/api'
import { DATOS, ESTADO, persistir } from '../soporte/datos'
import { crearUsuario, entrarComo, vincularFamiliar } from '../soporte/usuarios'
import { PaginaBitacora } from '../paginas/PaginaBitacora'
import { PaginaListadoResidentes } from '../paginas/PaginaResidentes'
import { SeccionEsquemas } from '../paginas/PaginaMedicamentos'

test.describe.serial('Familiar autorizado', () => {
  test('CP-30 prepara al familiar autorizado del residente', async ({}) => {
    const admin = credencialesAdmin()
    if (ESTADO.familiarId) {
      test.info().annotations.push({ type: 'nota', description: 'familiar ya creado' })
      return
    }
    const pacienteId = ESTADO.pacienteSanitarioId
    expect(pacienteId, 'CP-25 debe haber creado el residente').toBeTruthy()

    const api = await clienteAdmin(admin)
    try {
      const familiar = await crearUsuario(api, {
        nombreCompleto: DATOS.nombreFamiliar,
        correo: DATOS.correoFamiliar,
        contrasena: DATOS.contrasenaFamiliar,
        rol: 'FAMILIAR_AUTORIZADO',
      })
      await vincularFamiliar(api, pacienteId as number, familiar.id, 'Hija')
      ESTADO.familiarId = familiar.id
      ESTADO.familiarCorreo = DATOS.correoFamiliar
      ESTADO.familiarContrasena = DATOS.contrasenaFamiliar
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-31 el familiar ve al residente autorizado (solo lectura)', async ({ page }) => {
    expect(ESTADO.familiarCorreo, 'CP-30 debe preparar el familiar').toBeTruthy()
    await entrarComo(page, ESTADO.familiarCorreo as string, ESTADO.familiarContrasena as string)

    const listado = new PaginaListadoResidentes(page)
    await listado.abrir()
    await listado.buscarPorTexto(DATOS.documentoDuplicadoBase)

    // Aparece el residente autorizado...
    await expect(await listado.esperarFila(DATOS.nombreResidenteSanitario)).toBeVisible()
    // ...pero no la accion de alta (solo ADMINISTRADOR).
    await expect(page.getByRole('button', { name: 'Nuevo residente' })).toHaveCount(0)

    // La ruta de alta redirige fuera del formulario.
    await page.goto('/residentes/nuevo')
    await expect(page).not.toHaveURL(/\/residentes\/nuevo$/)
  })

  test('CP-32 el familiar consulta los esquemas sin poder registrarlos', async ({ page }) => {
    const pacienteId = ESTADO.pacienteSanitarioId
    expect(pacienteId, 'CP-25 debe haber creado el residente').toBeTruthy()
    expect(ESTADO.familiarCorreo, 'CP-30 debe preparar el familiar').toBeTruthy()

    await entrarComo(page, ESTADO.familiarCorreo as string, ESTADO.familiarContrasena as string)

    // Bitacora: puede leer el historial y no ve el formulario de registro.
    const bitacora = new PaginaBitacora(page)
    await bitacora.abrir(pacienteId as number, DATOS.nombreResidenteSanitario)
    await expect(page.getByTestId('tabla-signos')).toBeVisible()
    await expect(page.getByRole('button', { name: /Registrar signos/ })).toHaveCount(0)

    // Esquemas: puede ver el panel y no ve la accion de crear.
    await page.goto(`/residentes/${pacienteId}`)
    const esquemas = new SeccionEsquemas(page)
    await expect(esquemas.panel()).toBeVisible()
    await expect(esquemas.panel().getByRole('button', { name: /Nuevo esquema/ })).toHaveCount(0)
  })
})
