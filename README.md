# NOVA

NOVA es una plataforma de aprendizaje gamificado para estudiantes de primaria y secundaria. Refuerza contenidos sobre derechos de la mujer, prevención de violencia, equidad de género y dignidad por medio de misiones cortas, preguntas interactivas, niveles, rachas y retroalimentación visual adaptada a cada etapa educativa.

Busca ser una experiencia accesible y motivadora tanto para estudiantes como para docentes, que se pueda trabajar desde el aula o desde el teléfono.

El producto es **NOVA** y lo desarrolla el equipo **MindFlow** para el reto de plataforma de aprendizaje basado en juegos del Hackathon Nicaragua 2026.

---

## Qué hay hoy

| Parte | Estado | Dónde corre |
| --- | --- | --- |
| **Backend** (`backend/`) | API REST completa para autenticación, salas, niveles, misiones, intentos, progreso, rachas y vistas por rol | Railway, con PostgreSQL |
| **App móvil** (`mobile/`) | Flujo completo del estudiante y pantalla del docente con datos de ejemplo | Android |
| **Landing** (`landing/`) | Página de presentación del producto | Cloudflare Workers |

En la app el estudiante puede:

- Iniciar sesión con su ID o con Google, y unirse a su sala con un código.
- Recorrer la ruta de aprendizaje de su nivel y jugar misiones de verdadero o falso, opción múltiple y emparejar.
- Llevar sus plumas (vidas) y su racha diaria, que se congela si deja de jugar y vuelve a encenderse al completar una misión.
- Ver su progreso y su perfil, y cambiar entre modo claro y oscuro.

---

## Estructura del repositorio

```txt
NOVA/
├─ backend/              API REST en Node.js + Express
│  ├─ src/
│  │  ├─ routes/         Rutas HTTP y qué rol puede usarlas
│  │  ├─ middleware/     Validación del token y del rol
│  │  ├─ controllers/    Un caso de uso por función: valida, orquesta y responde
│  │  ├─ services/       Reglas de negocio puras (calificar, racha, quién ve a quién)
│  │  ├─ repositories/   Todo el SQL, agrupado por tabla o agregado
│  │  ├─ database/       Conexión, transacciones, schema.sql y data.sql
│  │  ├─ utils/          JWT, contraseñas y verificación del token de Google
│  │  ├─ config/         Variables de entorno
│  │  ├─ app.js
│  │  └─ server.js
│  ├─ tests/             Tests con Jest
│  └─ railway.json       Configuración del despliegue
├─ mobile/               App Android en Kotlin + Jetpack Compose
│  └─ app/src/
│     ├─ main/java/com/mindflow/nova/
│     │  ├─ ui/          Pantallas, componentes, ViewModels y tema
│     │  └─ data/        Repositorios, cliente de la API, sesión y modelos
│     └─ test/           Tests unitarios
├─ landing/              Landing en React + Vite + Tailwind (ver landing/README.md)
└─ .github/workflows/    CI
```

---

## Arquitectura

El backend y la app siguen la misma separación por capas del análisis y diseño orientado a objetos: **Presentación → Negocio → Persistencia**. Cada capa solo conoce a la de abajo, así que no hay ciclos, y ninguna pantalla ni ruta habla directamente con la base de datos o con la API.

| Estereotipo | Backend | App móvil |
| --- | --- | --- |
| «Frontera» | `routes/`, `middleware/` | Pantallas y componentes de `ui/` |
| «Control» | `controllers/`, con las reglas puras en `services/` | ViewModels (`*ViewModel.kt`) |
| «Entidad» | Tablas de `schema.sql`, leídas y escritas desde `repositories/` | `data/model/` |
| «Servicio» | `utils/` (JWT, contraseñas, token de Google), `database/transaction.js` | Repositorios de `data/`, `data/remote/` (Retrofit) y `data/session/` |

En el backend, cada función de un repositorio recibe la conexión (`db`) como parámetro. Así la misma consulta sirve sola o dentro de una transacción de `withTransaction`.

En la app, las pantallas leen el estado de su ViewModel con `StateFlow`, y el ViewModel pide los datos a una interfaz de repositorio. En los tests esa interfaz se reemplaza por una falsa, sin red.

---

## Tecnologías

| Parte | Tecnologías |
| --- | --- |
| Backend | Node.js 18+, Express 5, PostgreSQL (`pg`), JWT (`jsonwebtoken`), `bcryptjs`, `google-auth-library`, Jest |
| App móvil | Kotlin, Jetpack Compose, Material 3, ViewModel + StateFlow, Retrofit, JUnit, `kotlinx-coroutines-test` |
| Landing | React 19, Vite, Tailwind CSS 4, oxlint, Cloudflare Workers |
| Infraestructura | Railway (backend), PostgreSQL gestionado, GitHub Actions |

---

## Backend

### Instalación

```bash
git clone https://github.com/THEGABOALE/NOVA.git
cd NOVA/backend
npm install
```

### Variables de entorno

Copiar `.env.example` como `.env` y completar los valores:

```env
PORT=3000
NODE_ENV=development

# En producción basta con DATABASE_URL y se ignoran las variables DB_* de abajo
DATABASE_URL=

DB_HOST=localhost
DB_PORT=5432
DB_NAME=mindflow_db
DB_USER=postgres
DB_PASSWORD=your_password_here

GOOGLE_CLIENT_ID=your_google_oauth_client_id_here

JWT_SECRET=your_jwt_secret_here
JWT_EXPIRES_IN=7d

CORS_ORIGINS=
```

`JWT_SECRET` es obligatoria en producción: sin ella, el servidor no arranca. En desarrollo, si falta, se usa un secreto de prueba y aparece un aviso en la consola. `CORS_ORIGINS` lista, separados por coma, los sitios web que pueden llamar a la API desde un navegador. La app móvil no lo necesita, así que vacío no permite ninguno. El archivo `.env` nunca se sube al repositorio.

### Base de datos

Crear la base y cargar primero la estructura y después los datos de prueba:

```bash
psql -U postgres -d mindflow_db -f src/database/schema.sql
psql -U postgres -d mindflow_db -f src/database/data.sql
```

También se pueden ejecutar abriendo los archivos en SQLTools, desde VS Code.

### Ejecución

```bash
npm run dev     # con recarga automática (nodemon)
npm start       # modo normal
npm test        # tests con Jest
```

La API queda en `http://localhost:3000`, o en el puerto definido en `.env`.

### Despliegue

El backend se publica en Railway según `railway.json`. Railway arranca el servidor con `npm start`, revisa que esté vivo con `/api/health` y lo reinicia si falla. En producción la conexión a la base llega por `DATABASE_URL` y va cifrada.

La instancia puede tardar en responder la primera petición después de un rato sin uso. Por eso la app la despierta al abrirse y espera hasta 30 segundos por respuesta.

### Endpoints

Las rutas protegidas piden el header `Authorization: Bearer <token>`. El token se obtiene al iniciar sesión.

| Método | Ruta | Acceso | Qué hace |
| --- | --- | --- | --- |
| GET | `/api/health` | Público | Confirma que el servidor está vivo |
| GET | `/api/health/db` | Público | Confirma la conexión con PostgreSQL |
| POST | `/api/auth/login/id` | Público | Inicia sesión con `loginId` y `password` |
| POST | `/api/auth/login/google` | Público | Inicia sesión con el `idToken` de Google |
| GET | `/api/auth/me` | Con sesión | Devuelve el usuario de la sesión |
| POST | `/api/auth/students` | Coordinador, admin | Crea una cuenta con ID y contraseña |
| GET | `/api/levels` | Con sesión | Niveles educativos con sus misiones |
| POST | `/api/groups/join` | Estudiante | Une al estudiante a una sala con un `code` |
| GET | `/api/students/:studentId/context` | Ver nota | Sala, nivel y centro del estudiante |
| GET | `/api/students/:studentId/progress` | Ver nota | Puntos, misiones completadas y racha |
| GET | `/api/missions/:missionId` | Con sesión | Misión con sus preguntas |
| POST | `/api/missions/:missionId/attempts` | Estudiante | Abre un intento, si la misión es de su nivel y ya completó la anterior |
| POST | `/api/missions/attempts/:attemptId/finish` | Estudiante | Califica las respuestas y actualiza progreso y racha |
| GET | `/api/teacher/me/students` | Docente | Estudiantes de sus salas |
| GET | `/api/coordinator/me/overview` | Coordinador | Resumen de su centro |
| GET | `/api/admin/overview` | Admin | Resumen global |
| GET | `/api/admin/users` | Admin | Lista de usuarios |
| GET | `/api/admin/centers/:centerId/overview` | Admin | Resumen de un centro |

**Nota:** los datos de un estudiante los puede ver él mismo, el docente de su sala, el coordinador de su centro y el admin.

La calificación, el límite de tiempo y las semillas los decide siempre el servidor. Un repaso paga la mitad y solo una vez cada 24 horas por misión. El login y los códigos de sala aceptan 10 intentos fallidos cada 15 minutos; después responden `429`.

Al crear cuentas, el coordinador las crea en su propio centro. El admin indica `centerId`, que es obligatorio para docentes y coordinadores. Las contraseñas de estudiantes piden al menos 4 caracteres y las demás al menos 8.

Ejemplo de inicio de sesión:

```json
POST /api/auth/login/id
{
  "loginId": "profedemo",
  "password": "profe2026"
}
```

```json
{
  "message": "Sesión iniciada correctamente",
  "status": "OK",
  "token": "<JWT_TOKEN>",
  "user": {
    "id": 4,
    "fullName": "Profesor Demo",
    "email": null,
    "loginId": "profedemo",
    "role": "teacher",
    "centerId": 1,
    "group": null
  }
}
```

### Roles

| Rol | Puede |
| --- | --- |
| `student` | Unirse a una sala, jugar misiones y ver su propio progreso |
| `teacher` | Ver a los estudiantes de sus salas |
| `coordinator` | Ver su centro educativo y crear cuentas de estudiantes y docentes |
| `admin` | Ver todo el sistema y crear cuentas de cualquier rol (equipo MindFlow) |

---

## App móvil

Se abre la carpeta `mobile/` en Android Studio, que usa JDK 21. La app pide Android 7.0 (API 24) o superior.

La URL de la API depende del tipo de build:

- **debug:** `http://10.0.2.2:3000/`, que desde el emulador apunta al backend local de la computadora.
- **release:** el backend desplegado en Railway.

Para probar contra el backend local basta con levantarlo con `npm run dev` y correr la app en el emulador. Para una demo en un teléfono se instala el build release, que se firma con la clave de debug y no sirve para publicar en Play Store.

Tests unitarios, desde `mobile/`:

```bash
./gradlew testDebugUnitTest
```

---

## Landing

Está en `landing/`. Cómo correrla, publicarla y los detalles de marca están en [landing/README.md](landing/README.md).

---

## Datos de prueba

`data.sql` crea un centro, una sala de Primaria alta con el código `NOVA123`, cinco misiones de prueba (tres de Primaria alta y una en cada nivel de secundaria) y estas cuentas:

| Rol | loginId | Contraseña |
| --- | --- | --- |
| Estudiante | `garciaga` | `1234` |
| Docente | `profedemo` | `profe2026` |
| Coordinador | `coordinador` | `coord2026` |
| Admin | `adminmindflow` | `admin2026` |

El código `NOVA123` vence 30 días después de cargar los datos.

Estas credenciales son solo para desarrollo local.

---

## CI

GitHub Actions corre tres trabajos en cada push y en cada PR hacia `main`:

- **Backend:** `npm test`.
- **Móvil:** `./gradlew testDebugUnitTest`.
- **Landing:** `npm run lint` y `npm run build`.

Un PR solo debería mergearse con los tres en verde.

---

## Flujo de trabajo

Hay una rama por área, y todos los PRs van hacia `main`:

| Rama | Para |
| --- | --- |
| `main` | Lo que está integrado y funcionando |
| `backend` | API y base de datos |
| `frontend/mobile` | App Android |
| `frontend/landing` | Landing |
| `docs/readme` | Documentación |

Antes de empezar, se actualiza la rama con `main`. Cuando el cambio está listo, se abre el PR hacia `main`. No se trabaja directamente sobre `main` ni se abren PRs de una rama hacia otra.

### Commits

```txt
tipo(alcance): descripción breve
```

```bash
git commit -m "feat(mobile): agregar racha en el inicio"
git commit -m "fix(backend): calcular la racha en la zona horaria del estudiante"
git commit -m "docs(readme): actualizar estructura del proyecto"
```

| Tipo | Uso |
| --- | --- |
| `feat` | Funcionalidad nueva |
| `fix` | Corrección de un error |
| `refactor` | Cambio interno sin cambiar el comportamiento |
| `docs` | Documentación |
| `test` | Tests |
| `style` | Formato |
| `perf` | Rendimiento |
| `ci` | GitHub Actions |
| `chore` | Mantenimiento |

### Qué no se sube

`node_modules/`, `.env`, `dist/`, `mobile/.gradle/`, `mobile/build/`, `mobile/app/build/`, `mobile/local.properties`, `.idea/` y `.vscode/`. Solo se sube `.env.example`.

---

## Próximos pasos

1. Conectar la pantalla del docente a datos reales del backend.
2. Crear las pantallas de coordinador y administrador en la app.
3. Probar el login con Google en un teléfono real contra el backend desplegado.
4. Sumar misiones a los niveles de secundaria, que hoy tienen una cada uno.
5. Preparar una versión estable para la demostración.

Más adelante está prevista una versión web/PWA con uso sin conexión y la generación de material imprimible.

---

## Equipo

**NOVA** es desarrollado por el equipo **MindFlow** para el Hackathon Nicaragua 2026.
