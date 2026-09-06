# NOVA

NOVA es una plataforma digital de aprendizaje gamificado orientada a estudiantes de preescolar, primaria y secundaria. Su propósito es reforzar contenidos relacionados con derechos de la mujer, prevención de violencia, equidad de género y dignidad mediante misiones, retos, preguntas interactivas, niveles, retroalimentación visual y actividades breves adaptadas a cada etapa educativa.

El proyecto busca ofrecer una experiencia accesible, motivadora y útil tanto para estudiantes como para docentes, permitiendo que los contenidos puedan ser trabajados desde el aula o desde dispositivos móviles y navegadores web.

El proyecto es desarrollado por el equipo **MindFlow**. El nombre del producto es **NOVA**.

---

## Descripción técnica general

NOVA está pensado como una plataforma web/móvil con enfoque educativo y gamificado. La arquitectura actual contempla un backend desarrollado con **Node.js + Express**, una base de datos central en **PostgreSQL** y una aplicación móvil desarrollada en **Android con Kotlin y Jetpack Compose**.

El backend expone una API REST que permite manejar autenticación, roles de usuario, grupos, códigos de acceso, niveles educativos, misiones, intentos de misión, progreso de estudiantes y vistas específicas para docentes, coordinadores y administradores.

Actualmente, el sistema ya cuenta con una base funcional conectada a PostgreSQL, datos de prueba, endpoints principales, una integración inicial con la aplicación móvil y despliegue del backend en la nube para pruebas externas.

---

## Tecnologías utilizadas

### Backend

- **pg**: cliente de PostgreSQL para conectar el backend con la base de datos.
- **Node.js**: entorno de ejecución para JavaScript en el servidor.
- **Express.js**: framework para construir la API REST.
- **PostgreSQL**: base de datos relacional central del sistema.
- **pg**: cliente de PostgreSQL para Node.js.
- **dotenv**: manejo de variables de entorno.
- **cors**: configuración de acceso entre cliente y servidor.
- **jsonwebtoken**: generación y validación de tokens JWT.
- **bcryptjs**: manejo de contraseñas cifradas.
- **nodemon**: reinicio automático del servidor durante desarrollo.
- **JavaScript**: lenguaje utilizado en el backend.

### Aplicación móvil

- **Kotlin**: lenguaje principal de la aplicación Android.
- **Jetpack Compose**: construcción de interfaces declarativas.
- **Retrofit**: consumo de endpoints del backend.
- **Material Design**: componentes visuales para la interfaz móvil.

### Tecnologías previstas para el proyecto completo

- **React + Vite**: frontend web/PWA previsto.
- **Progressive Web App (PWA)**: instalación y uso offline.
- **IndexedDB**: almacenamiento local para escenarios con conectividad limitada.
- **Kaboom.js**: motor para experiencias interactivas o lúdicas.
- **jsPDF**: generación de material imprimible.

> Nota: el backend y la aplicación móvil ya tienen avances funcionales. El frontend web/PWA, IndexedDB, Kaboom.js y jsPDF forman parte de la arquitectura prevista, pero no necesariamente están implementados en esta etapa.

---

## Estructura actual del proyecto

```txt
MindFlow/
├─ backend/
│  ├─ .env.example
│  ├─ package.json
│  ├─ package-lock.json
│  └─ src/
│     ├─ app.js
│     ├─ server.js
│     ├─ config/
│     │  └─ env.js
│     ├─ controllers/
│     │  ├─ admin.controller.js
│     │  ├─ auth.controller.js
│     │  ├─ coordinator.controller.js
│     │  ├─ group.controller.js
│     │  ├─ health.controller.js
│     │  ├─ level.controller.js
│     │  ├─ mission.controller.js
│     │  ├─ student.controller.js
│     │  └─ teacher.controller.js
│     ├─ middlewares/
│     │  └─ auth.middleware.js
│     ├─ routes/
│     │  ├─ admin.routes.js
│     │  ├─ auth.routes.js
│     │  ├─ coordinator.routes.js
│     │  ├─ group.routes.js
│     │  ├─ health.routes.js
│     │  ├─ level.routes.js
│     │  ├─ mission.routes.js
│     │  ├─ student.routes.js
│     │  └─ teacher.routes.js
│     └─ database/
│        ├─ connection.js
│        ├─ schema.sql
│        └─ data.sql
├─ mobile/
│  ├─ app/
│  │  └─ src/
│  │     └─ main/
│  │        └─ java/com/mindflow/nova/
│  │           ├─ data/
│  │           │  ├─ model/
│  │           │  └─ remote/
│  │           ├─ ui/
│  │           │  ├─ components/
│  │           │  ├─ screens/
│  │           │  └─ theme/
│  │           ├─ MainActivity.kt
│  │           └─ NovaApp.kt
│  ├─ build.gradle.kts
│  ├─ settings.gradle.kts
│  └─ gradle.properties
├─ .gitignore
└─ README.md
```

---

## Instalación del proyecto

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd MindFlow
```

### 2. Entrar al backend

```bash
cd backend
```

### 3. Instalar dependencias

```bash
npm install
```

Este comando instala las dependencias definidas en `package.json`, como Express, dotenv y nodemon.

---

## Variables de entorno

El backend utiliza un archivo `.env` dentro de la carpeta `backend/`.

Crear el archivo:

```bash
touch .env
```

Ejemplo de configuración local:

```env
PORT=3000
NODE_ENV=development

DB_HOST=localhost
DB_PORT=5432
DB_NAME=mindflow_db
DB_USER=postgres
DB_PASSWORD=your_password_here

JWT_SECRET=dev-secret-cambiar-en-produccion
JWT_EXPIRES_IN=7d
```

El archivo `.env` no debe subirse al repositorio. Solo debe subirse `.env.example`.

---

## Base de datos

El proyecto utiliza PostgreSQL como base de datos principal.

Los archivos principales son:

```txt
backend/src/database/schema.sql
backend/src/database/data.sql
```

- `schema.sql`: crea la estructura de tablas.
- `data.sql`: inserta datos de prueba para desarrollo.

Durante desarrollo local, si se necesita reiniciar la base de datos, ejecutar primero `schema.sql` y luego `data.sql`.

Ejemplo usando Node desde la carpeta `backend/`:

```bash
node -e "require('dotenv').config(); const fs = require('fs'); const { Pool } = require('pg'); const pool = new Pool({ host: process.env.DB_HOST, port: process.env.DB_PORT, database: process.env.DB_NAME, user: process.env.DB_USER, password: process.env.DB_PASSWORD }); const sql = fs.readFileSync('src/database/schema.sql', 'utf8'); pool.query(sql).then(() => { console.log('schema.sql ejecutado correctamente'); pool.end(); }).catch(e => { console.error(e.message); pool.end(); });"
```

Luego:

```bash
node -e "require('dotenv').config(); const fs = require('fs'); const { Pool } = require('pg'); const pool = new Pool({ host: process.env.DB_HOST, port: process.env.DB_PORT, database: process.env.DB_NAME, user: process.env.DB_USER, password: process.env.DB_PASSWORD }); const sql = fs.readFileSync('src/database/data.sql', 'utf8'); pool.query(sql).then(() => { console.log('data.sql ejecutado correctamente'); pool.end(); }).catch(e => { console.error(e.message); pool.end(); });"
```

También se puede ejecutar desde SQLTools en VS Code, abriendo los archivos y ejecutando los bloques SQL correspondientes.

---

## Ejecución del backend

Desde la carpeta `backend/`:

```bash
npm run dev
```

Si todo está correcto, el backend debería levantarse en:

```txt
http://localhost:3000
```

O en el puerto definido dentro del archivo `.env`.

---

## Despliegue en la nube

Además de poder ejecutarse localmente, el backend de NOVA ya cuenta con un despliegue en la nube para facilitar pruebas externas e integración con la aplicación móvil.

Durante el desarrollo local, la API puede ejecutarse en:

```txt
http://localhost:3000
```

Para pruebas en la nube, se utiliza la URL del backend desplegado:

```txt
<URL_DEL_BACKEND_EN_LA_NUBE>
```

La aplicación móvil puede consumir la API local o la API desplegada, dependiendo de la configuración definida en el cliente móvil.

Ejemplos de endpoints en local:

```txt
POST http://localhost:3000/api/auth/login/id
GET http://localhost:3000/api/levels
POST http://localhost:3000/api/groups/join
```

Ejemplos de endpoints en la nube:

```txt
POST <URL_DEL_BACKEND_EN_LA_NUBE>/api/auth/login/id
GET <URL_DEL_BACKEND_EN_LA_NUBE>/api/levels
POST <URL_DEL_BACKEND_EN_LA_NUBE>/api/groups/join
```

> Nota: la URL exacta del backend en la nube debe mantenerse actualizada según el servicio de despliegue utilizado por el equipo.

## Scripts disponibles

Los scripts principales del backend se encuentran en `backend/package.json`.

Ejemplo esperado:

```json
{
  "scripts": {
    "dev": "nodemon src/server.js",
    "start": "node src/server.js"
  }
}
```

Ejecutar en desarrollo:

```bash
npm run dev
```

Ejecutar en modo normal:

```bash
npm start
```

---

## Endpoints principales

### Salud del sistema

```txt
GET /
GET /api/health/db
```

Permiten verificar que el servidor esté activo y que exista conexión con PostgreSQL.

---

### Autenticación

```txt
POST /api/auth/login/id
```

Permite iniciar sesión mediante `loginId` y contraseña.

Ejemplo:

```json
{
  "loginId": "profedemo",
  "password": "profe2026"
}
```

Respuesta esperada:

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

El token debe enviarse en rutas protegidas mediante el header:

```txt
Authorization: Bearer <JWT_TOKEN>
```

---

### Códigos de grupo

```txt
POST /api/groups/join
```

Permite que un estudiante se una a un grupo mediante código de acceso.

Ejemplo:

```json
{
  "code": "NOVA123",
  "studentName": "Gabriel Demo"
}
```

El backend valida que el código exista, esté activo, no haya expirado y no haya alcanzado su límite de usos.

---

### Niveles educativos

```txt
GET /api/levels
```

Devuelve los niveles educativos registrados en la base de datos.

---

### Estudiantes

```txt
GET /api/students/:studentId/context
GET /api/students/:studentId/progress
```

Permiten obtener el contexto del estudiante, su grupo, nivel educativo y progreso.

Estas rutas pueden requerir token de autenticación.

---

### Misiones

```txt
GET /api/missions
GET /api/missions/:id
GET /api/missions/:id/questions
POST /api/missions/:id/attempts
POST /api/missions/attempts/:attemptId/finish
```

Permiten consultar misiones, obtener preguntas, iniciar intentos y finalizar misiones con respuestas del estudiante.

---

### Docente

```txt
GET /api/teacher/me/students
```

Permite consultar información asociada al docente, como estudiantes o grupos relacionados.

---

### Coordinador

```txt
GET /api/coordinator/me/overview
```

Permite consultar información general del centro educativo asociado al coordinador.

El rol coordinador está pensado para gestionar usuarios de su institución, como estudiantes y docentes, sin permisos globales de administrador.

---

### Administrador

```txt
GET /api/admin/overview
GET /api/admin/users
GET /api/admin/centers/:centerId/overview
```

Permiten consultar información global del sistema, usuarios y centros educativos.

---

## Roles del sistema

El backend contempla los siguientes roles:

```txt
student
teacher
coordinator
admin
```

### Estudiante

Puede acceder a su contexto, grupo, nivel, misiones y progreso.

### Docente

Puede consultar información relacionada con sus grupos y estudiantes.

### Coordinador

Puede gestionar usuarios dentro de su institución y consultar información de su centro educativo.

### Administrador

Puede consultar información general del sistema y gestionar información global.

---

## Datos de prueba

El archivo `backend/src/database/data.sql` contiene usuarios y datos iniciales para desarrollo.

Credenciales de prueba usadas durante el desarrollo:

```txt
Estudiante:
loginId: garciaga
password: 1234

Profesor:
loginId: profedemo
password: profe2026

Coordinador:
loginId: coordinador
password: coord2026

Admin:
loginId: adminmindflow
password: admin2026
```

Estas credenciales son únicamente para desarrollo local.

---

## Pruebas recomendadas del backend

Después de ejecutar `schema.sql`, `data.sql` y levantar el servidor, se recomienda probar:

Las pruebas pueden realizarse usando la API local o la API desplegada en la nube. Para pruebas locales se utiliza `http://localhost:3000`. Para pruebas externas se utiliza la URL del backend desplegado.

```txt
GET /
GET /api/health/db
POST /api/auth/login/id
GET /api/levels
POST /api/groups/join
GET /api/students/:studentId/context
GET /api/students/:studentId/progress
GET /api/missions
GET /api/missions/:id
GET /api/missions/:id/questions
POST /api/missions/:id/attempts
POST /api/missions/attempts/:attemptId/finish
GET /api/teacher/me/students
GET /api/coordinator/me/overview
GET /api/admin/overview
GET /api/admin/users
```

También se deben probar casos de error:

```txt
Login con contraseña incorrecta
Token ausente
Token inválido
Acceso con rol incorrecto
Código de grupo expirado
Código de grupo inexistente
Código de grupo con límite de usos alcanzado
```

---

## Aplicación móvil

La aplicación móvil está desarrollada en Android usando Kotlin y Jetpack Compose.

Actualmente incluye avances como:

- Pantalla para unirse a un grupo mediante código.
- Consumo del endpoint `POST /api/groups/join`.
- Manejo de mensajes de error del backend.
- Navegación inicial hacia pantallas de estudiante/docente.
- Estructura de modelos y servicios remotos para conexión con la API.
- Pantallas y componentes iniciales para home, lecciones, progreso, docente y perfil.

La integración móvil continúa en desarrollo, especialmente en la conexión con autenticación real, manejo de token JWT y navegación según rol.

---

## Buenas prácticas del repositorio

No subir al repositorio:

```txt
node_modules/
.env
backend/.env
mobile/.gradle/
mobile/build/
mobile/app/build/
mobile/local.properties
.idea/
.vscode/
```

Sí subir al repositorio:

```txt
package.json
package-lock.json
.env.example
src/
README.md
.gitignore
mobile/app/src/
mobile/build.gradle.kts
mobile/settings.gradle.kts
mobile/gradle.properties
mobile/gradle/wrapper/gradle-wrapper.properties
```

---

## Flujo de trabajo con Git y GitHub

El equipo utiliza Git y GitHub para controlar versiones, organizar avances y revisar cambios antes de integrarlos a la rama principal.

Flujo recomendado:

```bash
git switch main
git pull origin main
git switch -c tipo/nombre-de-la-rama
```

Después de trabajar:

```bash
git status
git add archivo-modificado
git commit -m "tipo(alcance): descripcion breve"
git push -u origin tipo/nombre-de-la-rama
```

Luego se crea un Pull Request en GitHub para revisión.

No se recomienda trabajar directamente sobre `main`.

---

## Convención de commits

Formato recomendado:

```txt
tipo(alcance): descripción breve del cambio realizado
```

Ejemplos:

```bash
git commit -m "core(config): agregar variables de entorno"
git commit -m "docs(readme): agregar base del repositorio"
git commit -m "core(api): inicializar backend express"
git commit -m "core(db): configurar conexion PostgreSQL"
git commit -m "feat(api): agregar login por id"
git commit -m "feat(api): agregar contexto de estudiante"
git commit -m "fix(mobile): mostrar mensaje de codigo invalido"
git commit -m "docs(readme): actualizar estado del proyecto"
```

Tipos de commit:

```txt
feat: nueva funcionalidad
fix: corrección de errores
refactor: refactorización sin cambiar la lógica externa
docs: cambios en documentación
style: cambios de formato
perf: mejoras de rendimiento
test: pruebas
chore: tareas de mantenimiento
ci: cambios en CI/CD
core: cambios de infraestructura o base del sistema
```

---

## Convención de nombres de ramas

Las ramas deben tener nombres claros y relacionados con la tarea que se está trabajando. Se recomienda usar minúsculas, guiones medios y un prefijo según el tipo de trabajo.

Formato recomendado:

```txt
tipo/nombre-de-la-rama
```

Ejemplos:

```txt
feature/conexion-postgresql
feat/api-student-session
feat/api-auth-login
mobile/group-code-login
mobile/auth-login
docs/readme-flujo-proyecto
fix/error-puerto-db
refactor/estructura-backend
```

---

## Estado actual del proyecto

Actualmente el proyecto cuenta con:

- Backend en Node.js y Express.
- Conexión funcional con PostgreSQL.
- Esquema de base de datos inicial.
- Datos de prueba para desarrollo.
- Autenticación por `loginId` y contraseña.
- Generación de tokens JWT.
- Roles de estudiante, docente, coordinador y administrador.
- Validación de códigos de grupo.
- Endpoints para niveles, estudiantes, misiones, docentes, coordinadores y administradores.
- Aplicación móvil en Kotlin con Jetpack Compose.
- Integración inicial entre mobile y backend.
- Pruebas manuales con Postman y SQLTools.
- Backend desplegado en la nube para pruebas externas.

---

## Próximos pasos técnicos

Los próximos avances recomendados son:

1. Terminar la integración del login real en la aplicación móvil.
2. Guardar el token JWT en Android para consumir rutas protegidas.
3. Redirigir al usuario según su rol.
4. Conectar las pantallas móviles con datos reales del backend.
5. Pulir el flujo de estudiante: login, home, misiones, progreso y retroalimentación.
6. Pulir el flujo de docente: grupos, estudiantes y seguimiento.
7. Validar permisos por rol en todas las rutas protegidas.
8. Limpiar archivos locales del repositorio, especialmente cachés de Android como `.gradle` e `.idea`.
9. Agregar scripts más simples para ejecutar `schema.sql` y `data.sql`.
10. Validar que la aplicación móvil consuma correctamente la API desplegada en la nube.
11. Preparar una versión estable para demostración.

---

## Rama de trabajo

El backend se está trabajando actualmente en la rama:

```txt
backend
```

Para subir cambios:

```bash
git add .
git commit -m "mensaje del commit"
git push origin backend
```

## Equipo

El proyecto **NOVA** es desarrollado por el equipo **MindFlow** como parte del reto de plataforma de aprendizaje basado en juegos del Hackathon Nicaragua 2026.
