// Formulario en linea para registrar signos vitales de un residente. Todos los
// signos son opcionales por separado, pero se exige al menos uno. La fecha y
// hora se interpretan en hora de Bogota y se convierten a instante UTC; el
// autor lo pone el backend a partir del usuario autenticado. Los errores 400 se
// muestran con el mensaje contextual en espanol de lib/mensajes-error.

import { useState } from 'react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { aInstanteUtc, horaActualBogota, hoyBogota } from '@/lib/fechas-bogota'
import { registrarSignosVitales } from '@/features/bitacora/services/bitacoraService'
import {
  aEntero,
  aNumero,
  validarSignosVitales,
  type ErroresSignosVitales,
  type ValoresSignosVitales,
} from '@/features/bitacora/signosVitales'
import type { DatosSignosVitales } from '@/features/bitacora/types'

/**
 * Redondea la temperatura a un decimal (como la columna NUMERIC(4,1)). Tras la
 * validacion el numero siempre existe; el 0 es solo para el tipo.
 */
function redondearTemperatura(valor: number | null): number {
  return Math.round((valor ?? 0) * 10) / 10
}

interface PropsFormularioSignos {
  pacienteId: number
  /** Se llama tras un registro exitoso para refrescar el historial. */
  alRegistrar: () => void
  /** Se llama al cancelar; la pantalla decide si oculta el formulario. */
  alCancelar: () => void
}

/** Formulario de registro de signos vitales (ADMIN o CUIDADOR). */
export function FormularioSignosVitales({ pacienteId, alRegistrar, alCancelar }: PropsFormularioSignos) {
  const [valores, setValores] = useState<ValoresSignosVitales>(() => ({
    fecha: hoyBogota(),
    hora: horaActualBogota(),
    sistolica: '',
    diastolica: '',
    glucosa: '',
    temperatura: '',
    observaciones: '',
  }))
  const [errores, setErrores] = useState<ErroresSignosVitales>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const cambiar = (campo: keyof ValoresSignosVitales, valor: string) =>
    setValores((actual) => ({ ...actual, [campo]: valor }))

  const guardar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validarSignosVitales(valores)
    // La fecha no puede ser futura (regla del backend, 400). Una medicion de
    // ahora mismo si es valida: solo se rechaza un instante estrictamente mayor.
    const instante = valores.fecha && valores.hora ? aInstanteUtc(valores.fecha, valores.hora) : null
    if (valores.fecha && valores.hora && !instante) {
      nuevos.fecha = 'La fecha u hora no es valida.'
    } else if (instante && new Date(instante).getTime() > Date.now()) {
      nuevos.hora = 'La fecha y hora de la medicion no puede estar en el futuro.'
    }
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return
    if (!instante) return

    const datos: DatosSignosVitales = {
      fechaHora: instante,
      presionSistolica: valores.sistolica.trim() ? aEntero(valores.sistolica) : null,
      presionDiastolica: valores.diastolica.trim() ? aEntero(valores.diastolica) : null,
      glucosaMgDl: valores.glucosa.trim() ? aEntero(valores.glucosa) : null,
      temperaturaC: valores.temperatura.trim()
        ? redondearTemperatura(aNumero(valores.temperatura))
        : null,
      observaciones: valores.observaciones.trim() || null,
    }
    setEnviando(true)
    try {
      await registrarSignosVitales(pacienteId, datos)
      alRegistrar()
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'VALIDACION') {
        setErrorGeneral(mensajeDeErrorContexto('VALIDACION', 'bitacora-valores-incoherentes'))
      } else {
        setErrorGeneral(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="formulario-signos" onSubmit={guardar} noValidate>
      <h4>Registrar signos vitales</h4>
      <div className="formulario-signos-grid">
        <label>
          Fecha
          <input
            type="date"
            value={valores.fecha}
            max={hoyBogota()}
            onChange={(evento) => cambiar('fecha', evento.target.value)}
            aria-invalid={Boolean(errores.fecha)}
          />
          {errores.fecha && <span className="campo-error">{errores.fecha}</span>}
        </label>
        <label>
          Hora
          <input
            type="time"
            value={valores.hora}
            onChange={(evento) => cambiar('hora', evento.target.value)}
            aria-invalid={Boolean(errores.hora)}
          />
          {errores.hora && <span className="campo-error">{errores.hora}</span>}
        </label>
        <label>
          Presion sistolica (mmHg)
          <input
            inputMode="numeric"
            value={valores.sistolica}
            onChange={(evento) => cambiar('sistolica', evento.target.value)}
            aria-invalid={Boolean(errores.sistolica)}
            placeholder="120"
          />
          {errores.sistolica && <span className="campo-error">{errores.sistolica}</span>}
        </label>
        <label>
          Presion diastolica (mmHg)
          <input
            inputMode="numeric"
            value={valores.diastolica}
            onChange={(evento) => cambiar('diastolica', evento.target.value)}
            aria-invalid={Boolean(errores.diastolica)}
            placeholder="80"
          />
          {errores.diastolica && <span className="campo-error">{errores.diastolica}</span>}
        </label>
        <label>
          Glucosa (mg/dL)
          <input
            inputMode="numeric"
            value={valores.glucosa}
            onChange={(evento) => cambiar('glucosa', evento.target.value)}
            aria-invalid={Boolean(errores.glucosa)}
            placeholder="102"
          />
          {errores.glucosa && <span className="campo-error">{errores.glucosa}</span>}
        </label>
        <label>
          Temperatura (grados C)
          <input
            inputMode="decimal"
            value={valores.temperatura}
            onChange={(evento) => cambiar('temperatura', evento.target.value)}
            aria-invalid={Boolean(errores.temperatura)}
            placeholder="36,5"
          />
          {errores.temperatura && <span className="campo-error">{errores.temperatura}</span>}
        </label>
      </div>
      <label className="campo-ancho">
        Observaciones (opcional)
        <textarea
          value={valores.observaciones}
          onChange={(evento) => cambiar('observaciones', evento.target.value)}
          maxLength={500}
          rows={3}
          aria-invalid={Boolean(errores.observaciones)}
        />
        {errores.observaciones && <span className="campo-error">{errores.observaciones}</span>}
      </label>
      {errorGeneral && (
        <p className="form-error" role="alert">
          {errorGeneral}
        </p>
      )}
      <div className="form-acciones">
        <BotonAccion type="submit" disabled={enviando}>
          {enviando ? 'Registrando...' : 'Registrar signos'}
        </BotonAccion>
        <BotonAccion variant="outline" onClick={alCancelar} disabled={enviando}>
          Cancelar
        </BotonAccion>
      </div>
    </form>
  )
}
