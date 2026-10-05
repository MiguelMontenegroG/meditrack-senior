import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowUpRight } from 'lucide-react'
import { MarcaMediTrack } from '@/components/layout/MarcaMediTrack'
import { useAutenticacion } from '@/features/auth/useAutenticacion'
import type { RolUsuario } from '@/features/auth/types'

// Rol elegido en la pantalla de inicio de sesion (solo Paso 2, mientras la
// autenticacion es simulada). Se elimina en el Paso 3 al conectar el backend.
const ROLES_PRUEBA: { valor: RolUsuario; etiqueta: string }[] = [
  { valor: 'ADMINISTRADOR', etiqueta: 'Administrador' },
  { valor: 'CUIDADOR_ENFERMERO', etiqueta: 'Cuidador' },
  { valor: 'FAMILIAR_AUTORIZADO', etiqueta: 'Familiar' },
]

/** Pantalla de inicio de sesion. Acepta cualquier credencial de prueba. */
export function InicioSesion() {
  const { iniciarSesion } = useAutenticacion()
  const navegar = useNavigate()
  const [mostrarContrasena, setMostrarContrasena] = useState(false)
  const [rol, setRol] = useState<RolUsuario>('ADMINISTRADOR')

  async function alEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    const datos = new FormData(evento.currentTarget)
    const correo = String(datos.get('correo') ?? '')
    const contrasena = String(datos.get('contrasena') ?? '')
    await iniciarSesion({ correo, contrasena }, rol)
    navegar(rol === 'CUIDADOR_ENFERMERO' ? '/cuidador' : rol === 'ADMINISTRADOR' ? '/dashboard' : '/familiar')
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
          <form onSubmit={alEnviar}>
            <label>
              Correo electronico
              <input name="correo" type="email" defaultValue="mariana.lopez@solandes.co" required />
            </label>
            <label>
              Contrasena
              <div className="password-field">
                <input
                  name="contrasena"
                  type={mostrarContrasena ? 'text' : 'password'}
                  defaultValue="meditrack2026"
                  required
                />
                <button type="button" onClick={() => setMostrarContrasena(!mostrarContrasena)}>
                  {mostrarContrasena ? 'Ocultar' : 'Mostrar'}
                </button>
              </div>
            </label>

            {/* Selector de rol SOLO para pruebas en el Paso 2. Se elimina en el Paso 3. */}
            <fieldset className="role-fieldset">
              <legend>Perfil de prueba</legend>
              <div className="role-options">
                {ROLES_PRUEBA.map((opcion) => (
                  <label key={opcion.valor} className="check-label">
                    <input
                      type="radio"
                      name="rolPrueba"
                      value={opcion.valor}
                      checked={rol === opcion.valor}
                      onChange={() => setRol(opcion.valor)}
                    />
                    <span>{opcion.etiqueta}</span>
                  </label>
                ))}
              </div>
            </fieldset>

            <div className="form-meta">
              <label className="check-label">
                <input type="checkbox" defaultChecked /> <span>Recordar este dispositivo</span>
              </label>
              <button type="button" className="text-link">
                ¿Olvidaste tu contrasena?
              </button>
            </div>
            <button type="submit" className="btn btn-primary login-button">
              Ingresar al sistema <ArrowUpRight size={17} />
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
      <span className="aside-footer">Centro de Vida Sol de los Andes · Armenia, Quindio</span>
    </section>
  )
}
