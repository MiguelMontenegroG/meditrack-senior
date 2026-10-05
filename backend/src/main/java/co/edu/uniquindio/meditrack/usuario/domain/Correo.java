package co.edu.uniquindio.meditrack.usuario.domain;

import co.edu.uniquindio.meditrack.shared.domain.ConflictoDeNegocioException;

import java.util.Objects;

/**
 * Correo electronico como value object del dominio.
 *
 * <p>Normaliza (recorta y pasa a minusculas) para garantizar que el correo sea
 * unico sin distinguir mayusculas, tal como lo exige el indice
 * {@code uq_usuario_correo_lower} de la tabla {@code usuario}.</p>
 */
public final class Correo {

    private final String valor;

    private Correo(String valor) {
        this.valor = valor;
    }

    public static Correo de(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new ConflictoDeNegocioException("El correo es obligatorio");
        }
        String normalizado = correo.trim().toLowerCase();
        if (!normalizado.contains("@") || normalizado.startsWith("@") || normalizado.endsWith("@")) {
            throw new ConflictoDeNegocioException("El correo no tiene un formato valido");
        }
        return new Correo(normalizado);
    }

    public String valor() {
        return valor;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Correo correo)) {
            return false;
        }
        return valor.equals(correo.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
