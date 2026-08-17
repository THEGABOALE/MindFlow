package com.mindflow.nova.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.data.remote.RetrofitClient

private val NovaPurple = Color(0xFF82368C)
private val NovaBlue = Color(0xFF1600B5)
private val NovaLightPurple = Color(0xFFF5ECF7)
private val NovaSoftPurple = Color(0xFFE9D8EE)
private val NovaText = Color(0xFF211A24)
private val NovaTextSecondary = Color(0xFF6F6473)
private val NovaBackground = Color(0xFFFCF8FD)
private val NovaBorder = Color(0xFFE7D8EA)

@Composable
fun HomeScreen() {
    var levels by remember { mutableStateOf<List<LevelResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(NovaTab.Home) }
    var selectedMissionForGame by remember { mutableStateOf<MissionResponse?>(null) }

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
    if (selectedMissionForGame != null) {
        MiniGamePlaceholderScreen(
            mission = selectedMissionForGame!!,
            onBack = {
                selectedMissionForGame = null
            }
        )
        return
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
                    onMissionSelected = { mission ->
                        selectedMissionForGame = mission
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

private enum class NovaTab {
    Home,
    Lessons,
    Progress,
    Profile
}

@Composable
private fun NovaMainContent(
    selectedTab: NovaTab,
    level: LevelResponse,
    onMissionSelected: (MissionResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    when (selectedTab) {
        NovaTab.Home -> {
            HomeRouteContent(
                level = level,
                modifier = modifier
            )
        }

        NovaTab.Lessons -> {
            LessonsContent(
                level = level,
                onMissionSelected = onMissionSelected,
                modifier = modifier
            )
        }

        NovaTab.Progress -> {
            ProgressContent(
                level = level,
                modifier = modifier
            )
        }

        NovaTab.Profile -> {
            ProfileContent(
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HomeRouteContent(
    level: LevelResponse,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            NovaHeader()
        }

        item {
            WelcomeBanner()
        }

        item {
            CurrentLevelCard(level = level)
        }

        item {
            SectionHeader(
                title = "Ruta de aprendizaje",
                points = level.missions.sumOf { it.pointsReward }
            )
        }

        itemsIndexed(level.missions) { index, mission ->
            MissionPathRow(
                mission = mission,
                index = index,
                total = level.missions.size
            )
        }

        item {
            MotivationCard()
            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Composable
private fun NovaHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "☰",
            fontSize = 28.sp,
            color = NovaText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(14.dp),
            color = NovaPurple
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "◖",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "NOVA",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = NovaPurple
            )
            Text(
                text = "Aplicación educativa",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = NovaTextSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder)
        ) {
            Text(
                text = "120 pts",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = NovaPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun WelcomeBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = NovaPurple,
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Bienvenido a NOVA",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Avanza en misiones sobre igualdad, justicia, dignidad y respeto.",
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            SafiroBadge()
        }
    }
}

@Composable
private fun SafiroBadge() {
    Surface(
        modifier = Modifier.size(86.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.35f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Safiro",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CurrentLevelCard(level: LevelResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                color = NovaLightPurple
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "01",
                        color = NovaPurple,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = level.name,
                    color = NovaText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = level.description ?: "Nivel educativo disponible",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                NovaProgressBar(progress = 0.25f)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "25%",
                color = NovaPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun NovaProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFFEDE6EF))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50.dp))
                .background(NovaPurple)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    points: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = NovaText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Completa retos y desbloquea nuevas lecciones",
                color = NovaTextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder)
        ) {
            Text(
                text = "$points pts",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = NovaBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MissionPathRow(
    mission: MissionResponse,
    index: Int,
    total: Int
) {
    val isCompleted = index == 0
    val isCurrent = index == 1
    val isLocked = index > 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 142.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(92.dp)
                .height(142.dp),
            contentAlignment = Alignment.Center
        ) {
            if (index > 0) {
                VerticalConnector(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .height(48.dp)
                )
            }

            if (index < total - 1) {
                VerticalConnector(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(48.dp)
                )
            }

            MissionNode(
                number = index + 1,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                isLocked = isLocked
            )
        }

        MissionInfoCard(
            mission = mission,
            index = index,
            isCompleted = isCompleted,
            isCurrent = isCurrent,
            isLocked = isLocked
        )
    }
}

@Composable
private fun VerticalConnector(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.width(8.dp)) {
        drawLine(
            color = NovaSoftPurple,
            start = center.copy(y = 0f),
            end = center.copy(y = size.height),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun MissionNode(
    number: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean
) {
    val nodeSize = if (isCurrent) 82.dp else 70.dp

    val background = when {
        isCompleted -> NovaPurple
        isCurrent -> NovaBlue
        isLocked -> Color(0xFFD9D2DC)
        else -> NovaPurple
    }

    val border = when {
        isCurrent -> NovaPurple
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .size(nodeSize)
            .clip(CircleShape)
            .background(background)
            .border(5.dp, border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                isCompleted -> "✓"
                isLocked -> "•"
                else -> number.toString()
            },
            color = Color.White,
            fontSize = if (isCurrent) 28.sp else 24.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun MissionInfoCard(
    mission: MissionResponse,
    index: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean
) {
    val statusText = when {
        isCompleted -> "Completada"
        isCurrent -> "En curso"
        else -> "Próxima misión"
    }

    val statusBackground = when {
        isCompleted -> Color(0xFFF2E5F4)
        isCurrent -> Color(0xFFEDEAFF)
        else -> Color(0xFFF0EDF2)
    }

    val statusColor = when {
        isCompleted -> NovaPurple
        isCurrent -> NovaBlue
        else -> NovaTextSecondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isCurrent) NovaPurple.copy(alpha = 0.45f) else NovaBorder
        ),
        shadowElevation = if (isCurrent) 4.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = statusBackground
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${index + 1}. ${mission.title}",
                color = if (isLocked) Color(0xFF6F6673) else NovaText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = mission.description ?: "Misión educativa de NOVA",
                color = NovaTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isLocked) Color(0xFFF0EDF2) else NovaLightPurple
            ) {
                Text(
                    text = "+${mission.pointsReward} puntos",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = if (isLocked) NovaTextSecondary else NovaPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MotivationCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(18.dp),
                color = NovaLightPurple
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "✦",
                        color = NovaPurple,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sigue transformando tu aprendizaje",
                    color = NovaPurple,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Completa misiones para avanzar en tu ruta educativa.",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Text(
                text = "›",
                color = NovaBlue,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
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
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder)
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

@Composable
private fun LessonsContent(
    level: LevelResponse,
    onMissionSelected: (MissionResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lecciones",
                color = NovaPurple,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Contenido disponible para ${level.name}",
                color = NovaTextSecondary,
                fontSize = 14.sp
            )
        }

        itemsIndexed(level.missions) { index, mission ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onMissionSelected(mission)
                    },
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = NovaLightPurple
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                color = NovaPurple,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mission.title,
                            color = NovaText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = mission.description ?: "Lección educativa de NOVA",
                            color = NovaTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "+${mission.pointsReward} puntos",
                            color = NovaPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProgressContent(
    level: LevelResponse,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Progreso",
            color = NovaPurple,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Resumen de tu avance en NOVA",
            color = NovaTextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = level.name,
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Has iniciado tu ruta de aprendizaje sobre derechos, igualdad y dignidad.",
                    color = NovaTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                NovaProgressBar(progress = 0.25f)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "25% completado",
                    color = NovaPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressStatCard(
                title = "Misiones",
                value = level.missions.size.toString(),
                modifier = Modifier.weight(1f)
            )

            ProgressStatCard(
                title = "Puntos",
                value = level.missions.sumOf { it.pointsReward }.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProgressStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = NovaLightPurple,
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = NovaPurple,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = title,
                color = NovaTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Perfil",
            color = NovaPurple,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Información básica del estudiante",
            color = NovaTextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = NovaPurple
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estudiante Demo",
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ruta activa: Primaria alta",
                    color = NovaTextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = NovaLightPurple
                ) {
                    Text(
                        text = "Aprendiendo con Safiro",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = NovaPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniGamePlaceholderScreen(
    mission: MissionResponse,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Minijuego",
                color = NovaPurple,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mission.title,
                color = NovaText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mission.description ?: "Pantalla reservada para el desarrollo del minijuego.",
                color = NovaTextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Área en blanco para minijuego",
                        color = NovaTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaPurple,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Volver a lecciones",
                fontWeight = FontWeight.Bold
            )
        }
    }
}