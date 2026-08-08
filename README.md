# NOVA

NOVA es una plataforma digital de aprendizaje gamificado orientada a estudiantes de preescolar, primaria y secundaria. Su propósito es reforzar contenidos relacionados con derechos de la mujer, prevención de violencia, equidad de género y dignidad mediante misiones, retos, preguntas interactivas, niveles, retroalimentación visual y actividades breves adaptadas a cada etapa educativa.

El proyecto busca ofrecer una experiencia accesible, motivadora y útil tanto para estudiantes como para docentes, permitiendo que los contenidos puedan ser trabajados desde el aula o desde dispositivos móviles y navegadores web.

Actualmente, el desarrollo se encuentra en una etapa inicial, con la configuración base del backend en Node.js y Express.

## Descripción técnica general

El sistema está pensado como una plataforma web/móvil con enfoque PWA, capaz de funcionar en escenarios con conectividad limitada. La arquitectura propuesta contempla un frontend desarrollado con React + Vite, un backend con Node.js + Express y una base de datos central en PostgreSQL.

Por el momento, el repositorio contiene la estructura inicial del backend, incluyendo:

- Servidor Express configurado.
- Separación básica entre `app.js` y `server.js`.
- Uso de variables de entorno mediante archivo `.env`.
- Archivo `.env.example` para documentar las variables necesarias.
- Script de desarrollo con `nodemon`.
- Estructura inicial preparada para futuras rutas, controladores y conexión a base de datos.

## Tecnologías utilizadas

### Backend actual

- **pg**: cliente de PostgreSQL para conectar el backend con la base de datos.
- **Node.js**: entorno de ejecución para JavaScript en el servidor.
- **Express.js**: framework para construir la API del backend.
- **Nodemon**: herramienta de desarrollo para reiniciar el servidor automáticamente cuando hay cambios.
- **dotenv**: manejo de variables de entorno desde archivo `.env`.
- **JavaScript**: lenguaje utilizado actualmente para el backend.

### Tecnologías definidas para el proyecto completo

- **React + Vite**: frontend de la aplicación web/PWA.
- **Progressive Web App (PWA)**: soporte para instalación y uso offline.
- **PostgreSQL**: base de datos relacional central del sistema.
- **IndexedDB**: almacenamiento local en el navegador para funcionamiento offline.
- **Kaboom.js**: motor para experiencias interactivas o lúdicas.
- **jsPDF**: generación de material imprimible, como hojas de trabajo o misiones físicas.

> Nota: PostgreSQL ya cuenta con una conexión inicial desde el backend, IndexedDB, Kaboom.js, jsPDF y el frontend aún forman parte de la arquitectura prevista, pero no necesariamente están implementados en esta etapa.

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
│     │  └─ health.controller.js
│     ├─ routes/
│     │  └─ health.routes.js
│     └─ database/
│        ├─ connection.js
│        └─ schema.sql
├─ .gitignore
└─ README.md
```

## Instalación básica

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd MindFlow
```

### 2. Entrar a la carpeta del backend

```bash
cd backend
```

### 3. Instalar dependencias

```bash
npm install
```

Este comando instala las dependencias definidas en `package.json`, como Express, dotenv y nodemon.

---

## Configuración de variables de entorno

El backend utiliza un archivo `.env` para manejar configuraciones locales, como el puerto del servidor.

Crear el archivo `.env` dentro de la carpeta `backend/`:

```bash
touch .env
```

Ejemplo de contenido para `backend/.env`:

```env
PORT=3000
NODE_ENV=development
```

También debe existir un archivo `.env.example` para que otros integrantes del equipo sepan qué variables deben configurar:

```env
PORT=3000
NODE_ENV=development
```

Importante: el archivo `.env` no debe subirse al repositorio. Solo debe subirse `.env.example`.

---

## Ejecución del sistema

### Modo desarrollo

Desde la carpeta `backend/`, ejecutar:

```bash
npm run dev
```

Este comando inicia el servidor usando nodemon, permitiendo que el backend se reinicie automáticamente al hacer cambios en el código.

Si todo está correcto, el backend debería ejecutarse en:

```txt
http://localhost:3000
```

O en el puerto definido dentro del archivo `.env`.

---

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

### Ejecutar en desarrollo

```bash
npm run dev
```

### Ejecutar en modo normal

```bash
npm start
```

---

## Estado actual del backend

En esta etapa, el backend ya cuenta con una base inicial funcional:

- Conexió con PostgreSQL mediante la libreria `pg`
- Se agregó la ruta `GET /api/health/db`, la cual permite saber si la API puede conectarse correctamente a la base de datos configurada en el archivo `.env`.
- Proyecto Node.js inicializado.
- Express instalado.
- Servidor separado en archivos base.
- Variables de entorno configuradas.
- Archivo `.gitignore` creado para evitar subir archivos innecesarios.
- Preparación inicial para seguir creciendo hacia rutas, controladores, servicios y base de datos.

El siguiente avance técnico recomendado es configurar la conexión con PostgreSQL mediante la librería `pg`, crear una carpeta `database/` dentro de `src/` y agregar una ruta de prueba para verificar la conexión con la base de datos.

---

## Próximos pasos técnicos

Los siguientes pasos previstos para el backend son:

### 1. Validar y ejecutar el esquema inicial de base de datos

El archivo `backend/src/database/schema.sql` contiene las tablas base propuestas para el producto mínimo viable.

### 2. Crear endpoints iniciales de consulta

Primeras rutas sugeridas:

```txt
GET /api/levels
GET /api/missions
GET /api/missions/:id/questions
```

## Buenas prácticas del repositorio

No subir al repositorio:

```txt
node_modules/
.env
```

Sí subir al repositorio:

```txt
package.json
package-lock.json
.env.example
src/
README.md
.gitignore
```

## Flujo de trabajo con Git y GitHub

El equipo utiliza Git y GitHub para llevar control de versiones, organizar los avances del proyecto y revisar los cambios antes de integrarlos a la rama principal.

El flujo recomendado es trabajar mediante ramas separadas según el tipo de tarea, realizar commits descriptivos y subir los cambios mediante Pull Requests para su revisión.

## Convención de commits

Los mensajes de commit deben ser claros, breves y describir el cambio realizado. Se recomienda usar una estructura basada en el tipo de cambio realizado:

```txt
tipo(alcance): descripción breve del cambio realizado
```

Ejemplos:

```bash
git commit -m "core(config): agregar variables de entorno"
git commit -m "docs(readme): agregar base del repositorio"
git commit -m "core(api): inicializar backend express"
git commit -m "refactor(api): separar ruta de verificación"
```

Tipos de commit:

```txt
feat: Nueva funcionalidad o característica
fix: Corrección de errores
refactor: Refactorizar sin cambiar la lógica
docs: Cambios en la documentación
style: Cambios en el formato o estilo sin alterar la lógica
perf: Mejoras de rendimiento
test: Agregar o actualizar tests
chore: Tareas de mantenimiento (deps, tooling, build, etc)
ci: Cambios en CI/CD o automatización
core: Cambios en funcionalidad central o infraestructura
```

## Convención de nombres de ramas

Las ramas deben tener nombres claros y relacionados con la tarea que se está trabajando. Se recomienda usar minúsculas, guiones medios y un prefijo según el tipo de trabajo.

Formato recomendado:

```txt
tipo/nombre-de-la-rama
```

Ejemplos:

```txt
feature/conexion-postgresql
docs/actualizar-readme
fix/error-puerto-db
refactor/estructura-backend
```

Tipos recomendados para ramas:

```txt
feature: nuevas funcionalidades
fix: corrección de errores
docs: documentación
config: configuración
refactor: mejoras internas del código
```

Ejemplos de flujo de trabajo:

```bash
git switch main
git pull origin main
git switch -c feature/conexion-postgresql
```

Después de trabajar los cambios

```bash
git add .
git commit -m "core(db): configurar conexion PostgreSQL"
git push -u origin feature/conexion-postgresql
```

Luego de esto se crea un Pull Request en GitHub para revisar los cambios e integrarlos al repositorio.

## Equipo

El proyecto **NOVA** es desarrollado por el equipo **MindFlow** como parte del reto de plataforma de aprendizaje basado en juegos del Hackathon Nicaragua 2026.

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
