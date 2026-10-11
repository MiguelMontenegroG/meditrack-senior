// Paginacion del listado. El total y las paginas vienen del backend; aqui
// solo se navega entre paginas ya calculadas.

interface PropsPaginacion {
  pagina: number
  totalPaginas: number
  total: number
  alCambiarPagina: (pagina: number) => void
}

/** Controles accesibles de paginacion (anterior/siguiente). */
export function Paginacion({ pagina, totalPaginas, total, alCambiarPagina }: PropsPaginacion) {
  const hayAnterior = pagina > 0
  const haySiguiente = pagina < totalPaginas - 1
  return (
    <nav className="paginacion" aria-label="Paginacion de residentes">
      <p className="muted">
        {total} {total === 1 ? 'residente' : 'residentes'} · página {pagina + 1} de{' '}
        {Math.max(totalPaginas, 1)}
      </p>
      <div className="paginacion-botones">
        <button
          type="button"
          className="btn btn-outline"
          disabled={!hayAnterior}
          onClick={() => alCambiarPagina(pagina - 1)}
        >
          Anterior
        </button>
        <button
          type="button"
          className="btn btn-outline"
          disabled={!haySiguiente}
          onClick={() => alCambiarPagina(pagina + 1)}
        >
          Siguiente
        </button>
      </div>
    </nav>
  )
}
