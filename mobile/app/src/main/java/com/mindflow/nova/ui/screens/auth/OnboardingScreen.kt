package com.mindflow.nova.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindflow.nova.ui.components.ScopedViewModels
import com.mindflow.nova.ui.components.zafiro.ZafiroIllustration
import com.mindflow.nova.ui.components.zafiro.ZafiroLines
import com.mindflow.nova.ui.components.zafiro.ZafiroPose
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaLoginButton
import com.mindflow.nova.ui.theme.NovaLoginFieldBorder
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlinx.coroutines.launch

/**
 * Splash de 2 páginas que sigue al primer login de un estudiante sin sala
 * (wireframe de Figma, sección "splash"): intro deslizable y después el
 * código de acceso. Solo se muestra una vez — la matrícula queda guardada en
 * el backend, así que en logins posteriores esto se salta directo al home.
 */
@Composable
fun OnboardingScreen(onJoined: () -> Unit, onLogout: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    // "Atrás" en la página del código vuelve a la bienvenida en vez de cerrar la app.
    BackHandler(enabled = pagerState.currentPage == 1) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Box(modifier = Modifier.fillMaxSize().background(NovaBackground)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = pagerState.currentPage == 0
        ) { page ->
            if (page == 0) {
                WelcomePage(
                    onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
            } else {
                // El ViewModel del código vive solo mientras se muestra esta pantalla.
                ScopedViewModels {
                    AccessCodePage(onJoined = onJoined)
                }
            }
        }

        // Salida para quien entró con la cuenta equivocada: sin esto quedaba
        // atrapado en el onboarding sin forma de volver al login.
        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Text(text = "Cerrar sesión", color = NovaTextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Bienvenido a NOVA",
            color = NovaText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "La primera App de educación 100% de apoyo a la clase de derecho y dignidad de la mujer",
            color = NovaText,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Zafiro todavía no tiene arte: en el wireframe es un recuadro gris.
        ZafiroIllustration(ZafiroPose.SALUDA, line = ZafiroLines.WELCOME)

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageDots(total = 2, activeIndex = 0)

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = onNext),
                shape = CircleShape,
                color = NovaPurple
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Siguiente",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AccessCodePage(
    onJoined: () -> Unit,
    viewModel: JoinGroupViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val errorMessage = state.errorMessage
    val isLoading = state.isLoading

    LaunchedEffect(state.joined) {
        if (state.joined) onJoined()
    }

    // imePadding: con el teclado abierto el botón "Continuar" sube en vez de quedar tapado.
    Column(modifier = Modifier.fillMaxSize().imePadding().padding(24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Accede a tu salón",
            color = NovaText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        ZafiroIllustration(ZafiroPose.EXPLICA, line = ZafiroLines.ACCESS_CODE)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pon el código que se te proporcionó de tu colegio/institución para acceder a tu salón de clases.",
            color = NovaText,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            // El texto se guarda tal cual se escribe y solo se muestra en mayúsculas:
            // cambiarlo dentro de onValueChange hace que el teclado pierda letras
            // al escribir rápido. El backend igual normaliza el código.
            onValueChange = { code = it; viewModel.clearError() },
            visualTransformation = UppercaseTransformation,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Insertar código", color = NovaTextSecondary, fontSize = 14.sp) },
            singleLine = true,
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp)
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFB3261E),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "¿No tienes código? Pídeselo a tu docente.",
            color = NovaTextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        PageDots(total = 2, activeIndex = 1, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.join(code) },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            enabled = code.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaLoginButton,
                contentColor = Color.White,
                disabledContainerColor = NovaLoginButton.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.4f)
            )
        ) {
            Text(text = if (isLoading) "Validando..." else "Continuar", fontSize = 16.sp)
        }
    }
}

/** Muestra el texto en mayúsculas sin modificar lo que guarda el campo. */
private val UppercaseTransformation = VisualTransformation { text ->
    TransformedText(
        AnnotatedString(text.text.map { it.uppercaseChar() }.joinToString("")),
        OffsetMapping.Identity
    )
}

@Composable
private fun PageDots(total: Int, activeIndex: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (index == activeIndex) NovaPurple else NovaLoginFieldBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}
