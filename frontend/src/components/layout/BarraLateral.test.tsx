// Pruebas de la barra lateral con datos reales del usuario autenticado: nombre,
// rol e iniciales en la tarjeta inferior, sin nombres ni roles fijos.

import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { BarraLateral } from '@/components/layout/BarraLateral'

let usuarioActual: { id: number; nombreCompleto: string; rol: string } | null = null

vi.mock('@/features/auth/useAutenticacion', () => ({
  useAutenticacion: () => ({ usuario: usuarioActual }),
}))

function renderBarra() {
  return render(
    <MemoryRouter>
      <BarraLateral />
    </MemoryRouter>,
  )
}

describe('BarraLateral', () => {
  it('muestra el nombre y el rol del usuario autenticado', () => {
    usuarioActual = { id: 1, nombreCompleto: 'Laura Rojas Pineda', rol: 'ADMINISTRADOR' }
    renderBarra()
    expect(screen.getByText('Laura Rojas Pineda')).toBeInTheDocument()
    expect(screen.getByText('Administrador')).toBeInTheDocument()
    expect(screen.queryByText('Mariana Lopez')).not.toBeInTheDocument()
  })

  it('calcula las iniciales a partir del nombre completo', () => {
    usuarioActual = { id: 2, nombreCompleto: 'Carlos Mejia', rol: 'CUIDADOR_ENFERMERO' }
    renderBarra()
    expect(screen.getByText('CM')).toBeInTheDocument()
    expect(screen.getByText('Cuidador enfermero')).toBeInTheDocument()
  })

  it('no ofrece el panel de administracion a un familiar', () => {
    usuarioActual = { id: 3, nombreCompleto: 'Ana Ruiz', rol: 'FAMILIAR_AUTORIZADO' }
    renderBarra()
    expect(screen.queryByText('Panel')).not.toBeInTheDocument()
    expect(screen.queryByText('Usuarios')).not.toBeInTheDocument()
    expect(screen.getByText('Residentes')).toBeInTheDocument()
  })

  it('sin sesion muestra un estado honesto en lugar de un nombre inventado', () => {
    usuarioActual = null
    renderBarra()
    expect(screen.getByText('Sin sesion')).toBeInTheDocument()
    expect(screen.getAllByText('--').length).toBeGreaterThan(0)
  })
})
