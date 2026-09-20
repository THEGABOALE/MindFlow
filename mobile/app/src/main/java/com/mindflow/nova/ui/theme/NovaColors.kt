package com.mindflow.nova.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Tokens que cambian con el tema (claro/oscuro): leen de la paleta activa.
// Los valores concretos viven en NovaPalette.kt.
val NovaPurple: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.purple
val NovaBlue: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.blue
val NovaLightPurple: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.lightPurple
val NovaSoftPurple: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.softPurple
val NovaText: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.text

/** Color de lo que va encima de un relleno [NovaText] (ej. texto de un botón oscuro; en modo oscuro el botón es claro). */
val NovaOnText: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.onText
val NovaTextSecondary: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.textSecondary
val NovaBackground: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.background

/** Fondo de tarjetas y diálogos (blanco en claro). */
val NovaSurface: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.surface
val NovaBorder: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.border

/** Parte vacía de las barras de progreso. */
val NovaTrack: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.track
val NovaDark: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.dark
val NovaLocked: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.locked

// Semillas/recompensas: un dorado calido, para que la moneda del juego se
// distinga del morado de marca en vez de perderse como "un morado mas".
val NovaGold: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.gold
val NovaGoldLight: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.goldLight

// Acierto/error dentro de las lecciones. Antes vivian sueltos y duplicados
// como Color(0xFF...) en cada pantalla de mecanica (opcion multiple,
// relacion de conceptos, verdadero/falso); centralizados aca para que sean
// un solo lugar si hay que ajustar el tono.
val NovaSuccess: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.success
val NovaSuccessBackground: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.successBackground
val NovaError: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.error
val NovaErrorBackground: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.errorBackground

// Fondo de la etiqueta "en curso" / "siguiente" (texto en NovaBlue).
val NovaInfoBackground: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.infoBackground

// Fondo neutro de las burbujas y opciones sin elegir dentro de una leccion.
val NovaNeutralCard: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.neutralCard

// Boton "Verdadero"/"Falso" sin elegir.
val NovaChoiceIdle: Color @Composable @ReadOnlyComposable get() = LocalNovaPalette.current.choiceIdle

// Wireframe de Login/Splash: tonos propios de esa pantalla (siempre en claro), distintos al morado del resto de la app.
val NovaLoginCard = Color(0xFFDBC0F9)
val NovaLoginButton = Color(0xFF160065)
val NovaLoginFieldBorder = Color(0xFFCCCCD1)
