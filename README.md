# TecnoUAM – Sistema de Gestión de Equipos y Préstamos

## Segundo Corte – Servicios Web

### Descripción del proyecto

TecnoUAM es una aplicación web desarrollada para administrar el inventario de equipos tecnológicos y gestionar los préstamos realizados dentro de una institución educativa.

El sistema permite registrar, consultar, actualizar y eliminar equipos, así como gestionar sus préstamos y devoluciones.

La solución utiliza una arquitectura cliente-servidor, con un backend desarrollado en Spring Boot, una base de datos PostgreSQL y un frontend implementado con React.

El acceso a los recursos está protegido mediante autenticación JWT y autorización basada en roles.

---

## 1. Tecnologías utilizadas

### Backend

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- JSON Web Tokens (JWT)
- BCrypt
- Hibernate
- Maven

### Base de datos

- PostgreSQL

### Frontend

- React
- JavaScript
- Vite
- Axios

### Pruebas

- Postman
- Colección de pruebas HTTP
- Scripts de validación de respuestas
- Pruebas funcionales y de seguridad
- Pruebas de integración frontend-backend

---

## 2. Arquitectura del sistema

La aplicación está organizada en tres componentes principales:

### Frontend

Desarrollado con React.

Permite a los usuarios interactuar con el sistema mediante una interfaz web, iniciar sesión y consultar la información de los equipos.

Las solicitudes HTTP se realizan hacia la API REST del backend.

### Backend

Desarrollado con Spring Boot.

Se encarga de:

- Procesar las solicitudes HTTP.
- Validar los datos recibidos.
- Aplicar las reglas de negocio.
- Gestionar los equipos y préstamos.
- Autenticar usuarios mediante JWT.
- Verificar permisos según los roles.
- Comunicarse con PostgreSQL.

### Base de datos

PostgreSQL almacena la información de:

- Equipos tecnológicos.
- Códigos patrimoniales.
- Tipos de equipos.
- Estados de disponibilidad.
- Préstamos registrados.
- Fechas de préstamo y devolución.
- Estados de los préstamos.

### Flujo general

```text
Usuario
   |
   v
Frontend React
   |
   | Peticiones HTTP / JWT
   v
API REST - Spring Boot
   |
   | Servicios y reglas de negocio
   v
Spring Data JPA / Hibernate
   |
   v
Base de datos PostgreSQL
```

---

## 3. Requisitos previos

Para ejecutar el proyecto es necesario contar con:

- JDK 17.
- PostgreSQL instalado y en ejecución.
- Node.js 20.19 o superior.
- npm.
- Git.
- IntelliJ IDEA u otro IDE compatible con Java.
- Postman para realizar las pruebas de la API.

### Puertos utilizados

| Servicio | Puerto |
|----------|--------|
| Backend Spring Boot | 8080 |
| Frontend React | 5173 |
| PostgreSQL | 5432 |

---

## 4. Configuración de la base de datos

El proyecto utiliza PostgreSQL.

Se debe crear una base de datos denominada:

```sql
CREATE DATABASE tecnouam;
```

La configuración de conexión se encuentra en:

```text
src/main/resources/application.properties
```

La configuración utiliza los siguientes parámetros:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tecnouam
spring.datasource.username=postgres
```

La contraseña debe coincidir con la configurada localmente en PostgreSQL y en `application.properties`.

### Importante

La configuración actual del proyecto utiliza:

```properties
spring.jpa.hibernate.ddl-auto=create
```

Esto significa que Hibernate vuelve a crear las tablas cuando se inicia el backend.

**Advertencia:** los registros almacenados anteriormente pueden eliminarse al reiniciar la aplicación.

Esta configuración se utiliza durante el desarrollo y las pruebas académicas.

---

## 5. Configuración de seguridad JWT

El backend utiliza Spring Security y tokens JWT para proteger los endpoints.

La aplicación requiere una variable de entorno denominada:

```text
JWT_SECRET_BASE64
```

Esta variable debe contener una clave de al menos 32 bytes codificada en Base64.

### Generar la clave desde PowerShell

Ejecutar:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
```

Para consultar la clave generada:

```powershell
$env:JWT_SECRET_BASE64
```

Si el backend se ejecuta desde IntelliJ IDEA, es necesario configurar la variable en:

```text
Run > Edit Configurations > Environment variables
```

Nombre:

```text
JWT_SECRET_BASE64
```

Valor:

```text
Clave Base64 generada
```

La clave JWT no debe incluirse directamente en el código fuente ni publicarse en el repositorio.

### Duración del token

Los tokens de autenticación tienen una duración de 30 minutos.

Cuando un token expire, el usuario deberá autenticarse nuevamente.

---

## 6. Ejecución del backend

### Opción 1: IntelliJ IDEA

1. Abrir el proyecto en IntelliJ IDEA.
2. Esperar a que Maven cargue las dependencias.
3. Verificar que PostgreSQL esté funcionando.
4. Configurar `JWT_SECRET_BASE64`.
5. Ejecutar la clase principal:

```text
TecnouamApplication
```

6. Esperar el mensaje:

```text
Started TecnouamApplication
```

La API quedará disponible en:

```text
http://localhost:8080
```

### Opción 2: PowerShell

Desde la carpeta raíz del proyecto, configurar la variable JWT y ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El backend debe permanecer encendido mientras se utiliza el frontend o Postman.

---

## 7. Ejecución del frontend

Abrir una segunda terminal desde la raíz del proyecto.

Ingresar a la carpeta del frontend:

```powershell
cd frontend
```

Instalar las dependencias:

```powershell
npm ci
```

Iniciar el servidor de desarrollo:

```powershell
npm run dev
```

Abrir en el navegador:

```text
http://127.0.0.1:5173
```

El frontend se comunica con el backend mediante solicitudes HTTP a los endpoints `/api`.

Vite redirige las peticiones correspondientes hacia el backend de Spring Boot.

---

## 8. Autenticación y roles

El sistema cuenta con dos roles principales:

| Rol | Descripción |
|-----|-------------|
| ADMIN | Permite consultar y administrar equipos y préstamos. |
| USER | Permite consultar equipos y préstamos, sin realizar modificaciones. |

### Usuarios de prueba

Las siguientes cuentas están configuradas únicamente para el entorno académico de desarrollo.

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin | Admin123! | ADMIN |
| user | User123! | USER |

No deben utilizarse estas credenciales en un entorno de producción.

### Inicio de sesión

**Método:**

```http
POST /api/auth/login
```

**Body:**

```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

**Respuesta esperada:**

```json
{
  "token": "JWT_GENERADO_POR_EL_SERVIDOR",
  "type": "Bearer"
}
```

El token debe enviarse en las solicitudes protegidas mediante la cabecera:

```http
Authorization: Bearer TOKEN_JWT
```

---

## 9. Endpoints de la API REST

### Autenticación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Autenticar usuarios y generar JWT. |

### Gestión de equipos

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| GET | `/api/equipos` | Listar equipos. | USER / ADMIN |
| GET | `/api/equipos/{id}` | Consultar equipo por ID. | USER / ADMIN |
| POST | `/api/equipos` | Registrar un equipo. | ADMIN |
| PUT | `/api/equipos/{id}` | Actualizar un equipo. | ADMIN |
| DELETE | `/api/equipos/{id}` | Eliminar un equipo. | ADMIN |

### Gestión de préstamos

| Método | Endpoint | Descripción | Rol |
|--------|----------|-------------|-----|
| GET | `/api/prestamos` | Listar préstamos. | USER / ADMIN |
| GET | `/api/prestamos/{id}` | Consultar préstamo por ID. | USER / ADMIN |
| POST | `/api/prestamos` | Registrar un préstamo. | ADMIN |
| PUT | `/api/prestamos/{id}/devolver` | Registrar una devolución. | ADMIN |

---

## 10. Modelo de datos

### Equipo

Un equipo contiene los siguientes atributos:

| Campo | Descripción |
|-------|-------------|
| id | Identificador único. |
| codigoPatrimonial | Código único del equipo. |
| nombre | Nombre del equipo. |
| tipo | Tipo de equipo tecnológico. |
| estado | Estado actual de disponibilidad. |

### Tipos de equipo permitidos

```text
LAPTOP
PROYECTOR
TABLETA
```

### Estados de equipo

```text
DISPONIBLE
PRESTADO
```

### Préstamo

Un préstamo contiene:

| Campo | Descripción |
|-------|-------------|
| id | Identificador del préstamo. |
| equipo | Equipo asociado al préstamo. |
| nombreSolicitante | Persona que solicita el equipo. |
| fechaPrestamo | Fecha del préstamo. |
| fechaPrevistaDevolucion | Fecha prevista de devolución. |
| estado | Estado del préstamo. |

### Estados de préstamo

```text
ACTIVO
DEVUELTO
```

---

## 11. Reglas de negocio

El sistema implementa las siguientes reglas:

1. Cada equipo debe tener un código patrimonial único.
2. Un equipo nuevo debe registrarse con estado `DISPONIBLE`.
3. No es posible prestar un equipo que ya se encuentra `PRESTADO`.
4. Al registrar un préstamo válido, el equipo cambia automáticamente a `PRESTADO`.
5. No se permite registrar dos préstamos activos para un mismo equipo.
6. No se permite eliminar un equipo que tenga un préstamo activo.
7. Al registrar una devolución, el préstamo cambia a `DEVUELTO`.
8. Después de la devolución, el equipo vuelve al estado `DISPONIBLE`.
9. La fecha prevista de devolución no puede ser anterior a la fecha de préstamo.
10. Los datos obligatorios deben proporcionarse correctamente.
11. Las operaciones de modificación requieren autenticación con rol `ADMIN`.

---

## 12. Pruebas de la API mediante Postman

### Responsable de las pruebas

Integrante 5: Nelson Lazo Rocha.

### Objetivo

Verificar el funcionamiento de los servicios web mediante pruebas funcionales, validación de seguridad JWT, autorización por roles, reglas de negocio e integración con el frontend.

Las pruebas se realizaron utilizando una colección de Postman con solicitudes HTTP y scripts de validación de respuestas.

### Colección Postman

Archivo:

```text
Coleccion_Examen_Servicios_Web_Integrante_5.postman_collection.json
```

Para ejecutar la colección:

1. Iniciar PostgreSQL.
2. Iniciar el backend de Spring Boot.
3. Abrir Postman.
4. Importar el archivo JSON de la colección.
5. Configurar la variable `baseUrl`.
6. Ejecutar el login de ADMIN.
7. Ejecutar el login de USER.
8. Realizar las solicitudes de prueba en el orden establecido.

### Variables de la colección

| Variable | Descripción |
|----------|-------------|
| baseUrl | Dirección del backend. |
| adminUsername | Usuario administrador de prueba. |
| adminPassword | Contraseña del administrador de prueba. |
| userUsername | Usuario con rol USER. |
| userPassword | Contraseña del usuario de prueba. |
| adminToken | Token JWT del administrador. |
| userToken | Token JWT del usuario. |
| tipoEquipo | Tipo de equipo válido. |
| codigoPatrimonial | Código generado para las pruebas. |
| equipoId | ID del equipo creado. |
| prestamoId | ID del préstamo registrado. |
| codigoReact | Código del equipo utilizado en React. |
| equipoReactId | ID del equipo utilizado en React. |
| prestamoReactId | ID del préstamo utilizado en React. |
| fechaPrestamo | Fecha del préstamo. |
| fechaPrevistaDevolucion | Fecha prevista de devolución. |
| fechaInvalida | Fecha utilizada para verificar validaciones. |
| idInexistente | ID utilizado para comprobar respuestas 404. |

Los scripts de Postman permiten guardar automáticamente los tokens JWT y los identificadores generados por el backend.

---

## 13. Casos de prueba realizados

### Pruebas de autenticación

| Caso | Resultado esperado |
|------|--------------------|
| Login de ADMIN con credenciales válidas | 200 OK |
| Login de USER con credenciales válidas | 200 OK |
| Login con credenciales incorrectas | 401 Unauthorized |

### Pruebas de autorización

| Caso | Resultado esperado |
|------|--------------------|
| Consultar equipos sin JWT | 401 Unauthorized |
| Consultar equipos con USER | 200 OK |
| Crear equipo utilizando USER | 403 Forbidden |
| Crear equipo utilizando ADMIN | 201 Created |

### Pruebas de equipos

| Caso | Resultado esperado |
|------|--------------------|
| Registrar equipo válido | 201 Created |
| Consultar equipos existentes | 200 OK |
| Consultar equipo por ID | 200 OK |
| Actualizar equipo existente | 200 OK |
| Registrar equipo sin nombre obligatorio | 400 Bad Request |
| Consultar equipo inexistente | 404 Not Found |
| Registrar código patrimonial duplicado | 409 Conflict |
| Eliminar equipo permitido | 204 No Content |
| Consultar equipo eliminado | 404 Not Found |

### Pruebas de préstamos

| Caso | Resultado esperado |
|------|--------------------|
| Consultar préstamos | 200 OK |
| Registrar préstamo con fechas inválidas | 400 Bad Request |
| Registrar préstamo válido | 201 Created |
| Consultar equipo después del préstamo | Estado PRESTADO |
| Registrar préstamo duplicado | 409 Conflict |
| Eliminar equipo con préstamo activo | 409 Conflict |
| Registrar devolución | 200 OK |
| Consultar equipo después de la devolución | Estado DISPONIBLE |
| Consultar préstamo por ID | 200 OK |
| Intentar devolver nuevamente el préstamo | 409 Conflict |

---

## 14. Integración entre Postman y React

La integración permite comprobar que los datos mostrados en el frontend provienen del backend y no de información simulada.

### Escenario 1: Creación de equipo

1. Autenticarse con el rol ADMIN.
2. Crear un equipo mediante Postman.
3. Verificar que el backend responda `201 Created`.
4. Abrir el frontend en React.
5. Actualizar el listado de equipos.
6. Comprobar que el nuevo registro aparezca en pantalla.

### Escenario 2: Registro de préstamo

1. Seleccionar el equipo creado.
2. Registrar un préstamo mediante Postman.
3. Verificar la respuesta `201 Created`.
4. Consultar el estado del equipo.
5. Actualizar la interfaz de React.
6. Comprobar que el equipo aparezca con estado `PRESTADO`.

### Escenario 3: Devolución

1. Registrar la devolución mediante Postman.
2. Verificar la respuesta `200 OK`.
3. Consultar nuevamente el equipo.
4. Actualizar el frontend.
5. Comprobar que el estado cambie a `DISPONIBLE`.

Estos escenarios permiten demostrar la comunicación real entre los componentes del sistema.

---

## 15. Evidencias de pruebas

Las evidencias se obtuvieron mediante capturas de las solicitudes y respuestas HTTP realizadas en Postman.

Se documentaron los siguientes aspectos:

- Autenticación exitosa.
- Generación y utilización de JWT.
- Restricciones de acceso por roles.
- Creación y actualización de equipos.
- Validación de campos obligatorios.
- Manejo de recursos inexistentes.
- Registro de préstamos.
- Validación de conflictos de negocio.
- Registro de devoluciones.
- Eliminación de equipos permitida.
- Integración entre Postman, Spring Boot y React.

Las capturas permiten verificar los métodos HTTP utilizados, las rutas consultadas, los cuerpos enviados, los códigos de respuesta y los resultados obtenidos.

Para su entrega, las evidencias deben organizarse junto con la documentación del proyecto.

---

## 16. Códigos HTTP utilizados

| Código | Significado | Aplicación |
|--------|-------------|------------|
| 200 OK | Solicitud procesada correctamente. | Consultas, actualizaciones y devoluciones. |
| 201 Created | Recurso creado correctamente. | Registro de equipos y préstamos. |
| 204 No Content | Operación completada sin contenido. | Eliminación exitosa. |
| 400 Bad Request | Datos inválidos. | Campos faltantes o fechas incorrectas. |
| 401 Unauthorized | Autenticación requerida o inválida. | Solicitudes sin JWT o credenciales incorrectas. |
| 403 Forbidden | Permisos insuficientes. | Intentos de modificación con USER. |
| 404 Not Found | Recurso inexistente. | Consultas de IDs no registrados. |
| 409 Conflict | Conflicto con las reglas de negocio. | Duplicados, préstamos activos y devoluciones repetidas. |

---

## 17. Verificación del proyecto

### Backend

Desde la raíz del proyecto:

```powershell
.\mvnw.cmd test
```

Este comando ejecuta las pruebas automatizadas configuradas en el proyecto.

### Frontend

Desde la carpeta `frontend`:

```powershell
npm test
```

Para verificar la compilación:

```powershell
npm run build
```

### API REST

Las pruebas funcionales y de autorización se ejecutan mediante la colección de Postman.

Para reproducir correctamente los escenarios que modifican datos, las peticiones deben ejecutarse en el orden indicado.

---

## 18. Consideraciones de seguridad

La aplicación implementa las siguientes medidas:

- Autenticación mediante JWT.
- Control de acceso por roles.
- Protección de endpoints REST.
- Cifrado de contraseñas mediante BCrypt.
- Validación de datos de entrada.
- Restricción de operaciones administrativas.
- Verificación de reglas de negocio.

### Recomendaciones para producción

Para utilizar el sistema fuera del entorno académico se recomienda:

- Eliminar las cuentas de prueba.
- Eliminar las cuentas de prueba.
- Configurar las contraseñas mediante variables de entorno.
- No publicar las claves JWT.
- Utilizar HTTPS.
- Implementar gestión segura de secretos.
- Incorporar migraciones de base de datos.
- Evitar `ddl-auto=create` en producción.
- Configurar mecanismos de auditoría y monitoreo.

---

## 19. Conclusiones

El desarrollo de TecnoUAM permitió aplicar conceptos de servicios web REST mediante la integración de Spring Boot, PostgreSQL y React.

La implementación contempla operaciones CRUD, autenticación JWT, autorización basada en roles y reglas de negocio relacionadas con la disponibilidad y el préstamo de equipos tecnológicos.

Las pruebas funcionales realizadas mediante Postman permitieron comprobar el comportamiento de los endpoints, las restricciones de seguridad y el manejo de respuestas HTTP.

Adicionalmente, las pruebas de integración con React permitieron observar los cambios realizados desde Postman directamente en la interfaz web.

De esta manera, el proyecto demuestra el funcionamiento de una aplicación cliente-servidor con persistencia de datos, servicios REST protegidos e integración entre sus componentes.

---

## 20. Contexto académico

**Proyecto:** TecnoUAM – Sistema de Gestión de Equipos y Préstamos.

**Asignatura:** Servicios Web.

**Evaluación:** Segundo Corte.

**Entorno:** Desarrollo académico.

**Contribución del integrante 5:** Elaboración de colección Postman, pruebas funcionales, validación de autenticación y autorización, comprobación de reglas de negocio, integración con React y recopilación de evidencias.