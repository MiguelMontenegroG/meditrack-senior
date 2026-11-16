// Utilidad para descargar un archivo binario (Blob) en el navegador usando un
// enlace temporal de objeto (URL.createObjectURL). Se usa para la descarga de
// PDF de reportes, cuyo JWT viaja en cabecera (no se puede usar un enlace
// directo). El nombre del archivo se toma del Content-Disposition o, si no
// viene, se usa uno generico SIN datos personales.

/**
 * Dispara la descarga de un Blob con el nombre de archivo indicado y libera el
 * objeto URL temporal al terminar.
 */
export function descargarBlob(blob: Blob, nombreArchivo: string): void {
  const url = URL.createObjectURL(blob)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = nombreArchivo
  enlace.rel = 'noopener'
  document.body.appendChild(enlace)
  enlace.click()
  document.body.removeChild(enlace)
  // Libera la memoria del objeto URL en el siguiente ciclo.
  setTimeout(() => URL.revokeObjectURL(url), 0)
}
