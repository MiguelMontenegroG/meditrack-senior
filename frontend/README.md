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

| Variable         | Descripcion                                              | Valor por defecto             |
| ---------------- | -------------------------------------------------------- | ----------------------------- |
| `VITE_API_URL`   | URL base de la API REST del backend (debe terminar en `/api`) | `http://localhost:8080/api`   |

Nunca hardcodees esta URL en el codigo: usala via `import.meta.env.VITE_API_URL`.

## Como correr en desarrollo

```bash
npm run dev
```

La aplicacion queda disponible en `http://localhost:5173`.

## Como construir para produccion

```bash
npm run build
npm run preview
```

El resultado se genera en `dist/`. El archivo `public/staticwebapp.config.json`
configura el `navigationFallback` a `index.html` para el despliegue en
Azure Static Web Apps.

## Estructura de carpetas

```
frontend/
├── index.html                 # Punto de entrada HTML
├── vite.config.ts             # Configuracion de Vite (alias @ -> src)
├── tsconfig.json              # Referencias a tsconfig.app / tsconfig.node
├── .env.example               # Variables de entorno de ejemplo
├── public/                    # Recursos estaticos y staticwebapp.config.json
└── src/
    ├── main.tsx               # Arranque de React y carga de estilos/fuentes
    ├── App.tsx                # Ruteo (React Router)
    ├── ClinicalWorkspace.tsx   # Componente principal del workspace clinico
    ├── components/            # Componentes UI compartidos (shadcn/ui)
    ├── core/                  # Guards, interceptores, modelos y servicios base
    ├── environments/          # (reservado)
    ├── features/              # Modulos por feature del negocio
    ├── layouts/               # Layouts de la aplicacion
    ├── lib/                   # Utilidades (cn, etc.)
    ├── shared/                # Componentes, pipes y directivas compartidas
    └── styles/                # Hojas de estilo globales (globals.css)
```

## Tecnologias

- Vite + React + TypeScript
- React Router
- Tailwind CSS v4 (via `@tailwindcss/vite`)
- shadcn/ui sobre `@base-ui/react`
- Recharts (graficos del dashboard)
- lucide-react (iconografia)
- Atkinson Hyperlegible (fuente de accesibilidad para baja vision)
