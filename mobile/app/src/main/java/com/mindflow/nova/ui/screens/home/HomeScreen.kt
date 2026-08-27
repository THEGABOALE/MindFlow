package com.mindflow.nova.ui.screens.home

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
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.ui.screens.lessons.LessonMockData
import com.mindflow.nova.ui.screens.lessons.LessonPlayScreen
import com.mindflow.nova.ui.screens.lessons.LessonsMapScreen
import com.mindflow.nova.ui.screens.lessons.MatchingLessonScreen
import com.mindflow.nova.ui.screens.lessons.MatchingMockData
import com.mindflow.nova.ui.screens.lessons.MiniGamePlaceholderScreen
import com.mindflow.nova.ui.screens.lessons.TrueFalseLessonScreen
import com.mindflow.nova.ui.screens.lessons.TrueFalseMockData
import com.mindflow.nova.ui.screens.profile.ProfileScreen
import com.mindflow.nova.ui.screens.progress.ProgressScreen
import com.mindflow.nova.ui.screens.teacher.TeacherRoomsScreen
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun HomeScreen() {
    var levels by remember { mutableStateOf<List<LevelResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(NovaTab.Home) }
    var activeLesson by remember { mutableStateOf<LessonRoute?>(null) }
    var showTeacherPanel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getLevels()

            if (response.isSuccessful) {
                levels = response.body().orEmpty()
            } else {
                errorMessage = "Error HTTP: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    if (showTeacherPanel) {
        TeacherRoomsScreen(onBack = { showTeacherPanel = false })
        return
    }
    when (val route = activeLesson) {
        is LessonRoute.MultipleChoice -> {
            LessonPlayScreen(
                mission = route.mission,
                questions = LessonMockData.lessonOneQuestions,
                onExit = { activeLesson = null }
            )
            return
        }

        is LessonRoute.Matching -> {
            MatchingLessonScreen(
                mission = route.mission,
                pairs = MatchingMockData.reconocerDerechosPairs,
                onExit = { activeLesson = null }
            )
            return
        }

        is LessonRoute.TrueFalse -> {
            TrueFalseLessonScreen(
                mission = route.mission,
                questions = TrueFalseMockData.decisionesConRespetoQuestions,
                onExit = { activeLesson = null }
            )
            return
        }

        is LessonRoute.Placeholder -> {
            MiniGamePlaceholderScreen(
                mission = route.mission,
                onBack = { activeLesson = null }
            )
            return
        }

        null -> Unit
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
            isLoading -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            errorMessage != null -> {
                ErrorState(
                    message = errorMessage ?: "Error desconocido",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            levels.isEmpty() -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            else -> {
                NovaMainContent(
                    selectedTab = selectedTab,
                    level = levels.first(),
                    onMissionSelected = { mission -> activeLesson = routeForMission(mission) },
                    onOpenTeacherPanel = { showTeacherPanel = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

internal enum class NovaTab {
    Home,
    Lessons,
    Progress,
    Profile
}

/**
 * A qué pantalla lleva tocar una misión, según su mecánica. Mientras el
 * backend no exponga el tipo de contenido de cada misión, se resuelve por
 * orderIndex: 1 = opción múltiple, 2 = relación de conceptos, 3 = verdadero/
 * falso, el resto usa el placeholder de minijuego.
 */
private sealed class LessonRoute {
    data class MultipleChoice(val mission: MissionResponse) : LessonRoute()
    data class Matching(val mission: MissionResponse) : LessonRoute()
    data class TrueFalse(val mission: MissionResponse) : LessonRoute()
    data class Placeholder(val mission: MissionResponse) : LessonRoute()
}

private fun routeForMission(mission: MissionResponse): LessonRoute = when (mission.orderIndex) {
    LessonMockData.lessonOneMissionOrderIndex -> LessonRoute.MultipleChoice(mission)
    2 -> LessonRoute.Matching(mission)
    3 -> LessonRoute.TrueFalse(mission)
    else -> LessonRoute.Placeholder(mission)
}

@Composable
private fun NovaMainContent(
    selectedTab: NovaTab,
    level: LevelResponse,
    onMissionSelected: (MissionResponse) -> Unit,
    onOpenTeacherPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (selectedTab) {
        NovaTab.Home -> {
            HomeDashboardContent(
                level = level,
                modifier = modifier
            )
        }

        NovaTab.Lessons -> {
            LessonsMapScreen(
                level = level,
                onMissionSelected = onMissionSelected,
                modifier = modifier
            )
        }

        NovaTab.Progress -> {
            ProgressScreen(
                level = level,
                modifier = modifier
            )
        }

        NovaTab.Profile -> {
            ProfileScreen(
                onOpenTeacherPanel = onOpenTeacherPanel,
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
        containerColor = Color.White,
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
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
            text = "No hay niveles disponibles",
            color = NovaTextSecondary,
            fontSize = 16.sp
        )
    }
}
