package com.mindflow.nova.ui.screens.auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.JoinGroupRequest
import com.mindflow.nova.data.model.JoinGroupResponse
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaLoginButton
import com.mindflow.nova.ui.theme.NovaLoginFieldBorder
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Splash de 2 páginas que sigue al primer login de un estudiante sin sala
 * (wireframe de Figma, sección "splash"): intro deslizable y después el
 * código de acceso. Solo se muestra una vez — la matrícula queda guardada en
 * el backend, así que en logins posteriores esto se salta directo al home.
 */
@Composable
fun OnboardingScreen(onJoined: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

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
                AccessCodePage(onJoined = onJoined)
            }
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

        // Ilustración pendiente de diseño: en el wireframe es un placeholder gris.
        MascotaIllustrationPlaceholder(
            label = "Mascota saludando al usuario",
            bubble = "Globo de texto de la mascota diciéndole hola al usuario"
        )

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
private fun AccessCodePage(onJoined: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Accede a tu salón",
            color = NovaText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        MascotaIllustrationPlaceholder(label = "Mascota, indicando esto (lock in)")

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pon el código que se te proporcionó de tu colegio/institución para acceder a tu salón de clases.",
            color = NovaText,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase(); errorMessage = null },
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

        Spacer(modifier = Modifier.weight(1f))

        PageDots(total = 2, activeIndex = 1, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (code.isBlank() || isLoading) return@Button

                isLoading = true
                errorMessage = null

                scope.launch {
                    try {
                        val response = RetrofitClient.api.joinGroupByCode(JoinGroupRequest(code.trim()))
                        val body = response.body()

                        if (response.isSuccessful && body?.status == "OK") {
                            onJoined()
                        } else {
                            errorMessage = extractErrorMessage(response.errorBody()?.string(), body)
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
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

private fun extractErrorMessage(rawError: String?, body: JoinGroupResponse?): String {
    if (!rawError.isNullOrEmpty()) {
        return try {
            JSONObject(rawError).optString("message", "No se pudo validar el código")
        } catch (e: Exception) {
            "No se pudo validar el código"
        }
    }

    return body?.message ?: "No se pudo validar el código"
}

@Composable
private fun MascotaIllustrationPlaceholder(label: String, bubble: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF626262), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)

            if (bubble != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFFD9D9D9), shape = RoundedCornerShape(16.dp)) {
                    Text(
                        text = bubble,
                        color = NovaText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
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
