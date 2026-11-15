# E2E - MediTrack Senior

Pruebas end-to-end del flujo principal de MediTrack Senior con **Playwright**
(solo Chromium). Recorren la aplicacion real contra el **backend** y la **base
de datos** ya levantados: no simulan `fetch`, sino que manejan el navegador como
lo haria un usuario y comprueban lo que se ve en pantalla y lo que queda
persistido por la API.

La suite es **secuencial**: los casos comparten estado de negocio dentro de una
misma ejecucion (un solo worker) y cada caso depende del anterior, igual que un
guion de demostracion.

## Requisitos

- Node.js 20 o superior (probado con Node 24).
- Backend en `http://localhost:8080` y frontend en `http://localhost:5173`
  (ver los README de `backend/` y `frontend/`).
- Base de datos PostgreSQL levantada y migrada (backend de desarrollo).

## Instalacion

```bash
npm ci
```

Los navegadores de Playwright se instalan una vez:

```bash
npx playwright install chromium
```

## Configuracion

Copia el archivo de ejemplo y completa las credenciales del administrador:

```bash
cp .env.example .env
```

Variables disponibles (se leen de `process.env`; si existe `e2e/.env` se carga
con `process.loadEnvFile`):

| Variable             | Descripcion                                                      | Valor por defecto           |
| -------------------- | ---------------------------------------------------------------- | --------------------------- |
| `E2E_ADMIN_EMAIL`    | Correo del administrador (obligatorio)                           | -                           |
| `E2E_ADMIN_PASSWORD` | Contrasena del administrador (obligatorio)                       | -                           |
| `E2E_BASE_URL`       | URL base del frontend (Vite dev server)                          | `http://localhost:5173`     |
| `E2E_API_URL`        | URL base de la API REST (debe terminar en `/api`)                | `http://localhost:8080/api` |
| `E2E_SLOWMO`         | Pausa en milisegundos entre acciones (util para demos)           | `0`                         |

Las credenciales del administrador se validan al usarlas y **nunca se
imprimen**; los mensajes de error solo incluyen metodo, ruta y status. El `.env`
no se versiona; el `.env.example` si.

## Como correr

```bash
npm test
```

Para ver el navegador durante la ejecucion (por ejemplo, en una demostracion):

```bash
npm run test:demo
```

`test:demo` equivale a `playwright test --headed`; combinalo con
`E2E_SLOWMO=500` si quieres ver cada paso con pausas.

Ver el reporte HTML de la ultima ejecucion:

```bash
npm run report
```

## Estado compartido entre casos

Los casos de un mismo archivo comparten datos mediante `test-results/estado-compartido.json`
(sufijo de la ejecucion e ids creados). Todo lo que se crea lleva el prefijo
`E2E-` y un sufijo aleatorio para no chocar con datos reales ni con ejecuciones
anteriores. Los pasos de preparacion (por ejemplo, crear el residente o el
cuidador) se hacen en el **primer caso** del grupo y los demas reutilizan esos
ids.

Por eso un caso que depende de otro no se puede ejecutar aislado: `03-cuidador`
necesita que `CP-25` haya preparado el cuidador y `04-familiar` reutiliza el
residente creado por `CP-25`. La forma correcta de correrlos es archivo completo
o la suite entera.

## Estructura de carpetas

```
e2e/
├── playwright.config.ts       # Configuracion (Chromium, serie, video/trace)
├── tsconfig.json              # TypeScript (strict, sin emision)
├── .env.example               # Variables de entorno de ejemplo
├── soporte/                   # Infraestructura compartida (no son pruebas)
│   ├── entorno.ts             # Lectura de variables y URLs del entorno
│   ├── api.ts                 # Cliente HTTP con token (+ login por API)
│   ├── datos.ts               # Datos unicos y estado compartido entre pasos
│   └── usuarios.ts            # Crear residentes/usuarios y sembrar sesion
├── paginas/                   # Page Objects (una clase por pantalla)
│   └── Pagina*.ts             # Login, Residentes, Usuarios, Medicamentos, ...
└── pruebas/                   # Casos, agrupados por modulo del negocio
    ├── 00-residentes.spec.ts
    ├── 01-usuarios.spec.ts
    ├── 02-medicamentos.spec.ts
    ├── 03-cuidador.spec.ts
    └── 04-familiar.spec.ts
```

- **`soporte/`**: prepara datos por API (lo que no interesa demostrar por UI) y
  deja la sesion del rol ya sembrada en `sessionStorage`. Asi cada caso se
  concentra en lo que si es visible para ese rol.
- **`paginas/`**: Page Objects con selectores accesibles (`getByRole`,
  `getByLabel`) y esperas explicitas; evitan selectores fragiles por CSS.
- **`pruebas/`**: los casos numerados `CP-01`..`CP-32`, agrupados con
  `test.describe.serial` por modulo.

## Cobertura

| Archivo                    | Casos  | Modulo                                         |
| -------------------------- | ------ | ---------------------------------------------- |
| `00-residentes.spec.ts`    | CP-01..08 | Residentes (alta, duplicado, filtro, estado) |
| `01-usuarios.spec.ts`      | CP-09..13 | Gestion de usuarios                          |
| `02-medicamentos.spec.ts`  | CP-14..22 | Medicamentos, esquemas y tomas               |
| `03-cuidador.spec.ts`      | CP-23..29 | Sesion y vista del cuidador (incluye signos) |
| `04-familiar.spec.ts`      | CP-30..32 | Familiar autorizado (solo lectura)           |

### Residentes (`00-residentes`)

- `CP-01` crea un residente desde el formulario.
- `CP-02` rechaza un documento duplicado (409 sobre el campo).
- `CP-03` valida campos obligatorios en el cliente.
- `CP-04` filtra el listado por documento y por nombre.
- `CP-05` desactiva un residente con motivo (borrado logico).
- `CP-06` reactiva al residente.
- `CP-07` el detalle muestra el panel de esquemas de medicacion.
- `CP-08` abre el formulario de nuevo esquema desde el residente.

### Usuarios (`01-usuarios`)

- `CP-09` el listado de usuarios muestra el correo y el rol.
- `CP-10` valida los campos obligatorios en el cliente.
- `CP-11` crea un cuidador y queda activo en el listado.
- `CP-12` rechaza un correo duplicado (409 sobre el campo).
- `CP-13` desactiva y vuelve a activar al cuidador.

### Medicamentos, esquemas y tomas (`02-medicamentos`)

- `CP-14` crea un medicamento en el catalogo.
- `CP-15` rechaza un medicamento con nombre duplicado.
- `CP-16` filtra el catalogo por nombre.
- `CP-17` crea un esquema de medicacion para el residente.
- `CP-18` la agenda del dia muestra las tomas programadas.
- `CP-19` registra la toma de las 08:00 como administrada.
- `CP-20` la toma omitida exige y guarda el motivo.
- `CP-21` el historial conserva las tomas registradas.
- `CP-22` suspende el esquema y deja el medicamento sin agenda activa.

### Sesion y cuidador (`03-cuidador`)

- `CP-23` un administrador inicia sesion y llega al panel.
- `CP-24` rechaza credenciales invalidas.
- `CP-25` prepara al cuidador con su residente asignado.
- `CP-26` el cuidador inicia sesion y ve su vista de trabajo.
- `CP-27` el cuidador no tiene acceso a la administracion de usuarios.
- `CP-28` el cuidador registra signos vitales del residente.
- `CP-29` el cuidador registra una toma administrada.

### Familiar autorizado (`04-familiar`)

- `CP-30` prepara al familiar autorizado del residente.
- `CP-31` el familiar ve al residente autorizado (solo lectura).
- `CP-32` el familiar consulta los esquemas sin poder registrarlos.

## Artefactos

Cada ejecucion escribe evidencia lista para revisar o presentar, en `test-results/`
y `playwright-report/` (ambos ignorados por Git):

- **video**: grabado en todas las pruebas (`video: 'on'`).
- **screenshot**: capturado en todas las pruebas, con la imagen del fallo al
  final si algo sale mal.
- **trace**: se conserva solo cuando una prueba falla (`retain-on-failure`); se
  abre con `npx playwright show-trace <archivo.zip>`.
- **reporte HTML**: se abre con `npm run report`.

## Notas

- Solo Chromium y zona horaria `America/Bogota` con locale `es-CO`, para que
  fechas y textos coincidan con el producto.
- La configuracion usa `workers: 1`, `fullyParallel: false` y `retries: 0`: el
  flujo es un guion ordenado, no pruebas independientes.
- La preparacion de datos por API (residentes y usuarios para los roles) usa el
  token del administrador; el token y las contrasenas nunca se imprimen.
