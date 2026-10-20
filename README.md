# GRH · Gestión de Recursos Humanos

Aplicación web para gestionar empleados, departamentos, cargos y contratos, con acceso por roles y reportes por departamento.

## Enfoque del proyecto
Tengo la intención de seguir desarrollando y mejorando este proyecto en el futuro.
Lo planteé como un proyecto de aprendizaje, en el que iba dedicando un rato cada semana. Mi objetivo era entender cómo trabajan juntos el backend y el frontend en una aplicación real, y no quedarme solo con la teoría.

- **Backend con Spring Boot:** empecé el proyecto para comenzar a aprender el framework: capas (controller, service, repository), JPA, DTOs, validación y manejo de errores.
- **Autenticación y autorización:** implementé JWT desde cero y un modelo de roles y authorities, con comprobaciones tanto en los controllers como en los servicios.
- **Protección de datos sensibles:** apliqué cada herramienta a su caso. BCrypt para contraseñas, AES-GCM para cifrar DNI y seguridad social, y HMAC para detectar duplicados sin descifrar.
- **Frontend con React:** hasta que lo construí no entendía del todo qué hace el frontend en la práctica: consumir la API, guardar el token, reflejar los errores del backend y adaptar la interfaz a cada rol.
- **Calidad:** escribí tests unitarios y de controller, y conseguí destapar fallos reales.

Es mi primer proyecto completo, así que prioricé buscar información y desarrollar el proyecto utilizando IA para aprender y cuestionar por que se hacía cada cosa.

## Demo

[Ver demo en YouTube](https://www.youtube.com/watch?v=ID_DEL_VIDEO)

## Qué hace

- **Empleados:** alta, edición, baja y alta lógica (nunca se borran). Al crear uno se genera su usuario con una contraseña temporal.
- **Departamentos y cargos:** CRUD completo. No se pueden eliminar si están en uso.
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

## Stack

**Backend:** Java 25, Spring Boot 4.1, Spring Security (JWT con jjwt), Spring Data JPA, MySQL, Lombok, Bean Validation.
**Frontend:** React, Vite, React Router, Recharts y lucide-react. Peticiones con `fetch` y un envoltorio propio (`apiFetch`).
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

Copia `.env.example` como `.env` en la raíz.

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

Si cambias el dominio, sustituye `grh.test`. Carlos Díaz está dado de baja a propósito y no puede iniciar sesión. Los datos solo se crean si no existe el admin, así que reiniciar no los duplica.

## Tests

```bash
./mvnw test
```

Cubren los servicios (reglas de negocio y acceso por rol), los controllers (validación, códigos HTTP y seguridad), los validadores (DNI/NIE) y el cifrado.

## Decisiones de diseño

- **Baja lógica:** desactivar un empleado también desactiva su usuario, y el filtro JWT comprueba `isEnabled()` en cada petición, así que el acceso se corta al instante.
- **Datos sensibles:** DNI y seguridad social se guardan cifrados con AES-GCM. Un HMAC en columna aparte permite detectar duplicados sin descifrar. La API nunca los devuelve.
- **Contraseñas:** BCrypt. Las temporales son aleatorias de 12 caracteres.
- **Errores:** `GlobalExceptionHandler` convierte cada excepción en su código (400, 403, 404, 409) y el frontend muestra el mensaje real.

## Limitaciones y mejoras futuras (y que tengo intención de aprender)
- Refactorizar
- Contenerización con Docker Compose.
- Implementar mas funcionalidades (sin requisitos me cuesta que implementar).
- Actualmente JWT en localStorage
- Implementar JWT con HttpOnly con Samesite
- Testing en frontend.
- Desplegar la aplicación

## Autor

Alberto · [GitHub](https://github.com/alberto0024dev)
