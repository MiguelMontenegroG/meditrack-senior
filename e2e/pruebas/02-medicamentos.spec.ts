// CP-14 a CP-21: catalogo de medicamentos, esquemas y agenda de tomas.
//
// Cubre el alta del medicamento (y el 409 por nombre duplicado), el esquema de
// medicacion del residente, la agenda del dia con los estados calculados
// (Pendiente/Atrasada), el registro de tomas (Administrada/Omitida), el
// historial y la suspension del esquema.
//
// El horario de la toma se toma por la manana: con esa hora, el tamano de toma
// del dia ya es registrable y el registro de la toma es independiente de la
// hora en que se ejecute el flujo.
import { test, expect } from '@playwright/test'
import { credencialesAdmin } from '../soporte/entorno'
import { clienteAdmin, type ClienteApi, type Credenciales } from '../soporte/api'
import { DATOS, ESTADO, horaActualEnBogota, persistir } from '../soporte/datos'
import { entrarComo } from '../soporte/usuarios'
import {
  PaginaFormularioEsquema,
  PaginaMedicamentos,
  PaginaTomas,
  SeccionEsquemas,
} from '../paginas/PaginaMedicamentos'

interface MedicamentoApi {
  id: number
  nombre: string
}

/** Busca un medicamento por nombre recorriendo las paginas de /medicamentos. */
async function buscarMedicamentoPorNombre(
  api: ClienteApi,
  nombre: string,
): Promise<MedicamentoApi | undefined> {
  const tamano = 100
  for (let pagina = 0; pagina < 50; pagina++) {
    const lista = await api.obtener<
      MedicamentoApi[] | { contenido: MedicamentoApi[]; totalPaginas?: number }
    >(`/medicamentos?pagina=${pagina}&tamano=${tamano}`)
    const items = Array.isArray(lista) ? lista : lista.contenido
    const encontrado = items.find((m) => m.nombre === nombre)
    if (encontrado) return encontrado
    if (Array.isArray(lista) || items.length < tamano) return undefined
    if (lista.totalPaginas !== undefined && pagina + 1 >= lista.totalPaginas) return undefined
  }
  return undefined
}

interface EsquemaApi {
  id: number
  pacienteId: number
  medicamentoId: number
  activo: boolean
}

interface PacienteListado {
  id: number
  nombreCompleto: string
  activo: boolean
}

/**
 * Garantiza que el residente principal exista y devuelve su id.
 *
 * Cuando este archivo se ejecuta por separado (sin 00-residentes) el estado
 * compartido no trae pacienteId. En ese caso se reutiliza el residente si ya
 * existe (por documento) o se crea por API: el alta del residente no forma
 * parte de lo que demuestran los casos de medicamentos.
 */
async function asegurarResidentePrincipal(admin: Credenciales): Promise<number> {
  if (ESTADO.pacienteId) return ESTADO.pacienteId

  const api: ClienteApi = await clienteAdmin(admin)
  try {
    const pagina = await api.obtener<{ contenido: PacienteListado[] }>(
      `/pacientes?texto=${encodeURIComponent(DATOS.documento)}&tamano=5`,
    )
    const existente = pagina.contenido.find((p) => p.nombreCompleto === DATOS.nombreResidente)
    if (existente) {
      ESTADO.pacienteId = existente.id
    } else {
      const creado = await api.post<PacienteListado>('/pacientes', {
        documento: DATOS.documento,
        nombres: DATOS.nombres,
        apellidos: DATOS.apellidos,
        fechaNacimiento: '1942-03-15',
        sexo: 'FEMENINO',
        habitacion: 'A-101',
        situacionClinica: 'Hipertension controlada y movilidad reducida',
      })
      ESTADO.pacienteId = creado.id
    }
    persistir()
    return ESTADO.pacienteId
  } finally {
    await api.cerrar()
  }
}

// Horarios de la agenda del dia. Se usan horas ya pasadas del mismo dia para
// que ambas tomas sean registrables: el backend rechaza (400) registrar una
// toma de una hora futura (RF-TOM-04). Se separan 60 minutos y quedan antes
// de la hora actual.
function horariosPasados(): { administrada: string; omitida: string } {
  const [hh, mm] = horaActualEnBogota().split(':').map(Number)
  const ahora = hh * 60 + mm
  const aMinutos = (total: number) => {
    const acotado = Math.max(total, 0)
    const h = String(Math.floor(acotado / 60)).padStart(2, '0')
    const m = String(acotado % 60).padStart(2, '0')
    return `${h}:${m}`
  }
  // La omitida 30 min atras y la administrada 90 min atras (si hay margen).
  const omitida = aMinutos(ahora - 30)
  const administrada = aMinutos(ahora - 90)
  return { administrada, omitida }
}

const HORAS_HOY = horariosPasados()

test.describe.serial('Medicamentos, esquemas y tomas', () => {
  test('CP-14 crea un medicamento en el catalogo', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const catalogo = new PaginaMedicamentos(page)
    await catalogo.abrir()

    // Idempotencia: si el medicamento ya existe (intento previo), no se repite.
    await catalogo.buscarPorTexto(DATOS.nombreMedicamento)
    const yaExiste = await catalogo.fila(DATOS.nombreMedicamento).count()
    if (yaExiste === 0) {
      await catalogo.abrirNuevo()
      await catalogo.completarCampos({
        nombre: DATOS.nombreMedicamento,
        presentacion: DATOS.presentacionMedicamento,
      })
      await catalogo.guardar()
      await expect(page).toHaveURL(/\/medicamentos$/)
    }

    await catalogo.buscarPorTexto(DATOS.nombreMedicamento)
    const fila = await catalogo.esperarFila(DATOS.nombreMedicamento)
    await expect(fila).toContainText(DATOS.presentacionMedicamento)
    await expect(fila).toContainText('Activo')

    const api = await clienteAdmin(admin)
    try {
      const creado = await buscarMedicamentoPorNombre(api, DATOS.nombreMedicamento)
      expect(creado, 'el medicamento debe existir en el backend').toBeTruthy()
      ESTADO.medicamentoId = creado?.id
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-15 rechaza un medicamento con nombre duplicado', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const catalogo = new PaginaMedicamentos(page)
    await catalogo.abrirNuevo()
    await catalogo.completarCampos({
      nombre: DATOS.nombreMedicamento,
      presentacion: 'Presentacion duplicada',
    })
    await catalogo.guardar()

    await expect(catalogo.errorNombre()).toBeVisible()
    await expect(catalogo.errorNombre()).toContainText(/ya existe|registrad|duplicad/i)
    await expect(page).toHaveURL(/\/medicamentos\/nuevo$/)
  })

  test('CP-16 filtra el catalogo por nombre', async ({ page }) => {
    const admin = credencialesAdmin()
    await entrarComo(page, admin.correo, admin.contrasena)

    const catalogo = new PaginaMedicamentos(page)
    await catalogo.abrir()

    await catalogo.buscarPorTexto('zz-sin-medicamento-e2e')
    await catalogo.esperarFilas(0)

    await catalogo.buscarPorTexto(DATOS.nombreMedicamento)
    await catalogo.esperarFilas(1)
    await expect(await catalogo.esperarFila(DATOS.nombreMedicamento)).toContainText(
      DATOS.presentacionMedicamento,
    )
  })

  test('CP-17 crea un esquema de medicacion para el residente', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = await asegurarResidentePrincipal(admin)
    expect(pacienteId, 'el residente debe existir para el esquema').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)

    const formulario = new PaginaFormularioEsquema(page)
    await formulario.abrirNuevo(pacienteId)
    await formulario.elegirMedicamento(DATOS.nombreMedicamento)
    await formulario.completarDatos({
      dosis: '1 tableta',
      frecuenciaHoras: 12,
      horarios: [HORAS_HOY.administrada, HORAS_HOY.omitida],
      via: 'Oral',
    })
    await formulario.guardar()
    await formulario.esperarDetalle(pacienteId)

    // El esquema aparece en el detalle del residente con su medicamento.
    const esquemas = new SeccionEsquemas(page)
    const item = await esquemas.esperarItem(DATOS.nombreMedicamento)
    await expect(item).toContainText('1 tableta')

    const api = await clienteAdmin(admin)
    try {
      const lista = await api.obtener<EsquemaApi[]>(`/pacientes/${pacienteId}/esquemas`)
      const creado = lista.find((e) => e.medicamentoId === ESTADO.medicamentoId)
      expect(creado, 'el esquema debe existir en el backend').toBeTruthy()
      ESTADO.esquemaId = creado?.id
      persistir()
    } finally {
      await api.cerrar()
    }
  })

  test('CP-18 la agenda del dia muestra las tomas programadas', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId as number
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    const tomas = new PaginaTomas(page)
    await tomas.abrir(pacienteId)
    await tomas.esperarCargada(DATOS.nombreMedicamento)

    for (const hora of [HORAS_HOY.administrada, HORAS_HOY.omitida]) {
      const slot = tomas.slotDe(hora)
      await expect(slot).toBeVisible()
      await expect(slot).toContainText(/Pendiente|Atrasada/)
    }
  })

  test('CP-19 registra la toma de las 08:00 como administrada', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId as number
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    const tomas = new PaginaTomas(page)
    await tomas.abrir(pacienteId)
    await tomas.esperarCargada(DATOS.nombreMedicamento)

    await tomas.registrarAdministrada(HORAS_HOY.administrada)
    await expect(tomas.slotDe(HORAS_HOY.administrada)).toContainText('Administrada')
    ESTADO.ultimaTomaRegistrada = HORAS_HOY.administrada
    persistir()
  })

  test('CP-20 la toma omitida exige y guarda el motivo', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId as number
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    const tomas = new PaginaTomas(page)
    await tomas.abrir(pacienteId)
    await tomas.esperarCargada(DATOS.nombreMedicamento)

    // Sin motivo la toma no se registra: el formulario muestra el error.
    await tomas.botonRegistrarDe(HORAS_HOY.omitida).click()
    const formulario = tomas.formularioDe(HORAS_HOY.omitida)
    await formulario.getByRole('button', { name: /Omitida/ }).click()
    await formulario.getByRole('button', { name: /^Registrar toma$/ }).click()
    await expect((await tomas.errorDeOmitida(HORAS_HOY.omitida))).toBeVisible()
    await expect(formulario).toBeVisible()

    // Con motivo, la toma queda como omitida con su justificacion.
    await tomas.registrarOmitida(HORAS_HOY.omitida, 'Residente en ayunas por examen')
    await expect(tomas.slotDe(HORAS_HOY.omitida)).toContainText('Omitida')
    ESTADO.ultimaTomaRegistrada = HORAS_HOY.omitida
    persistir()
  })

  test('CP-21 el historial conserva las tomas registradas', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId as number
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    const tomas = new PaginaTomas(page)
    await tomas.abrir(pacienteId)
    await tomas.esperarCargada(DATOS.nombreMedicamento)
    await tomas.abrirHistorial()

    // El historial lista ambas tomas con su estado.
    await expect(tomas.filaHistorial(/Administrada/).first()).toBeVisible()
    await expect(tomas.filaHistorial(/Omitida/).first()).toBeVisible()
  })

  test('CP-22 suspende el esquema y deja el medicamento sin agenda activa', async ({ page }) => {
    const admin = credencialesAdmin()
    const pacienteId = ESTADO.pacienteId as number
    expect(pacienteId, 'CP-01 debe haber creado el residente').toBeTruthy()

    await entrarComo(page, admin.correo, admin.contrasena)
    await page.goto(`/residentes/${pacienteId}`)

    const esquemas = new SeccionEsquemas(page)
    const item = await esquemas.esperarItem(DATOS.nombreMedicamento)
    await esquemas.botonEstado(DATOS.nombreMedicamento).click()

    // La suspension pide confirmacion inmediata; que da el esquema suspendido.
    const dialogo = item.getByRole('alertdialog', { name: /Confirmar suspensi/ })
    await expect(dialogo).toBeVisible()
    await dialogo.getByRole('button', { name: /Suspender|Si, suspender/ }).click()

    const api = await clienteAdmin(admin)
    try {
      const lista = await api.obtener<EsquemaApi[]>(`/pacientes/${pacienteId}/esquemas`)
      const esquema = lista.find((e) => e.id === ESTADO.esquemaId)
      expect(esquema?.activo, 'el esquema debe quedar suspendido').toBe(false)
    } finally {
      await api.cerrar()
    }
  })
})
