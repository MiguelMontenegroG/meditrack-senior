package co.edu.uniquindio.meditrack.paciente.domain;

/**
 * Sexo registrado del paciente. Los valores coinciden exactamente con la
 * restriccion CHECK {@code chk_paciente_sexo} de la tabla {@code paciente}
 * (migracion V2).
 */
public enum Sexo {
    MASCULINO,
    FEMENINO,
    OTRO,
    NO_ESPECIFICA
}
