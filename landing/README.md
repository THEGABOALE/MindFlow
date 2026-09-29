# NOVA — Landing page

Página de presentación de NOVA, hecha con React 19, Vite y Tailwind CSS 4. Se publica en Cloudflare Workers como sitio estático.

## Desarrollo

Desde `landing/`:

```bash
npm install
npm run dev
```

Queda disponible en `http://localhost:5173`.

## Scripts

| Comando | Qué hace |
| --- | --- |
| `npm run dev` | Servidor de desarrollo con recarga en caliente |
| `npm run build` | Genera el sitio listo para publicar en `dist/` |
| `npm run preview` | Sirve la versión de `dist/` para revisarla antes de publicar |
| `npm run lint` | Revisa el código con oxlint |

CI corre `lint` y `build` en cada PR hacia `main`.

## Publicación

La configuración de Cloudflare está en `wrangler.jsonc`: publica el contenido de `dist/` y cualquier ruta desconocida vuelve a `index.html`.

```bash
npm run build
npx wrangler deploy
```

## Estructura

```txt
landing/
├─ design/          Originales del logo y del ícono en alta resolución (no se publican)
├─ public/          Archivos que se sirven tal cual: favicon y ícono para iOS
├─ src/
│  ├─ assets/       Logo e ícono optimizados (WebP con fondo transparente)
│  ├─ App.jsx       Contenido de la página
│  ├─ index.css     Colores de marca, tipografía y animaciones
│  └─ main.jsx      Punto de entrada
├─ index.html       Título, descripción y vista previa al compartir
└─ wrangler.jsonc   Configuración de Cloudflare
```

## Marca

- **Colores:** definidos una sola vez en `src/index.css` (`@theme`) y usados como clases de Tailwind: `text-nova-purple`, `bg-nova-blue/10`, etc. Son los mismos de la app móvil.
- **Tipografía:** Chillax para títulos, cargada desde Fontshare. Agenor Neue, la de texto, todavía no está en el proyecto: mientras tanto se usa la fuente del sistema.
- **Imágenes:** si cambia el logo o el ícono, se exportan desde `design/` con fondo transparente y al tamaño en que se muestran, no en alta resolución.
