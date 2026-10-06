import { useState, type FormEvent } from 'react'
import { ArrowUpRight } from 'lucide-react'
import { MarcaMediTrack } from '@/components/layout/MarcaMediTrack'
import { EstadoError } from '@/components/ui/EstadoError'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import { ErrorApi } from '@/lib/api-client'
import type { UsuarioAutenticado } from '@/features/auth/types'

// Pantalla de inicio de sesion real contra POST /api/auth/login.
// No revela si el correo existe: cualquier fallo muestra un mensaje generico.

interface PropsInicioSesion {
  /** Se invoca tras un login correcto; recibe el usuario para redirigir. */
  alIngresar: (usuario: UsuarioAutenticado) => void
}

/** Pantalla de inicio de sesion. */
export function InicioSesion({ alIngresar }: PropsInicioSesion) {
  const { iniciarSesion } = useAutenticacion()
  const [mostrarContrasena, setMostrarContrasena] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function alEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando) return
    const datos = new FormData(evento.currentTarget)
    const correo = String(datos.get('correo') ?? '').trim()
    const contrasena = String(datos.get('contrasena') ?? '')

    // Validacion de campos en el cliente antes de llamar al backend.
    if (!correo || !contrasena) {
      setError('Ingresa tu correo y tu contrasena.')
      return
    }

    setError(null)
    setEnviando(true)
    try {
      const usuario = await iniciarSesion({ correo, contrasena })
      // El backend ya valido las credenciales: se redirige segun el rol.
      alIngresar(usuario)
    } catch (fallo) {
      if (fallo instanceof ErrorApi) {
        setError(fallo.mensajeUsuario)
      } else {
        setError('Ocurrio un error inesperado. Intentalo de nuevo.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-form-side">
        <div className="login-form-wrap">
          <MarcaMediTrack />
          <div className="login-heading">
            <p className="eyebrow">Acceso seguro al centro</p>
            <h1>Bienvenido de nuevo</h1>
            <p>Ingresa para continuar con el cuidado de nuestros residentes.</p>
          </div>
          <form onSubmit={alEnviar} noValidate>
            <label>
              Correo electronico
              <input
                name="correo"
                type="email"
                autoComplete="email"
                inputMode="email"
                required
                aria-invalid={error ? true : undefined}
              />
            </label>
            <label>
              Contrasena
              <div className="password-field">
                <input
                  name="contrasena"
                  type={mostrarContrasena ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  aria-invalid={error ? true : undefined}
                />
                <button
                  type="button"
                  onClick={() => setMostrarContrasena(!mostrarContrasena)}
                  aria-label={mostrarContrasena ? 'Ocultar contrasena' : 'Mostrar contrasena'}
                >
                  {mostrarContrasena ? 'Ocultar' : 'Mostrar'}
                </button>
              </div>
            </label>

            {error && <EstadoError mensaje={error} />}

            <div className="form-meta">
              <label className="check-label">
                <input type="checkbox" /> <span>Recordar este dispositivo</span>
              </label>
              <button type="button" className="text-link">
                ¿Olvidaste tu contrasena?
              </button>
            </div>
            <button type="submit" className="btn btn-primary login-button" disabled={enviando}>
              {enviando ? 'Ingresando...' : 'Ingresar al sistema'}
              {!enviando && <ArrowUpRight size={17} />}
            </button>
          </form>
          <p className="privacy-note">
            Tratamos tus datos personales de acuerdo con la Ley 1581 de 2012 y nuestra politica de
            privacidad.
          </p>
        </div>
      </section>
      <AsideInicioSesion />
    </main>
  )
}

/** Panel lateral informativo del inicio de sesion. */
function AsideInicioSesion() {
  return (
    <section className="login-aside">
      <div>
        <MarcaMediTrack oscuro />
        <p className="aside-kicker">Cuidado que se puede ver</p>
        <h2>Seguimiento clinico claro para quienes cuidan.</h2>
        <p className="aside-copy">
          Una mirada compartida sobre cada residente, para que cada decision este respaldada por
          informacion.
        </p>
        <div className="value-list">
          <div>
            <span>01</span>
            <p>
              <strong>Informacion en un solo lugar</strong>
              <small>Perfiles, medicacion y bitacoras siempre actualizados.</small>
            </p>
          </div>
          <div>
            <span>02</span>
            <p>
              <strong>Alertas que orientan</strong>
              <small>El equipo sabe que requiere atencion y cuando.</small>
            </p>
          </div>
          <div>
            <span>03</span>
            <p>
              <strong>Comunicacion responsable</strong>
              <small>Familias informadas con reportes claros y seguros.</small>
            </p>
          </div>
        </div>
      </div>
      <span className="aside-footer">Centro de Vida Sol de los Andes Â· Armenia, Quindio</span>
    </section>
  )
}