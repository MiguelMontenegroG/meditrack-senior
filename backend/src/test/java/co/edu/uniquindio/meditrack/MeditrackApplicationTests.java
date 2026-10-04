package co.edu.uniquindio.meditrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de contexto minima del backend.
 *
 * Verifica que el contexto de Spring arranca correctamente. Se excluyen las
 * autoconfiguraciones de base de datos (DataSource, JPA, gestor de transacciones
 * y Flyway) para que la prueba no dependa de un PostgreSQL en ejecucion. Es la
 * solucion mas simple sin agregar dependencias (no se usa H2 ni Testcontainers
 * en este paso).
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class MeditrackApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto arranca sin excepciones, la prueba pasa.
    }
}