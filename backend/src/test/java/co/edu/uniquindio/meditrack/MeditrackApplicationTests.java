package co.edu.uniquindio.meditrack;

import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.AsignacionCuidadorJpaRepository;
import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.ContactoEmergenciaJpaRepository;
import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.PacienteJpaRepository;
import co.edu.uniquindio.meditrack.paciente.infrastructure.persistence.VinculacionFamiliarJpaRepository;
import co.edu.uniquindio.meditrack.shared.infrastructure.persistence.AuditoriaJpaRepository;
import co.edu.uniquindio.meditrack.usuario.infrastructure.persistence.RolJpaRepository;
import co.edu.uniquindio.meditrack.usuario.infrastructure.persistence.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Prueba de contexto minima del backend.
 *
 * Verifica que el contexto de Spring arranca correctamente. Se excluyen las
 * autoconfiguraciones de base de datos (DataSource, JPA, gestor de transacciones
 * y Flyway) para que la prueba no dependa de un PostgreSQL en ejecucion. Es la
 * solucion mas simple sin agregar dependencias (no se usa H2 ni Testcontainers
 * en este paso).
 *
 * <p>Se proveen valores de prueba para JWT_SECRET (al menos 32 bytes),
 * JWT_EXPIRATION_MINUTES y CORS_ALLOWED_ORIGINS, que la configuracion de
 * seguridad y los adaptadores leen del entorno. Los repositorios de Spring Data
 * se sustituyen por mocks porque, al excluir JPA, no se autogeneran.</p>
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "JWT_SECRET=clave-de-prueba-con-mas-de-32-bytes-para-hs256!!",
        "JWT_EXPIRATION_MINUTES=60",
        "CORS_ALLOWED_ORIGINS=http://localhost:5173"
})
class MeditrackApplicationTests {

    @MockitoBean
    private UsuarioJpaRepository usuarioJpaRepository;

    @MockitoBean
    private RolJpaRepository rolJpaRepository;

    @MockitoBean
    private AuditoriaJpaRepository auditoriaJpaRepository;

    @MockitoBean
    private PacienteJpaRepository pacienteJpaRepository;

    @MockitoBean
    private ContactoEmergenciaJpaRepository contactoEmergenciaJpaRepository;

    @MockitoBean
    private VinculacionFamiliarJpaRepository vinculacionFamiliarJpaRepository;

    @MockitoBean
    private AsignacionCuidadorJpaRepository asignacionCuidadorJpaRepository;

    @Test
    void contextLoads() {
        // Si el contexto arranca sin excepciones, la prueba pasa.
    }
}