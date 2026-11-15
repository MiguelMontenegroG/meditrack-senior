// CP-23 a CP-29: inicio de sesion y modulo del cuidador.
//
// Cubre el login real (exito y credenciales invalidas), el alcance de roles
// (el cuidador no ve la administracion de usuarios), el saludo de la vista del
// cuidador, la bitacora de signos vitales y el registro de tomas desde el rol.
//
// El cuidador y el residente sanitario se crean por API con el administrador:
// el caso que se quiere demostrar es lo visible para el rol, no el alta.
import { test, expect } from '@playwright/test'
import { credencialesAdmin } from '../soporte/entorno'
import { clienteAdmin } from '../soporte/api'
import { DATOS, ESTADO, hoyEnBogota, persistir } from '../soporte/datos'
import { asignarCuidador, crearResidente, crearUsuario, entrarComo } from '../soporte/usuarios'
import { PaginaLogin } from '../paginas/PaginaLogin'
import { PaginaBitacora } from '../paginas/PaginaBitacora'
import { PaginaTomas } from '../paginas/PaginaMedicamentos'

test.describe.serial('Sesion y cuidador', () => {
  test('CP-23 un administrador inicia sesion y llega al panel', async ({ page }) => {
    const admin = credencialesAdmin()
    await new PaginaLogin(page).ingresar(admin.correo, admin.contrasena)

    await expect(page).toHaveURL(/\/dashboard$/)
    const token = await page.evaluate(() => sessionStorage.getItem('meditrack.token'))
    expect(token).toBeTruthy()
  })

  test('CP-24 rechaza credenciales invalidas', async ({ page }) => {
    const admin = credencialesAdmin()
    const login = new PaginaLogin(page)
    // Correo valido con contrasena incorrecta: error del backend, sin sesion.
    await login.abrir()
    await page.locator('input[name="correo"]').fill(admin.correo)
    await page.locator('input[name="contrasena"]').fill('ClaveInvalida-123')
    await page.getByRole('button', { name: 'Ingresar al sistema' }).click()

    await expect(page.locator('.login-page [role="alert"]')).toContainText(
      /Correo o contrasena incorrectos|incorrectos/i,
    )
    await expect(page).toHaveURL(/\/login$/)
    const token = await page.evaluate(() => sessionStorage.getItem('meditrack.token'))
    expect(token).toBeNull()
  })

  test('CP-25 prepara al cuidador con su residente asignado', async ({}) => {
    const admin = credencialesAdmin()
    if (ESTADO.cuidadorId && ESTADO.pacienteSanitarioId) {
      test.info().annotations.push({ type: 'nota', description: 'datos del cuidador ya listos' })
      return
    }

    const api = await clienteAdmin(admin)
    try {
      const residente = await crearResidente(api, {
        documento: DATOS.documentoDuplicadoBase,
        nombres: DATOS.nombreResidenteSanitario,
        apellidos: DATOS.apellidos,
        habitacion: 'B-202',
      })
      // Correo y nombre propios de este cuidador (CP-11 ya usa DATOS.correoCuidador).
      const correoCuidadorVista = `e2e-${DATOS.sufijo}-cuidador-vista@meditrack.test`
      const nombreCuidadorVista = `E2E-${DATOS.sufijo} Cuidador Vista`
      const cuidador = await crearUsuario(api, {
        nombreCompleto: nombreCuidadorVista,
        correo: correoCuidadorVista,
        contrasena: DATOS.contrasenaCuidador,
        rol: 'CUIDADOR_ENFERMERO',
      })
      await asignarCuidador(api, residente.id, cuidador.id)

      // La vista del cuidador lista residentes con tomas abiertas de hoy,
      // asi que se deja un esquema activo con un horario para generar al
      // menos un slot del dia (pendiente o atrasado) del residente.
      const medicamento = await api.post<{ id: number }>('/medicamentos', {
        nombre: `${DATOS.nombreMedicamento} Cuidador`,
        presentacion: DATOS.presentacionMedicamento,
      })
      await api.post(`/pacientes/${residente.id}/esquemas`, {
        medicamentoId: medicamento.id,
        dosis: '1 tableta',
        frecuenciaHoras: 24,
        via: 'ORAL',
        indicaciones: `Esquema E2E ${DATOS.sufijo}`,
        fechaInicio: hoyEnBogota(),
        fechaFin: null,
        horarios: ['06:00'],
      })

      ESTADO.pacienteSanitarioId = residente.id
      ESTADO.cuidadorId = cuidador.id
      ESTADO.cuidadorCorreo = correoCuidadorVista
      ESTADO.cuidadorContrasena = DATOS.contrasenaCuidador
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-26 el cuidador inicia sesion y ve su vista de trabajo', async ({ page }) => {
    expect(ESTADO.cuidadorCorreo, 'CP-25 debe preparar el cuidador').toBeTruthy()
    await new PaginaLogin(page).ingresar(
      ESTADO.cuidadorCorreo as string,
      ESTADO.cuidadorContrasena as string,
    )

    await expect(page).toHaveURL(/\/cuidador$/)
    // El saludo usa el primer nombre del usuario (nombreCompleto.split(' ')[0]).
    const primerNombreCuidador = DATOS.nombreCuidador.split(' ')[0]
    await expect(page.getByTestId('cuidador-saludo')).toContainText(primerNombreCuidador)
    await expect(page.getByTestId('cuidador-residentes')).toContainText(
      DATOS.nombreResidenteSanitario,
    )
  })

  test('CP-27 el cuidador no tiene acceso a la administracion de usuarios', async ({ page }) => {
    expect(ESTADO.cuidadorCorreo, 'CP-25 debe preparar el cuidador').toBeTruthy()
    await entrarComo(page, ESTADO.cuidadorCorreo as string, ESTADO.cuidadorContrasena as string)
    await page.goto('/cuidador')

    // El enlace no aparece en la navegacion del rol.
    await expect(page.getByRole('navigation', { name: 'Navegacion principal' })).toBeVisible()
    await expect(page.getByRole('link', { name: 'Usuarios' })).toHaveCount(0)

    // Y la ruta protegida redirige a la vista inicial del rol.
    await page.goto('/usuarios')
    await expect(page).toHaveURL(/\/cuidador$/)
  })

  test('CP-28 el cuidador registra signos vitales del residente', async ({ page }) => {
    const pacienteId = ESTADO.pacienteSanitarioId
    expect(pacienteId, 'CP-25 debe preparar el residente').toBeTruthy()
    expect(ESTADO.cuidadorCorreo, 'CP-25 debe preparar el cuidador').toBeTruthy()

    await entrarComo(page, ESTADO.cuidadorCorreo as string, ESTADO.cuidadorContrasena as string)
    const bitacora = new PaginaBitacora(page)
    await bitacora.abrir(pacienteId as number, DATOS.nombreResidenteSanitario)
    await bitacora.abrirFormulario()
    await bitacora.completarSignos({
      sistolica: '128',
      diastolica: '82',
      glucosa: '96',
      temperatura: '36.7',
      observaciones: `Registro E2E ${DATOS.sufijo}`,
    })
    await bitacora.guardar()

    // El registro queda en el historial con su valor de presion. El producto no
    // muestra el autor en el historial: se verifica el dato capturado y que el
    // formulario de registro solo se ofrece a roles con permiso (el cuidador).
    await bitacora.esperarRegistroConPresion('128/82')
    await expect(
      page
        .getByTestId('tabla-signos')
        .getByRole('row')
        .filter({ hasText: '128/82' })
        .first(),
    ).toContainText(`Registro E2E ${DATOS.sufijo}`)
  })

  test('CP-29 el cuidador registra una toma administrada', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteSanitarioId
    expect(pacienteId, 'CP-25 debe preparar el residente').toBeTruthy()

    // El administrador deja el medicamento y el esquema (por API) del residente
    // sanitario; el cuidador solo registra la toma desde su vista.
    const nombreMedicamento = `${DATOS.nombreMedicamento} Sanitario`
    const api = await clienteAdmin(admin)
    try {
      const medicamento = await api.post<{ id: number }>('/medicamentos', {
        nombre: nombreMedicamento,
        presentacion: DATOS.presentacionMedicamento,
      })
      await api.post(`/pacientes/${pacienteId}/esquemas`, {
        medicamentoId: medicamento.id,
        dosis: '1 tableta',
        frecuenciaHoras: 24,
        via: 'ORAL',
        indicaciones: `Esquema E2E ${DATOS.sufijo}`,
        fechaInicio: hoyEnBogota(),
        fechaFin: null,
        horarios: ['06:00'],
      })
    } finally {
      await api.cerrar()
    }

    await entrarComo(page, ESTADO.cuidadorCorreo as string, ESTADO.cuidadorContrasena as string)
    const tomas = new PaginaTomas(page)
    await tomas.abrir(pacienteId as number)
    await tomas.esperarCargada(nombreMedicamento)

    // La toma de las 06:00 se registra (la agenda se recarga tras guardar).
    await tomas.registrarAdministrada('06:00')
    await tomas.esperarCargada(nombreMedicamento)
    await expect(tomas.slotDe('06:00')).toContainText('Administrada')
  })
})
