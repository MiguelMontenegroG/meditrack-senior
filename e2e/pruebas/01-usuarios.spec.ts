// CP-09 a CP-13: modulo de usuarios (solo ADMINISTRADOR).
//
// Cubre el listado, la validacion del cliente, el alta visible, el rechazo del
// correo duplicado (409) y el cambio de estado (desactivar/reactivar). El
// cuidador creado aqui se reutiliza en la suite del cuidador.
//
// El listado de usuarios solo ofrece "Editar" por fila: el cambio de estado se
// hace con PATCH /usuarios/{id}/estado (no hay boton en la UI) y la suite
// verifica el resultado en el listado y por API.
import { test, expect } from '@playwright/test'
import { credencialesAdmin } from '../soporte/entorno'
import { clienteAdmin } from '../soporte/api'
import { DATOS, ESTADO, persistir } from '../soporte/datos'
import { entrarComo } from '../soporte/usuarios'
import { PaginaFormularioUsuario, PaginaListadoUsuarios } from '../paginas/PaginaUsuarios'

interface UsuarioApi {
  id: number
  correo: string
  activo: boolean
}

test.describe.serial('Usuarios', () => {
  test('CP-09 el listado de usuarios muestra el correo y el rol', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const listado = new PaginaListadoUsuarios(page)
    await listado.abrir()

    // La cuenta del administrador aparece (es la sesion actual). Se filtra por
    // rol porque la tabla pagina y hay muchas cuentas de ejecuciones previas.
    await listado.filtrarPorRol('Administrador')
    const fila = await listado.esperarFila(admin.correo)
    await expect(fila).toContainText('Administrador')
    await expect(fila).toContainText('Activo')
  })

  test('CP-10 valida los campos obligatorios en el cliente', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const formulario = new PaginaFormularioUsuario(page)
    await formulario.abrirNuevo()
    await formulario.guardar()

    await expect(formulario.errorDe('nombre')).toContainText('El nombre es obligatorio.')
    await expect(formulario.errorDe('correo')).toContainText('El correo es obligatorio.')
    await expect(formulario.errorDe('contrasena')).toContainText(/La contrase.a es obligatoria\./)
    await expect(page).toHaveURL(/\/usuarios\/nuevo$/)
  })

  test('CP-11 crea un cuidador y queda activo en el listado', async ({ page }) => {
    const admin = credencialesAdmin()

    // Idempotencia: si el correo ya quedo ocupado por un intento previo, no se
    // intenta crearlo otra vez (el correo es unico y no se puede editar).
    if (ESTADO.correoCuidadorEnUso && ESTADO.cuidadorId) {
      test.info().annotations.push({ type: 'nota', description: 'cuidador ya creado' })
      return
    }

    await entrarComo(page, admin.correo, admin.contrasena)
    const listado = new PaginaListadoUsuarios(page)
    await listado.abrir()

    const formulario = new PaginaFormularioUsuario(page)
    await formulario.abrirNuevo()
    await formulario.completarCampos({
      nombreCompleto: DATOS.nombreCuidador,
      correo: DATOS.correoCuidador,
      contrasena: DATOS.contrasenaCuidador,
      rol: 'Cuidador enfermero',
      telefono: '3001234567',
    })
    await formulario.guardar()
    await formulario.esperarListado()

    const fila = await listado.esperarFila(DATOS.correoCuidador)
    await expect(fila).toContainText(DATOS.nombreCuidador)
    await expect(fila).toContainText('Cuidador enfermero')
    await expect(fila).toContainText('Activo')

    const api = await clienteAdmin(admin)
    try {
      const pagina = await api.obtener<{ contenido: UsuarioApi[] }>('/usuarios?tamano=100')
      const creado = pagina.contenido.find((u) => u.correo === DATOS.correoCuidador)
      expect(creado, 'el cuidador debe existir en el backend').toBeTruthy()
      ESTADO.cuidadorId = creado?.id
      ESTADO.cuidadorCorreo = DATOS.correoCuidador
      ESTADO.cuidadorContrasena = DATOS.contrasenaCuidador
      ESTADO.correoCuidadorEnUso = true
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-12 rechaza un correo duplicado (409 sobre el campo)', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const formulario = new PaginaFormularioUsuario(page)
    await formulario.abrirNuevo()
    await formulario.completarCampos({
      nombreCompleto: `${DATOS.nombreCuidador} Dup`,
      correo: DATOS.correoCuidador,
      contrasena: DATOS.contrasenaFamiliar,
      rol: 'Familiar autorizado',
    })
    await formulario.guardar()

    await expect(formulario.errorDe('correo')).toContainText(/ya esta registrado|en uso|existe/i)
    await expect(page).toHaveURL(/\/usuarios\/nuevo$/)
  })

  test('CP-13 desactiva y vuelve a activar al cuidador', async ({ page }) => {
    const admin = credencialesAdmin()
    const cuidadorId = ESTADO.cuidadorId
    expect(cuidadorId, 'CP-11 debe haber creado el cuidador').toBeTruthy()

    // Desactivacion por API (el listado no ofrece el boton de estado).
    const api = await clienteAdmin(admin)
    try {
      await api.patch(`/usuarios/${cuidadorId}/estado`, { activo: false })
      const desactivado = await api.obtener<UsuarioApi>(`/usuarios/${cuidadorId}`)
      expect(desactivado.activo, 'el usuario debe quedar inactivo').toBe(false)
    } finally {
      await api.cerrar()
    }

    // En el listado, con el filtro "Inactivos", figura como desactivado.
    await entrarComo(page, admin.correo, admin.contrasena)
    const listado = new PaginaListadoUsuarios(page)
    await listado.abrir()
    await listado.filtrarInactivos()
    const filaInactiva = await listado.esperarFila(DATOS.correoCuidador)
    await expect(filaInactiva).toContainText('Inactivo')

    // Se reactiva por API para que pueda iniciar sesion en la suite del
    // cuidador y el listado de activos lo vuelve a mostrar.
    const apiReactivacion = await clienteAdmin(admin)
    try {
      await apiReactivacion.patch(`/usuarios/${cuidadorId}/estado`, { activo: true })
      const activado = await apiReactivacion.obtener<UsuarioApi>(`/usuarios/${cuidadorId}`)
      expect(activado.activo, 'el usuario debe quedar activo').toBe(true)
    } finally {
      await apiReactivacion.cerrar()
    }

    await listado.filtrarActivos()
    const filaActiva = await listado.esperarFila(DATOS.correoCuidador)
    await expect(filaActiva).toContainText('Activo')
    await expect(listado.fila(DATOS.correoCuidador)).toHaveCount(1)
    await expect(listado.fila(DATOS.correoCuidador)).toContainText('Activo')
  })
})
