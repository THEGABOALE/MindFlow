package com.mindflow.nova.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.SessionResult
import com.mindflow.nova.ui.screens.auth.LoginScreen
import com.mindflow.nova.ui.screens.auth.OnboardingScreen
import com.mindflow.nova.ui.screens.home.HomeScreen
import com.mindflow.nova.ui.screens.teacher.TeacherRoomsScreen
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

/**
 * Raíz de la app. Al arrancar valida el token guardado contra el backend
 * (GET /api/auth/me) y decide a dónde mandar a la persona:
 * - sin sesión válida -> [LoginScreen]
 * - estudiante sin sala todavía -> [OnboardingScreen] (splash + código)
 * - estudiante con sala -> [HomeScreen]
 * - docente -> [TeacherRoomsScreen]
 * - coordinador/admin -> todavía no tienen pantalla (falta su wireframe)
 */
private sealed class AppScreen {
    object Loading : AppScreen()
    object Login : AppScreen()
    object Onboarding : AppScreen()
    object StudentHome : AppScreen()
    object TeacherHome : AppScreen()
    data class Unsupported(val role: String) : AppScreen()
}

private fun routeForUser(user: SessionUser): AppScreen = when (user.role) {
    "student" -> if (user.group != null) AppScreen.StudentHome else AppScreen.Onboarding
    "teacher" -> AppScreen.TeacherHome
    else -> AppScreen.Unsupported(user.role)
}

@Composable
fun NovaApp(session: SessionRepository) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Loading) }

    LaunchedEffect(Unit) {
        screen = when (val result = session.restoreSession()) {
            is SessionResult.Success -> routeForUser(result.user)
            else -> AppScreen.Login
        }
    }

    when (val current = screen) {
        AppScreen.Loading -> LoadingScreen()

        AppScreen.Login -> LoginScreen(
            session = session,
            onLoginSuccess = { user -> screen = routeForUser(user) }
        )

        AppScreen.Onboarding -> OnboardingScreen(
            onJoined = { screen = AppScreen.StudentHome }
        )

        AppScreen.StudentHome -> HomeScreen()

        AppScreen.TeacherHome -> TeacherRoomsScreen(
            onBack = {
                session.logout()
                screen = AppScreen.Login
            }
        )

        is AppScreen.Unsupported -> UnsupportedRoleScreen(
            role = current.role,
            onLogout = {
                session.logout()
                screen = AppScreen.Login
            }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(NovaBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = NovaPurple)
    }
}

// Coordinador y admin todavía no tienen wireframe de pantalla propia.
@Composable
private fun UnsupportedRoleScreen(role: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(120.dp))

        Text(
            text = "NOVA",
            color = NovaPurple,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Todavía no hay una pantalla para el rol \"$role\".",
            color = NovaTextSecondary,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}
