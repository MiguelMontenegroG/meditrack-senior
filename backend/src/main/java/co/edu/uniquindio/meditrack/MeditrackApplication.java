package co.edu.uniquindio.meditrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del backend de MediTrack Senior.
 *
 * <p>Monolito modular organizado por modulos de negocio
 * (usuario, paciente, medicacion, cita, bitacora, alerta, reporte, dashboard).
 * Cada modulo sigue arquitectura hexagonal: domain / application / infrastructure.</p>
 */
@SpringBootApplication
public class MeditrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeditrackApplication.class, args);
    }
}
