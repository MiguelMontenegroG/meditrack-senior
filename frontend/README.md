# MediTrack Senior - Frontend

Frontend del sistema de gestion geriatrica MediTrack Senior. Aplicacion web
responsive (mobile-first) construida con Vite + React + TypeScript + Tailwind CSS v4.

Toda la informacion clinica proviene del backend Java Spring Boot por HTTP
(API REST bajo `/api` con autenticacion JWT). Este frontend no contiene logica
de negocio en servidor ni acceso directo a base de datos.

## Requisitos

- Node.js 20 o superior (probado con Node 24).
- npm (viene con Node).

## Instalacion

```bash
npm ci
```

Si `package-lock.json` no estuviera disponible, usa `npm install`.

## Configuracion

Copia el archivo de ejemplo y ajusta la URL de la API:

```bash
cp .env.example .env
```

Variables disponibles:

| Variable       | Descripcion                                                  | Valor por defecto           |
| -------------- | ------------------------------------------------------------ | --------------------------- |
| `VITE_API_URL` | URL base de la API REST del backend (debe terminar en `/api`) | `http://localhost:8080/api` |

Nunca hardcodees esta URL en el codigo: usala via `import.meta.env.VITE_API_URL`.

## Como correr en desarrollo

```bash
npm run dev
```

La aplicacion queda disponible en `http://localhost:5173`.

## Verificacion

```bash
npm run lint
npm run build
npm run test
npm run preview
```

El resultado de la build se genera en `dist/`. El archivo
`public/staticwebapp.config.json` configura el `navigationFallback` a
`index.html` para el despliegue en Azure Static Web Apps.

## Pruebas

Las pruebas usan **Vitest** con entorno **jsdom** y **React Testing Library**:

```bash
npm run test
```

- `src/lib/api-client.test.ts`: cliente HTTP (exito, Bearer, 401/400, error de
  red, callback de 401).
- `src/features/auth/AuthContext.test.tsx`: login correcto/fallido, restauracion
  de sesion con `/me` y cierre de sesion.
- `src/features/pacientes/services/pacienteService.test.ts`: query string del
  listado, detalle, alta, edicion y cambio de estado.
- `src/features/pacientes/ListadoResidentes.test.tsx`: carga, vacio, error con
  reintento y visibilidad de acciones segun permisos.
- `src/features/pacientes/DetalleResidente.test.tsx`: 404 "no encontrado",
  datos cargados y secciones segun el rol.

No requieren backend: `fetch` se simula con `vi.stubGlobal`.


Es decir, las tres líneas nuevas van entre `...cierre de sesion.` y la línea en blanco que hay antes de `No requieren backend`.

Y en `## Estructura de carpetas`, dentro del bloque de `features/`, reemplaza:

```
    │   ├── auth/              # Inicio de sesion, AuthContext y contrato de tipos
    │   ├── dashboard/         # Panel de administrador (KPI, graficos, mocks)
    │   ├── cuidador/          # Vista del cuidador enfermero
    │   └── pacientes/ medicacion/ citas/ bitacora/ reportes/ admin/  (por implementar)
```

por:

```
    │   ├── auth/              # Inicio de sesion, AuthContext y contrato de tipos
    │   ├── dashboard/         # Panel de administrador (KPI, graficos, mocks)
    │   ├── cuidador/          # Vista del cuidador enfermero
    │   ├── pacientes/         # Listado, detalle, formularios y gestion (residentes)
    │   └── medicacion/ citas/ bitacora/ reportes/ admin/  (por implementar)
```
## Autenticacion

- El login consume `POST /api/auth/login` y valida la sesion con
  `GET /api/auth/me`. El token se envia como `Authorization: Bearer`.
- La sesion se guarda en **`sessionStorage`** (se pierde al cerrar la pestana).
  No hay refresh tokens: al vencer el JWT se cierra sesion.
- Los mensajes de error se traducen en `src/lib/mensajes-error.ts`; nunca se
  muestran mensajes tecnicos del backend.
- Requiere `VITE_API_URL` configurada (ver la seccion Configuracion).

## Modulo de residentes

- **Listado** (`/residentes`): todos los roles autenticados. Busqueda por texto
  y paginacion; el filtro activo/inactivo y el boton "Nuevo residente" solo se
  muestran al ADMINISTRADOR.
- **Detalle** (`/residentes/:id`): datos, contactos de emergencia y (solo
  ADMINISTRADOR) gestion de familiares vinculados y cuidadores asignados.
- **Alta/edicion** (`/residentes/nuevo`, `/residentes/:id/editar`): solo
  ADMINISTRADOR.
- El backend aplica la seguridad real: fuera de alcance responde `404`
  (mostrado como "Residente no encontrado") y la escritura sin permiso `403`.
  La interfaz solo decide que controles mostrar.

## Estructura de carpetas

```
frontend/
├── index.html                 # Punto de entrada HTML
├── vite.config.ts             # Configuracion de Vite (alias @ -> src)
├── eslint.config.js           # Configuracion minima de ESLint (React + TS)
├── tsconfig.json              # Referencias a tsconfig.app / tsconfig.node
├── .env.example               # Variables de entorno de ejemplo
├── public/                    # Recursos estaticos y staticwebapp.config.json
└── src/
    ├── main.tsx               # Arranque de React y carga de estilos/fuentes
    ├── App.tsx                # Proveedores + RouterProvider
    ├── app/                   # Router (rutas reales) y providers
    ├── components/
    │   ├── ui/                # Primitivas genericas (boton, estado, aviso)
    │   └── layout/            # Barra lateral, encabezado, navegacion inferior, marca
    ├── features/              # Modulos por feature del negocio
    │   ├── auth/              # Inicio de sesion, AuthContext y contrato de tipos
    │   ├── dashboard/         # Panel de administrador (KPI, graficos, mocks)
    │   ├── cuidador/          # Vista del cuidador enfermero
    │   └── pacientes/ medicacion/ citas/ bitacora/ reportes/ admin/  (por implementar)
    ├── layouts/               # AuthLayout y MainLayout
    ├── lib/                   # Utilidades (cn)
    └── styles/                # Hojas de estilo globales (globals.css)
```

## Tecnologias

- Vite + React + TypeScript
- React Router
- Tailwind CSS v4 (via `@tailwindcss/vite`)
- Recharts (graficos del dashboard)
- lucide-react (iconografia)
- Atkinson Hyperlegible (fuente de accesibilidad para baja vision)