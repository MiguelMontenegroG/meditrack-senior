package co.edu.uniquindio.meditrack.medicacion.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de entrada para crear o actualizar un medicamento del catalogo.
 */
public record MedicamentoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @Size(max = 100, message = "La presentacion no puede superar 100 caracteres")
        String presentacion) {
}
