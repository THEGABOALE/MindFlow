package com.mindflow.nova.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.ui.screens.lessons.LessonHost
import com.mindflow.nova.ui.screens.lessons.LessonsMapScreen
import com.mindflow.nova.ui.screens.lessons.common.StartLessonDialog
import com.mindflow.nova.ui.screens.profile.ProfileScreen
import com.mindflow.nova.ui.screens.progress.ProgressScreen
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaSurface
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun HomeScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit = {},
    viewModel: StudentHomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val progress = state.progress
    val user = state.user
    // El nivel de la sala del estudiante, no el primero de la lista: si no, uno
    // de secundaria veía y jugaba las misiones de primaria. Hasta saber quién
    // es no se elige ninguno.
    val level = user?.let { levelForStudent(state.levels, it) }

    var selectedTab by remember { mutableStateOf(NovaTab.Home) }
    var activeMission by remember { mutableStateOf<MissionResponse?>(null) }
    // Misión elegida que todavía espera la confirmación "¿Quieres comenzar?".
    var pendingMission by remember { mutableStateOf<MissionResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.start()
    }

    activeMission?.let { mission ->
        LessonHost(
            mission = mission,
            onExit = {
                activeMission = null
                // Al salir de una lección se vuelve a pedir el progreso, para que
                // la ruta refleje la misión recién completada sin reabrir la app.
                viewModel.refreshProgress()
            }
        )
        return
    }

    // "Atrás" del teléfono: desde otra pestaña vuelve al Inicio en vez de
    // cerrar la app; desde el Inicio sí deja que se cierre.
    BackHandler(enabled = selectedTab != NovaTab.Home) {
        selectedTab = NovaTab.Home
    }

    Scaffold(
        containerColor = NovaBackground,
        bottomBar = {
            NovaBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        when {
            state.errorMessage != null -> {
                ErrorState(
                    message = state.errorMessage ?: "Error desconocido",
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            state.accountFailed && user == null -> {
                ErrorState(
                    message = "No pudimos cargar tu cuenta. Revisa tu conexión e inténtalo de nuevo.",
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            // Se espera a la ruta y a saber quién es: si la ruta llega primero,
            // no se muestra un nivel que quizá no es el suyo.
            state.isLoading || user == null -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            level == null -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                NovaMainContent(
                    selectedTab = selectedTab,
                    level = level,
                    user = state.user,
                    progress = progress,
                    onMissionSelected = { mission -> pendingMission = mission },
                    onOpenLessons = { selectedTab = NovaTab.Lessons },
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = {
                        viewModel.clear()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }

    pendingMission?.let { mission ->
        StartLessonDialog(
            mission = mission,
            isReplay = mission.id in progress?.completedMissionIds.orEmpty(),
            onConfirm = {
                pendingMission = null
                activeMission = mission
            },
            onDismiss = { pendingMission = null }
        )
    }
}

internal enum class NovaTab {
    Home,
    Lessons,
    Progress,
    Profile
}

@Composable
private fun NovaMainContent(
    selectedTab: NovaTab,
    level: LevelResponse,
    user: SessionUser?,
    progress: StudentProgress?,
    onMissionSelected: (MissionResponse) -> Unit,
    onOpenLessons: () -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (selectedTab) {
        NovaTab.Home -> {
            HomeDashboardContent(
                level = level,
                progress = progress,
                onMissionSelected = onMissionSelected,
                onOpenLessons = onOpenLessons,
                modifier = modifier
            )
        }

        NovaTab.Lessons -> {
            LessonsMapScreen(
                level = level,
                completedMissionIds = progress?.completedMissionIds?.toSet().orEmpty(),
                onMissionSelected = onMissionSelected,
                modifier = modifier
            )
        }

        NovaTab.Progress -> {
            ProgressScreen(
                level = level,
                progress = progress,
                modifier = modifier
            )
        }

        NovaTab.Profile -> {
            ProfileScreen(
                user = user,
                progress = progress,
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                onLogout = onLogout,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun NovaBottomNavigation(
    selectedTab: NovaTab,
    onTabSelected: (NovaTab) -> Unit
) {
    NavigationBar(
        containerColor = NovaSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == NovaTab.Home,
            onClick = { onTabSelected(NovaTab.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Inicio"
                )
            },
            label = {
                Text(text = "Inicio")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NovaPurple,
                selectedTextColor = NovaPurple,
                indicatorColor = NovaLightPurple,
                unselectedIconColor = NovaTextSecondary,
                unselectedTextColor = NovaTextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == NovaTab.Lessons,
            onClick = { onTabSelected(NovaTab.Lessons) },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = "Lecciones"
                )
            },
            label = {
                Text(text = "Lecciones")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NovaPurple,
                selectedTextColor = NovaPurple,
                indicatorColor = NovaLightPurple,
                unselectedIconColor = NovaTextSecondary,
                unselectedTextColor = NovaTextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == NovaTab.Progress,
            onClick = { onTabSelected(NovaTab.Progress) },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Timeline,
                    contentDescription = "Progreso"
                )
            },
            label = {
                Text(text = "Progreso")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NovaPurple,
                selectedTextColor = NovaPurple,
                indicatorColor = NovaLightPurple,
                unselectedIconColor = NovaTextSecondary,
                unselectedTextColor = NovaTextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == NovaTab.Profile,
            onClick = { onTabSelected(NovaTab.Profile) },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Perfil"
                )
            },
            label = {
                Text(text = "Perfil")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NovaPurple,
                selectedTextColor = NovaPurple,
                indicatorColor = NovaLightPurple,
                unselectedIconColor = NovaTextSecondary,
                unselectedTextColor = NovaTextSecondary
            )
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = NovaPurple)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cargando ruta de aprendizaje...",
                color = NovaTextSecondary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NovaSurface,
            border = BorderStroke(1.dp, NovaBorder)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No se pudo cargar NOVA",
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = NovaTextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = NovaPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Reintentar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Todavía no hay misiones para tu nivel",
            color = NovaTextSecondary,
            fontSize = 16.sp
        )
    }
}
