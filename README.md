# Valhora Backend

API REST del e-commerce de relojes Valhora.

## Stack

- Java 21
- Spring Boot 4.1.0 (Web, Security, Data JPA, Validation)
- Hibernate / PostgreSQL
- JWT (jjwt) para autenticación (access + refresh token)
- Lombok + MapStruct
- springdoc-openapi (Swagger UI)

## Estructura de paquetes

Monolito modular, organizado por dominio (`com.valhora.backend`):

- `config` — configuración transversal (seguridad, CORS, OpenAPI)
- `common` — utilidades y manejo de errores compartido (`GlobalExceptionHandler`, excepciones base)
- `auth` — login, registro, emisión/refresco de JWT
- `users` — usuarios y roles
- `products` — catálogo de relojes
- `categories` — categorías y marcas
- `orders` — pedidos

Cada módulo de dominio contendrá sus propios `controller`, `service`, `repository`, `dto` y `mapper` (MapStruct) a medida que se implemente.

## Cómo levantar el entorno local

1. Levantar PostgreSQL con Docker Compose:

   ```bash
   docker compose up -d
   ```

2. Ejecutar la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

3. La API queda disponible en `http://localhost:8080`.
   Swagger UI: `http://localhost:8080/swagger-ui.html`

## Notas

- `SecurityConfig` actualmente permite todas las peticiones (`permitAll`) como placeholder temporal; se reemplazará por el filtro JWT real al implementar el módulo `auth`.
- `spring.jpa.hibernate.ddl-auto=update` es solo para desarrollo; se revisará una estrategia de migraciones (Flyway/Liquibase) más adelante.
- El secreto JWT (`app.jwt.secret`) debe sobreescribirse vía variable de entorno `JWT_SECRET` fuera de desarrollo local.
