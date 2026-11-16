// CP-01 a CP-08: modulo de residentes de extremo a extremo.
//
// Cubre el alta con validaciones, el filtro por documento y nombre, la
// desactivacion con motivo (borrado logico) y su reactivacion, y el acceso a
// los esquemas de medicacion del residente desde su detalle.
//
// El alta se hace una sola vez: si CP-01 se reintenta en un run nuevo, el
// residente del intento anterior se desactiva por API para liberar el
// documento y el flujo es repetible. La desactivacion de CP-05 se revierte en
// CP-06, que deja el residente activo para CP-07 y CP-08.
import { test, expect } from '@playwright/test'
import { credencialesAdmin } from '../soporte/entorno'
import { clienteAdmin } from '../soporte/api'
import { DATOS, ESTADO, persistir } from '../soporte/datos'
import { entrarComo } from '../soporte/usuarios'
import {
  PaginaDetalleResidente,
  PaginaFormularioResidente,
  PaginaListadoResidentes,
} from '../paginas/PaginaResidentes'

interface PacienteListado {
  id: number
  nombreCompleto: string
  activo: boolean
  habitacion: string | null
}

interface EstadoPaciente {
  activo: boolean
}

test.describe.serial('Residentes', () => {
  test('CP-01 crea un residente desde el formulario', async ({ page }) => {
    const admin = credencialesAdmin()

    // Idempotencia: si el residente ya existe (reintento del caso), se
    // desactiva por API para liberar el documento y repetir el alta.
    const api = await clienteAdmin(admin)
    try {
      const pagina = await api.obtener<{ contenido: PacienteListado[] }>(
        `/pacientes?texto=${encodeURIComponent(DATOS.documento)}&tamano=5`,
      )
      const previo = pagina.contenido.find((p) => p.nombreCompleto === DATOS.nombreResidente)
      if (previo?.activo) {
        await api.patch(`/pacientes/${previo.id}/estado`, {
          activo: false,
          motivo: `Repeticion E2E ${DATOS.sufijo}`,
        })
      }
      ESTADO.pacienteId = previo?.id
    } finally {
      await api.cerrar()
    }

    await entrarComo(page, admin.correo, admin.contrasena)
    const listado = new PaginaListadoResidentes(page)
    await listado.abrir()

    const formulario = new PaginaFormularioResidente(page)
    await formulario.abrirNuevo()
    await formulario.completarCampos({
      documento: DATOS.documento,
      nombres: DATOS.nombres,
      apellidos: DATOS.apellidos,
      fechaNacimiento: '1942-03-15',
      situacionClinica: 'Hipertension controlada y movilidad reducida',
      habitacion: 'A-101',
    })
    await formulario.guardar()
    await formulario.esperarListado()

    await listado.buscarPorTexto(DATOS.nombreResidente)
    await listado.esperarFila(DATOS.nombreResidente)

    // La tabla muestra la habitacion capturada y el residente queda activo.
    const fila = await listado.esperarFila(DATOS.nombreResidente)
    await expect(fila).toContainText('A-101')
    await expect(fila).toContainText('Activo')

    // El id se resuelve por API para las verificaciones del resto del flujo.
    const apiVerificacion = await clienteAdmin(admin)
    try {
      const pagina = await apiVerificacion.obtener<{ contenido: PacienteListado[] }>(
        `/pacientes?texto=${encodeURIComponent(DATOS.documento)}&tamano=5`,
      )
      const encontrado = pagina.contenido.find((p) => p.nombreCompleto === DATOS.nombreResidente)
      expect(encontrado, 'el residente recien creado debe estar en el listado').toBeTruthy()
      ESTADO.pacienteId = encontrado?.id
      ESTADO.residenteActivo = encontrado?.activo ?? true
      persistir()
    } finally {
      await apiVerificacion.cerrar()
    }
  })

  test('CP-02 rechaza un documento duplicado (409 sobre el campo)', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const formulario = new PaginaFormularioResidente(page)
    await formulario.abrirNuevo()
    await formulario.completarCampos({
      documento: DATOS.documento,
      nombres: DATOS.nombres,
      apellidos: `Duplicado ${DATOS.sufijo}`,
      fechaNacimiento: '1945-07-20',
      situacionClinica: 'Sin antecedentes relevantes',
    })
    await formulario.guardar()

    await expect(formulario.errorDe('documento')).toContainText(/ya existe|duplicad|registrad/i)
    // No sale del formulario: no se crea un segundo residente.
    await expect(page).toHaveURL(/\/residentes\/nuevo$/)
  })

  test('CP-03 valida campos obligatorios en el cliente', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const formulario = new PaginaFormularioResidente(page)
    await formulario.abrirNuevo()
    await formulario.guardar()

    await expect(formulario.errorDe('documento')).toContainText('El documento es obligatorio.')
    await expect(formulario.errorDe('nombres')).toContainText('Los nombres son obligatorios.')
    await expect(formulario.errorDe('situacion-clinica')).toContainText(
      'La situacion clinica es obligatoria',
    )
    // No sale del formulario.
    await expect(page).toHaveURL(/\/residentes\/nuevo$/)
  })

  test('CP-04 filtra el listado por documento y por nombre', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const listado = new PaginaListadoResidentes(page)
    await listado.abrir()

    // El documento del residente creado lo deja visible en el resultado.
    await listado.buscarPorTexto(DATOS.documento)
    await expect(await listado.esperarFila(DATOS.nombreResidente)).toContainText('A-101')
    await expect(await listado.esperarFila(DATOS.nombreResidente)).toContainText('Activo')


    // Un texto que no coincide con ningun residente no devuelve filas.
    await listado.buscarPorTexto('zz-sin-coincidencias-e2e')
    await listado.esperarFilas(0)
  })

  test('CP-05 desactiva un residente con motivo (borrado logico)', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    const listado = new PaginaListadoResidentes(page)
    await listado.abrir()
    await listado.buscarPorTexto(DATOS.documento)
    await listado.abrirDetalle(DATOS.nombreResidente)

    const detalle = new PaginaDetalleResidente(page)
    await detalle.esperarCargado(DATOS.nombreResidente)
    await detalle.confirmarDesactivacion(DATOS.motivoDesactivacion)

    await expect(detalle.estado()).toHaveText(/Inactivo/)
    await expect(detalle.botonActivar()).toBeVisible()

    // El residente inactivo deja de aparecer en el filtro por defecto y sigue
    // visible cuando se cambia a "Todos" (borrado logico, no fisico).
    await listado.abrir()
    await listado.buscarPorTexto(DATOS.documento)
    await listado.esperarFilas(0)
    await listado.mostrarTodos()
    await listado.esperarFilas(1)
    await expect(await listado.esperarFila(DATOS.nombreResidente)).toContainText('Inactivo')

    // Verificacion por API: el registro sigue existiendo con activo = false.
    const api = await clienteAdmin(admin)
    try {
      const estado = await api.obtener<EstadoPaciente>(`/pacientes/${pacienteId}`)
      expect(estado.activo, 'el borrado debe ser logico').toBe(false)
      ESTADO.residenteActivo = false
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-06 reactiva al residente', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    const listado = new PaginaListadoResidentes(page)
    await listado.abrir()
    await listado.buscarPorTexto(DATOS.documento)

    const detalle = new PaginaDetalleResidente(page)
    // El residente esta inactivo: se muestra con el filtro "Todos".
    await listado.mostrarTodos()
    await listado.abrirDetalle(DATOS.nombreResidente)
    await detalle.esperarCargado(DATOS.nombreResidente)
    await expect(detalle.estado()).toHaveText(/Inactivo/)

    await detalle.reactivar()
    await expect(detalle.estado()).toHaveText(/Activo/)
    await expect(detalle.botonDesactivar()).toBeVisible()

    const api = await clienteAdmin(admin)
    try {
      const estado = await api.obtener<EstadoPaciente>(`/pacientes/${pacienteId}`)
      expect(estado.activo).toBe(true)
      ESTADO.residenteActivo = true
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-07 el detalle muestra el panel de esquemas de medicacion', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    await page.goto(`/residentes/${pacienteId}`)

    const detalle = new PaginaDetalleResidente(page)
    await detalle.esperarCargado(DATOS.nombreResidente)

    // El detalle incluye el bloque de medicacion con la accion de alta.
    const esquemas = detalle.panelEsquemas()
    await expect(esquemas).toBeVisible()
    await expect(esquemas.getByRole('heading', { name: 'Esquemas de medicacion' })).toBeVisible()
    await expect(esquemas.getByText('Sin esquemas de medicacion registrados.')).toBeVisible()
    await expect(esquemas.getByRole('button', { name: /Nuevo esquema/ })).toBeVisible()
  })

  test('CP-08 abre el formulario de nuevo esquema desde el residente', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    await page.goto(`/residentes/${pacienteId}`)

    const detalle = new PaginaDetalleResidente(page)
    await detalle.esperarCargado(DATOS.nombreResidente)

    await detalle.panelEsquemas().getByRole('button', { name: /Nuevo esquema/ }).click()
    await expect(page).toHaveURL(new RegExp(`/residentes/${pacienteId}/esquemas/nuevo$`))
    await expect(
      page.getByRole('heading', { name: 'Nuevo esquema de medicacion' }),
    ).toBeVisible()
  })
})
