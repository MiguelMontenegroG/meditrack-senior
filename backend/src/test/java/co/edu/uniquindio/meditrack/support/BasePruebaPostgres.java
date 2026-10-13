package co.edu.uniquindio.meditrack.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base para las pruebas de integracion de la capa de persistencia.
 *
 * <p>Levanta un unico PostgreSQL 16 efimero con Testcontainers (misma version
 * que el {@code docker-compose.yml}) y lo conecta al contexto Spring con
 * {@link DynamicPropertySource}. De este modo las consultas JPQL reales se
 * ejecutan contra PostgreSQL, no contra una BD embebida distinta: es justo el
 * escenario donde aparecieron los errores de tipado (bytea/timestamp/SMALLINT)
 * que los mocks no detectan.</p>
 *
 * <p>El contenedor se arranca una sola vez por JVM en un bloque estatico y se
 * reutiliza entre todas las clases de prueba que heredan de esta base. No se
 * usa la anotacion {@code @Testcontainers}/{@code @Container}: con ese ciclo de
 * vida, un contenedor {@code static} se detiene al terminar la primera clase y
 * las siguientes fallan al conectar ({@code CannotCreateTransaction}). El
 * arranque manual en un singleton estatico garantiza que el contenedor viva
 * hasta que la JVM de pruebas termine. La JVM de Testcontainers se encarga de
 * detenerlo (Ryuk).</p>
 *
 * <p>Flyway aplica las migraciones en el contenedor al arrancar el contexto, por
 * lo que el esquema es exactamente el de produccion. Las pruebas deben limpiar
 * los datos que insertan (el contenedor se comparte entre metodos y clases).</p>
 *
 * <p>Se fijan las variables que la configuracion de seguridad lee del entorno
 * (JWT) para que el contexto arranque sin depender del {@code .env}.</p>
 */
@SpringBootTest(properties = {
        "JWT_SECRET=clave-de-prueba-con-mas-de-32-bytes-para-hs256!!",
        "JWT_EXPIRATION_MINUTES=60",
        "CORS_ALLOWED_ORIGINS=http://localhost:5173"
})
@ActiveProfiles("test")
public abstract class BasePruebaPostgres {

    /** Contenedor unico para toda la JVM de pruebas. */
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.6-alpine"))
                    .withDatabaseName("meditrack_test")
                    .withUsername("meditrack")
                    .withPassword("meditrack");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registrarPropiedades(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
    }
}