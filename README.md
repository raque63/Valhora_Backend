# Valhora Backend

API REST del e-commerce de relojes Valhora.

## Stack

- Java 21
- Spring Boot 4.1.0 (Web, Security, Data JPA, Validation)
- Hibernate / PostgreSQL
- JWT (jjwt) para autenticación (access token + refresh token rotado en cookie httpOnly)
- Cloudinary para imágenes
- Lombok + MapStruct
- springdoc-openapi (Swagger UI)

## Estructura de paquetes

Monolito modular, organizado por dominio (`com.valhora.backend`):

- `config` — configuración transversal (seguridad, CORS, OpenAPI, datos de arranque)
- `common` — utilidades y manejo de errores compartido (`GlobalExceptionHandler`, excepciones base, Cloudinary)
- `auth` — login, registro, emisión/refresco de JWT
- `users` — usuarios y roles
- `products` — catálogo de relojes
- `categories` — marcas y categorías
- `wishlist` — favoritos por usuario
- `reviews` — reseñas de clientes (con moderación desde el panel admin)

## Cómo levantar el entorno local

1. Levantar PostgreSQL con Docker Compose:

   ```bash
   docker compose up -d
   ```

2. Ejecutar la aplicación (con las credenciales de Cloudinary):

   ```bash
   CLOUDINARY_CLOUD_NAME=... CLOUDINARY_API_KEY=... CLOUDINARY_API_SECRET=... ./mvnw spring-boot:run
   ```

3. La API queda disponible en `http://localhost:8080`.
   Swagger UI: `http://localhost:8080/swagger-ui.html`

Sin perfil activo se usa `application.yml`, pensado para desarrollo local (CORS abierto a `localhost:5173`, cookies no-seguras, secreto JWT de ejemplo).

## Despliegue a producción

Existe un perfil `prod` (`application-prod.yml`) que se activa con `SPRING_PROFILES_ACTIVE=prod`. A diferencia del perfil de desarrollo, **no trae valores por defecto para nada sensible** — si falta una variable de entorno requerida, la aplicación falla al arrancar en vez de arrancar mal configurada (verificado: sin `JWT_SECRET` la app no levanta).

Variables de entorno requeridas en producción:

| Variable | Descripción |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | debe ser `prod` |
| `DATABASE_URL` | ej. `jdbc:postgresql://host:5432/valhora` |
| `DATABASE_USERNAME` | usuario de la base de datos |
| `DATABASE_PASSWORD` | contraseña de la base de datos |
| `JWT_SECRET` | secreto largo y aleatorio — generar con `openssl rand -base64 48` (nunca reutilizar el de desarrollo) |
| `CORS_ALLOWED_ORIGINS` | dominio(s) del frontend en producción, separados por coma (ej. `https://valhoracr.com,https://www.valhoracr.com`) |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | credenciales de Cloudinary |
| `PORT` | puerto en el que debe escuchar (muchas plataformas como Railway/Render lo inyectan automáticamente) |

Opcionales:

| Variable | Default en prod | Descripción |
| --- | --- | --- |
| `SEED_ENABLED` | `false` | poner en `true` **solo** en el primer arranque si querés crear el usuario admin inicial y datos de ejemplo; volver a `false` después |
| `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD` | vacío | credenciales del admin inicial, solo se usan si `SEED_ENABLED=true` |

El perfil `prod` además:
- Fuerza `secure-cookies: true` (el refresh token solo viaja por HTTPS).
- Desactiva `show-sql` (menos ruido en logs).

Para correr el jar empaquetado en el servidor de producción:

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=prod
```

(las variables de entorno anteriores deben estar definidas en el entorno donde corre el jar).

## Notas

- `spring.jpa.hibernate.ddl-auto=update` se usa tanto en desarrollo como en producción por ahora; para un proyecto que crezca más, considerar migrar a Flyway/Liquibase.
- El secreto JWT de `application.yml` (perfil por defecto) es solo un placeholder de desarrollo — nunca usarlo en producción.
