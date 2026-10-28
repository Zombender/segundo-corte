# TecnoUAM: React, Axios y visualización (integrante 4)

Frontend del inventario de equipos. Usa componentes funcionales, `useState`, `useEffect` y Axios para consultar el backend Spring Boot de este repositorio. La aplicación no incluye registros de ejemplo: todos los equipos y contadores provienen de `GET /api/equipos`.

## Ejecutar

Requisitos: Node.js 20.19 o superior, Java 17 y el backend con su base PostgreSQL disponible.

1. Inicia PostgreSQL con la base `tecnouam`. Ejecuta `TecnouamApplication` desde tu IDE con `JWT_SECRET_BASE64` configurada en sus variables de entorno, o usa `.\mvnw.cmd spring-boot:run` en una terminal con esa variable. El [README principal](../README.md) explica cómo generar la clave. La conexión utiliza `postgres` / `123`. Con `ddl-auto=create`, las tablas se recrean y los registros se borran en cada arranque.

2. En otra terminal:

   ```powershell
   cd frontend
   npm ci
   npm run dev
   ```

3. Abre **http://127.0.0.1:5173** e inicia sesión con una cuenta del backend.

   | Cuenta de desarrollo | Contraseña | Rol |
   | --- | --- | --- |
   | `user` | `User123!` | USER |
   | `admin` | `Admin123!` | ADMIN |

   Ambas cuentas pueden consultar. El frontend de este integrante es una interfaz de consulta; las operaciones de escritura se realizan mediante los endpoints del backend.

## Conexión con la API

Vite redirige `/api` hacia `http://127.0.0.1:8080`, tanto en desarrollo como con `npm run preview`. Esto permite utilizar Axios desde el navegador sin agregar permisos CORS al backend.

Para cambiar el puerto o dirección del backend, copia `.env.example` a `.env`:

```dotenv
API_PROXY_TARGET=http://127.0.0.1:8080
VITE_API_BASE_URL=/api
```

Reinicia Vite después de cambiar estas variables. Las variables `VITE_` son públicas; no deben contener claves JWT, contraseñas ni secretos. Para desplegar `dist/`, el servidor de producción debe servir la aplicación y redirigir `/api` a Spring Boot, o configurar una URL de API y CORS adecuados en el backend.

Flujo de autenticación:

1. `POST /api/auth/login` recibe `{ "username": "...", "password": "..." }`.
2. La interfaz lee `{ "token": "...", "type": "Bearer" }`.
3. Axios consulta `GET /api/equipos` con `Authorization: Bearer <token>`.
4. El token se conserva en `sessionStorage` de esa pestaña para sobrevivir a una recarga. La contraseña no se almacena.
5. Una respuesta 401 en la consulta elimina la sesión y vuelve al formulario. Un 403 muestra un mensaje de permisos. Cerrar sesión borra el token local.

El login usa JSON y autenticación Bearer sin cookies. El ajuste de CSRF del backend permite estas solicitudes bajo `/api/**` y conserva las reglas de autorización USER/ADMIN.

## Funcionalidad y demostración

- Tabla con código patrimonial, nombre, tipo y estado.
- Contadores calculados a partir de la respuesta de la API.
- Búsqueda por nombre, código o tipo y filtro por disponibilidad.
- Estados visibles de carga, inventario vacío, búsqueda sin resultados y error con reintento.
- Vista adaptable a móvil y escritorio, etiquetas accesibles y navegación por teclado.
- Botón **Actualizar equipos** y recarga del navegador para obtener los cambios de Postman.

Para demostrar la integración, inicia sesión, crea un equipo desde Postman con un token ADMIN y pulsa **Actualizar equipos**. Registra un préstamo para ese mismo equipo y vuelve a actualizar: debe aparecer como **Prestado**. Registra la devolución y actualiza para verlo **Disponible**. Si la tabla está vacía, registra datos mediante la API; no hay una lista simulada de respaldo.

## Compilar y verificar

```powershell
npm test
npm run build
npm run preview
```

La compilación queda en `frontend/dist`. Las pruebas del cliente usan un servidor HTTP aislado para comprobar credenciales, cabecera Bearer, respuestas vacías, errores 401/403, contrato y cancelación. Ese servidor pertenece únicamente a las pruebas y no se usa al ejecutar la aplicación.

En la raíz, desde una terminal con `JWT_SECRET_BASE64` configurada, se puede ejecutar la prueba de conexión del backend. Usa las cuentas originales y H2 en memoria, independiente de PostgreSQL:

```powershell
.\mvnw.cmd -Dtest=FrontendIntegrationTests test
```

`FrontendIntegrationTests` usa una base H2 en memoria y la cadena real de Spring Security para comprobar login sin CSRF de sesión, consulta con JWT, 401 sin token, 403 al intentar escribir con USER y credenciales incorrectas.

## Archivos para explicar en la defensa

| Archivo | Responsabilidad |
| --- | --- |
| `src/App.jsx` | Estado de sesión y cambio entre acceso e inventario. |
| `src/components/LoginForm.jsx` | Formulario y solicitud de autenticación. |
| `src/api/client.js` | Instancia Axios, login, consulta Bearer y errores. |
| `src/hooks/useEquipos.js` | `useEffect`, carga real y cancelación al desmontar o recargar. |
| `src/components/Inventory.jsx` | `useState`, búsqueda, filtro, contadores y actualización. |
| `src/components/EquipoTable.jsx` | Tabla y estados de carga, vacío y error. |
| `vite.config.js` | Proxy local hacia Spring Boot. |

Documentación de referencia: [useEffect de React](https://react.dev/reference/react/useEffect), [instancias Axios](https://axios-http.com/docs/instance) y [proxy de Vite](https://vite.dev/config/server-options#server-proxy).
