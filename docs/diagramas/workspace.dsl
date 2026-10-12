workspace "MediTrack Senior" "Descripcion de arquitectura - C4 Model (niveles 1 a 3)" {

    !identifiers hierarchical

    model {
        admin = person "Administrador" "Gestiona usuarios, pacientes y operacion del centro. Acceso completo."
        cuidador = person "Cuidador / Enfermero" "Registra bitacoras, administra medicacion y atiende tareas del dia."
        familiar = person "Familiar autorizado" "Consulta la informacion clinica de sus vinculados. Solo lectura."

        meditrack = softwareSystem "MediTrack Senior" "Aplicacion web para centros de atencion geriatrica: perfiles de adultos mayores, esquemas de medicacion, citas, bitacora de signos vitales, alertas y reportes PDF." "MediTrack" {

            spa = container "SPA (Frontend)" "Vite + React 19 + TypeScript + Tailwind CSS v4. React Router 7. Estado: AuthContext + api-client (fetch). Desplegado en Azure Static Web Apps." "React / TypeScript" {
                tags "Web"
            }

            api = container "API REST (Backend)" "Monolito modular por feature (paciente, medicacion, cita, bitacora, alerta, reporte, dashboard, usuario). Arquitectura hexagonal: domain / application / infrastructure (web + persistence). Stateless, JWT." "Java 21 / Spring Boot 3.4" {

                seguridad = component "Seguridad (config)" "SecurityFilterChain stateless, JwtAuthenticationFilter, BCrypt, @PreAuthorize por rol, CORS, entry points 401/403." "Spring Security" {
                    tags "Infra"
                }
                modPaciente = component "Modulo paciente" "Perfiles, contactos de emergencia, vinculacion familiar y asignacion de cuidador. Alcance por rol resuelto en backend (AlcancePaciente)." "Hexagonal" {
                    tags "Dominio"
                }
                modUsuario = component "Modulo usuario" "Autenticacion (login JWT), gestion de usuarios y cambio de contrasena. 8 casos de uso." "Hexagonal" {
                    tags "Dominio"
                }
                modMedicacion = component "Modulo medicacion" "Catalogo, esquemas, horarios y registro de tomas (pendiente)." "Hexagonal" {
                    tags "Dominio"
                }
                modCita = component "Modulo cita" "Agenda de citas medicas (pendiente)." "Hexagonal" {
                    tags "Dominio"
                }
                modBitacora = component "Modulo bitacora" "Registro de signos vitales (pendiente)." "Hexagonal" {
                    tags "Dominio"
                }
                modAlerta = component "Modulo alerta" "Alertas operativas; candidato a extraerse como microservicio (pendiente)." "Hexagonal" {
                    tags "Dominio"
                }
                modReporte = component "Modulo reporte" "Generador de reportes PDF (pendiente)." "Hexagonal" {
                    tags "Dominio"
                }
                modDashboard = component "Modulo dashboard" "KPIs operativos (pendiente)." "Hexagonal" {
                    tags "Dominio"
                }
                casosUso = component "Casos de uso (application)" "POJOs sin Spring: orquestan reglas de negocio, cableados como beans en config/*UseCaseConfig." "Application" {
                    tags "Application"
                }
                puertos = component "Puertos (application)" "Interfaces de salida: repositorios, PasswordEncoderPort, JwtPort, AuditoriaPort." "Application" {
                    tags "Application"
                }
                adaptadores = component "Adaptadores (infrastructure)" "Controllers REST (web) y repositorios Spring Data JPA (persistence) que implementan los puertos." "Infrastructure" {
                    tags "Infra"
                }
                scheduler = component "Scheduler / procesos" "Jobs programados para alertas (dosis atrasadas, citas proximas, signos fuera de rango)." "Spring Scheduling" {
                    tags "Infra"
                }
                auditoria = component "Auditoria" "AuditoriaPort + adaptador REQUIRES_NEW hacia auditoria_acceso (Ley 1581)." "Shared" {
                    tags "Infra"
                }

                seguridad -> casosUso "autentica/autoriza"
                modPaciente -> casosUso "usa"
                modUsuario -> casosUso "usa"
                modMedicacion -> casosUso "usa"
                modCita -> casosUso "usa"
                modBitacora -> casosUso "usa"
                modAlerta -> casosUso "usa"
                modReporte -> casosUso "usa"
                modDashboard -> casosUso "usa"
                casosUso -> puertos "depende de"
                adaptadores -> puertos "implementa"
                adaptadores -> db "JPA / Flyway" "JDBC"
                scheduler -> modAlerta "evalua reglas"
                casosUso -> auditoria "registra accesos"
            }

            db = container "Base de datos" "PostgreSQL 16. Esquema creado solo por Flyway (V1-V3: 16 tablas). Hibernate solo valida (ddl-auto: validate)." "PostgreSQL 16 / Flyway" {
                tags "Database"
            }

            spa -> api "HTTPS, JSON /api/**" "REST + JWT Bearer" "HTTPS/JSON"
            api -> db "SQL" "JDBC"
        }

        swa = softwareSystem "Azure Static Web Apps" "Sirve la SPA estatica y el fallback de navegacion." "Plataforma" {
            tags "External"
        }
        appService = softwareSystem "Azure App Service" "Host del backend Spring Boot (contenedor Docker multi-stage)." "Plataforma" {
            tags "External"
        }
        azureDb = softwareSystem "Azure Database for PostgreSQL" "Base de datos administrada (destino en produccion)." "Plataforma" {
            tags "External"
        }
        ghActions = softwareSystem "GitHub Actions" "CI/CD filtrado por paths (backend/**, frontend/**)." "Plataforma" {
            tags "External"
        }

        admin -> meditrack "Gestiona el centro" "HTTPS"
        cuidador -> meditrack "Registra bitacoras y medicacion" "HTTPS"
        familiar -> meditrack "Consulta (solo lectura)" "HTTPS"
        meditrack -> swa "se despliega en"
        meditrack -> appService "se despliega en"
        meditrack -> azureDb "persiste en (prod)"
        ghActions -> meditrack "construye y publica"
    }

    views {
        systemContext meditrack "ContextoDelSistema" {
            include *
            autolayout lr
        }

        container meditrack "Contenedores" {
            include *
            autolayout lr
        }

        component api "ComponentesBackend" {
            include *
            autolayout lr
        }

        styles {
            element "Person" {
                background #084d5c
                color #ffffff
                shape Person
            }
            element "Software System" {
                background #0e5a64
                color #ffffff
            }
            element "Container" {
                background #12707d
                color #ffffff
            }
            element "Component" {
                background #2a9daa
                color #ffffff
            }
            element "Database" {
                shape Cylinder
                background #0b3f47
                color #ffffff
            }
            element "Web" {
                shape WebBrowser
            }
            element "External" {
                background #6b7f85
                color #ffffff
            }
            relationship "Relationship" {
                color #0e5a64
            }
        }
    }
}
