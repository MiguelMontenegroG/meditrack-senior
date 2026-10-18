// Formulario de alta y edicion de medicamentos del catalogo (solo
// ADMINISTRADOR). Valida en cliente; el 409 (nombre duplicado) se muestra
// sobre el campo nombre. La edicion recibe el medicamento por el estado de
// navegacion (el contrato no expone GET /medicamentos/{id}).

import { useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { BotonAccion } from '@/components/ui/BotonAccion'
import { ErrorApi } from '@/lib/api-client'
import { mensajeDeErrorContexto, mensajeDeFallo } from '@/lib/mensajes-error'
import { BotonEstadoMedicamento } from '@/features/medicacion/BotonEstadoMedicamento'
import {
  actualizarMedicamento,
  crearMedicamento,
} from '@/features/medicacion/services/medicamentoService'
import type { Medicamento } from '@/features/medicacion/types'

const LARGO_NOMBRE = 150
const LARGO_PRESENTACION = 100

/** Localiza el medicamento a editar en el estado de navegacion. */
interface EstadoNavegacion {
  medicamento?: Medicamento
}

/** Pantalla de crear/editar un medicamento del catalogo. */
export function FormularioMedicamento() {
  const { id } = useParams()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const esEdicion = id !== undefined
  const medicamentoInicial = (ubicacion.state as EstadoNavegacion | null)?.medicamento

  const [nombre, setNombre] = useState(medicamentoInicial?.nombre ?? '')
  const [presentacion, setPresentacion] = useState(medicamentoInicial?.presentacion ?? '')
  const [activo, setActivo] = useState(medicamentoInicial?.activo ?? true)
  const [errores, setErrores] = useState<{ nombre?: string; presentacion?: string }>({})
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const validar = () => {
    const nuevos: { nombre?: string; presentacion?: string } = {}
    if (!nombre.trim()) {
      nuevos.nombre = 'El nombre es obligatorio.'
    } else if (nombre.trim().length > LARGO_NOMBRE) {
      nuevos.nombre = `El nombre no puede superar ${LARGO_NOMBRE} caracteres.`
    }
    if (presentacion.length > LARGO_PRESENTACION) {
      nuevos.presentacion = `La presentacion no puede superar ${LARGO_PRESENTACION} caracteres.`
    }
    return nuevos
  }

  const guardar = async (evento: React.FormEvent) => {
    evento.preventDefault()
    if (enviando) return
    const nuevos = validar()
    setErrores(nuevos)
    setErrorGeneral(null)
    if (Object.keys(nuevos).length > 0) return
    setEnviando(true)
    try {
      const datos = { nombre: nombre.trim(), presentacion: presentacion.trim() || null }
      if (esEdicion) {
        await actualizarMedicamento(Number(id), datos)
      } else {
        await crearMedicamento(datos)
      }
      navegar('/medicamentos')
    } catch (fallo) {
      if (fallo instanceof ErrorApi && fallo.codigo === 'CONFLICTO') {
        setErrores({ nombre: mensajeDeErrorContexto('CONFLICTO', 'medicamento-duplicado') })
      } else {
        setErrorGeneral(mensajeDeFallo(fallo))
      }
    } finally {
      setEnviando(false)
    }
  }

  if (esEdicion && !medicamentoInicial) {
    return (
      <div className="page-content">
        <div className="acciones-detalle">
          <BotonAccion variant="ghost" onClick={() => navegar('/medicamentos')}>
            <ArrowLeft size={16} /> Volver
          </BotonAccion>
        </div>
        <p className="form-error" role="alert">
          No pudimos abrir el medicamento. Vuelve al catalogo y selecciona de nuevo.
        </p>
      </div>
    )
  }

  return (
    <div className="page-content">
      <div className="acciones-detalle">
        <BotonAccion variant="ghost" onClick={() => navegar('/medicamentos')}>
          <ArrowLeft size={16} /> Volver
        </BotonAccion>
      </div>
      <form className="formulario-residente" onSubmit={guardar} noValidate>
        <h2>{esEdicion ? 'Editar medicamento' : 'Nuevo medicamento'}</h2>

        <label>
          Nombre
          <input
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            maxLength={LARGO_NOMBRE}
            aria-invalid={Boolean(errores.nombre)}
            aria-describedby={errores.nombre ? 'error-nombre-med' : undefined}
          />
          {errores.nombre && (
            <span className="campo-error" id="error-nombre-med" role="alert">
              {errores.nombre}
            </span>
          )}
        </label>

        <label>
          Presentacion (opcional)
          <input
            value={presentacion}
            onChange={(e) => setPresentacion(e.target.value)}
            maxLength={LARGO_PRESENTACION}
            aria-invalid={Boolean(errores.presentacion)}
            aria-describedby={errores.presentacion ? 'error-pres-med' : undefined}
          />
          {errores.presentacion && (
            <span className="campo-error" id="error-pres-med" role="alert">
              {errores.presentacion}
            </span>
          )}
        </label>

        {errorGeneral && (
          <p className="form-error" role="alert">
            {errorGeneral}
          </p>
        )}

        <div className="form-acciones">
          <BotonAccion type="submit" disabled={enviando}>
            {enviando ? 'Guardando...' : 'Guardar medicamento'}
          </BotonAccion>
          <BotonAccion variant="outline" onClick={() => navegar('/medicamentos')} disabled={enviando}>
            Cancelar
          </BotonAccion>
          {esEdicion && (
            <BotonEstadoMedicamento
              medicamentoId={Number(id)}
              activo={activo}
              alCambiar={async () => setActivo((v) => !v)}
            />
          )}
        </div>
      </form>
    </div>
  )
}