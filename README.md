# GRH · Gestión de Recursos Humanos

Aplicación web para gestionar empleados, departamentos, cargos y contratos, con acceso por roles y reportes por departamento. Es mi primer proyecto completo: backend, frontend, seguridad y tests.

## Qué hace

- **Empleados:** alta, edición, baja y alta lógica (nunca se borran). Al crear uno se genera su usuario con una contraseña temporal.
- **Departamentos y cargos:** CRUD completo. No se pueden eliminar si están en uso (respuesta 409).
- **Contratos:** crear, ver y editar el contrato de cada empleado (tipo, fechas, salario, horario).
- **Reportes por departamento:** empleados activos e inactivos, edad y antigüedad media, rango salarial, presupuesto frente a masa salarial, empleados por tipo de contrato, cargo y lugar de trabajo, altas del año y contratos que vencen.
- **Perfil propio:** cada usuario ve su ficha y su contrato, y edita sus datos de contacto.
- **Cambio de contraseña.**

## Roles y permisos

| Rol | Qué puede hacer |
|---|---|
| **ADMIN** | Todo: empleados, departamentos, cargos, contratos y reportes |
| **MANAGER** | Ver su departamento, sus empleados, sus contratos y sus reportes; ver su perfil |
| **EMPLOYEE** | Ver su perfil y su contrato; editar sus datos de contacto |

El frontend oculta lo que cada rol no puede usar, pero **la seguridad real está en el backend** (`@PreAuthorize` más comprobaciones en los servicios, por ejemplo que un manager solo acceda a su departamento).

## Stack

**Backend:** Java 25, Spring Boot 4.1, Spring Security (JWT con jjwt), Spring Data JPA, MySQL, Lombok, Bean Validation.
**Frontend:** React, Vite, React Router, Recharts y lucide-react. Peticiones con `fetch` y un envoltorio propio (`apiFetch`), sin `axios`: cubre lo mismo con una dependencia menos.
**Tests:** JUnit 5, Mockito, AssertJ y MockMvc (`@WebMvcTest`).

## Estructura

```
/                  Backend (pom.xml, src/main/java/com/grh/grh/...)
/grh-frontend      Frontend React
```

Backend organizado por funcionalidad: `auth`, `security`, `config`, `usuario`, `employee`, `department`, `position`, `contract`, `reports`, `exception`, `validation`.

## Requisitos

- JDK 25
- Node.js 20 o superior
- MySQL 8

## Puesta en marcha

### 1. Base de datos

```sql
CREATE DATABASE grh;
CREATE USER 'grh_app'@'localhost' IDENTIFIED BY 'tu_contraseña';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON grh.* TO 'grh_app'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Variables de entorno

Copia `.env.example` como `.env` en la raíz (junto al `pom.xml`) y rellénalo. Sin comillas ni espacios alrededor del `=`.

| Variable | Descripción |
|---|---|
| `DB_USERNAME`, `DB_PASSWORD` | Credenciales de MySQL |
| `JWT_SECRET` | 32 bytes aleatorios en Base64 |
| `DATA_ENCRYPTION_KEY` | 32 bytes aleatorios en Base64 (cifra DNI y seguridad social) |
| `DATA_HASH_KEY` | 32 bytes aleatorios en Base64 (hash para detectar duplicados) |
| `APP_EMAIL_DOMAIN` | Opcional. Dominio de los emails. Por defecto `grh.test` |
| `DEMO_PASSWORD` | Contraseña de los usuarios de demo (mínimo 8 caracteres, con letra y número) |
| `DB_URL`, `SHOW_SQL`, `JWT_EXPIRATION` | Opcionales |

Las tres claves deben ser **distintas**. Genera cada una en PowerShell:

```powershell
$b = New-Object byte[] 32; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

> Guarda las claves fuera del repositorio. Con la clave de cifrado perdida no se pueden recuperar los datos cifrados, y cambiar la de hash invalida la detección de duplicados.

### 3. Backend

Para cargar datos de demo, arranca con el perfil `dev`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

En IntelliJ: *Edit Configurations*, campo *Active profiles*, valor `dev`. (El perfil no se activa desde el `.env`.) El backend queda en `http://localhost:8080`.

### 4. Frontend

```bash
cd grh-frontend
npm install
npm run dev
```

Queda en `http://localhost:5173`; Vite redirige `/api` al backend. Si cambias `APP_EMAIL_DOMAIN`, cambia también `EMAIL_DOMAIN` en `grh-frontend/src/pages/Employees.jsx`.

## Datos de demo

Con el perfil `dev` y la base de datos vacía se crean 2 departamentos (Tecnología y Operaciones), 3 cargos y 10 empleados con contratos variados. Todos los usuarios entran con el valor de `DEMO_PASSWORD`:

| Usuario | Rol |
|---|---|
| `admin@grh.test` | ADMIN |
| `pedro.martinez@grh.test` | MANAGER de Tecnología |
| `laura.gomez@grh.test` | MANAGER de Operaciones |
| `ana.garcia@grh.test` | EMPLOYEE |

Si cambias el dominio, sustituye `grh.test` por el tuyo. Carlos Díaz está dado de baja a propósito y no puede iniciar sesión. Los datos solo se crean si no existe el admin, así que reiniciar no los duplica.

## Tests

```bash
./mvnw test
```

Cubren los servicios (reglas de negocio y acceso por rol), los controllers (validación, códigos HTTP y seguridad), los validadores (DNI/NIE) y el cifrado.

## Decisiones de diseño

- **Seguridad en capas:** `@PreAuthorize` en los controllers y comprobación de acceso por departamento en los servicios, también en los reportes.
- **Baja lógica:** desactivar un empleado también desactiva su usuario, y el filtro JWT comprueba `isEnabled()` en cada petición, así que el acceso se corta al instante.
- **Datos sensibles:** DNI y seguridad social se guardan cifrados con AES-GCM. Un HMAC en columna aparte permite detectar duplicados sin descifrar. La API nunca los devuelve.
- **Contraseñas:** BCrypt. Las temporales son aleatorias de 12 caracteres.
- **Validación en dos niveles:** el navegador frena lo evidente y el backend lo comprueba siempre (DNI con letra de control, formatos, rangos, coherencia de fechas).
- **Errores coherentes:** `GlobalExceptionHandler` convierte cada excepción en su código (400, 403, 404, 409) y el frontend muestra el mensaje real.
- **Secretos fuera del código:** todo va por variables de entorno.

## Limitaciones y mejoras futuras

Pensado como entorno de desarrollo y demo, **no listo para producción**. Pendiente:

- Token JWT en cookie `HttpOnly` con `SameSite` (hoy está en `localStorage`).
- Migraciones con Flyway y `ddl-auto=validate`.
- Obligar a cambiar la contraseña temporal en el primer acceso.
- Contenerización con Docker Compose.
- Tests de frontend (Vitest y React Testing Library) y cobertura con JaCoCo.
- HTTPS y gestión de secretos en un despliegue real.

## Autor

Alberto · [GitHub](https://github.com/alberto0024dev)
