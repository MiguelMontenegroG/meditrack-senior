// Tipos del contrato de usuarios. Espejo exacto de los DTO de
// docs/api/openapi.yaml (Usuario, CrearUsuarioRequest, ActualizarUsuarioRequest,
// CambiarEstadoRequest y PageUsuario).

/** Roles que puede tener un usuario del sistema. */
export type RolUsuario = 'ADMINISTRADOR' | 'CUIDADOR_ENFERMERO' | 'FAMILIAR_AUTORIZADO'

/** Usuario tal como lo devuelve el backend (sin datos sensibles). */
export interface Usuario {
  id: number
  nombreCompleto: string
  correo: string
  telefono: string | null
  rol: RolUsuario
  activo: boolean
  ultimoAccesoEn: string | null
  creadoEn: string | null
  actualizadoEn: string | null
}

/** Cuerpo para crear un usuario (POST /api/usuarios). */
export interface DatosCrearUsuario {
  nombreCompleto: string
  correo: string
  contrasena: string
  rol: RolUsuario
  telefono?: string | null
}

/** Cuerpo para actualizar un usuario (PUT /api/usuarios/{id}). */
export interface DatosActualizarUsuario {
  nombreCompleto: string
  telefono?: string | null
  rol: RolUsuario
}

/** Pagina del listado de usuarios devuelta por el backend. */
export interface PaginaUsuarios {
  contenido: Usuario[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
}

/** Filtros del listado de usuarios (se envian como query string). */
export interface FiltrosUsuarios {
  rol?: RolUsuario
  activo?: boolean
  pagina?: number
  tamano?: number
}
