package com.mindflow.nova.ui.screens.teacher

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

/**
 * Panel docente: lista de Salas y, dentro de cada una, el perfil del maestro
 * con pestañas por grado (roster de alumnos) y una pestaña de Resumen con el
 * porcentaje de acierto por minijuego. Todo sobre datos mock — no hay
 * backend de salones/alumnos todavía.
 */
@Composable
fun TeacherRoomsScreen(onBack: () -> Unit) {
    var selectedRoom by remember { mutableStateOf<TeacherRoom?>(null) }

    val room = selectedRoom
    if (room != null) {
        TeacherRoomDetailScreen(
            room = room,
            onBack = { selectedRoom = null }
        )
    } else {
        SalasListScreen(
            rooms = TeacherMockData.rooms,
            onBack = onBack,
            onRoomSelected = { selectedRoom = it }
        )
    }
}

@Composable
private fun SalasListScreen(
    rooms: List<TeacherRoom>,
    onBack: () -> Unit,
    onRoomSelected: (TeacherRoom) -> Unit
) {
    Scaffold(containerColor = NovaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = NovaText
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Salas",
                    color = NovaText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rooms.forEach { room ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoomSelected(room) },
                        shape = RoundedCornerShape(16.dp),
                        color = room.accentColor
                    ) {
                        Text(
                            text = room.name,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            color = NovaText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherRoomDetailScreen(
    room: TeacherRoom,
    onBack: () -> Unit
) {
    var selectedTab by remember(room.id) { mutableStateOf(0) }
    val tabLabels = room.gradeLabels + "Resumen"

    Scaffold(containerColor = NovaBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Volver a salas",
                        tint = NovaText
                    )
                }

                Text(
                    text = "perfil maestro",
                    color = NovaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = NovaLightPurple
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = NovaPurple,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = room.teacherName,
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(value = room.studentCount, label = "Cantidad de alumnos")
                    ProfileStat(value = room.roomsAssigned, label = "Salas asignadas")
                    ProfileStat(value = room.minigamesScheduled, label = "Mini juegos programados")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, NovaBorder)
                ) {
                    Text(
                        text = room.levelLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        color = NovaText,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabLabels) { label ->
                    val index = tabLabels.indexOf(label)
                    val isSelected = index == selectedTab
                    Surface(
                        modifier = Modifier.clickable { selectedTab = index },
                        shape = RoundedCornerShape(50.dp),
                        color = if (isSelected) NovaPurple else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, NovaBorder)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White else NovaTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == tabLabels.lastIndex) {
                ResumenList(results = room.minigameResults)
            } else {
                RosterList(students = room.students)
            }
        }
    }
}

@Composable
private fun ProfileStat(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), color = NovaText, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(
            text = label,
            color = NovaTextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
private fun RosterList(students: List<TeacherStudent>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(students) { student ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = NovaLightPurple
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = NovaPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = student.name,
                    modifier = Modifier.weight(1f),
                    color = NovaText,
                    fontSize = 13.sp
                )

                if (student.elapsedTime != null) {
                    Text(
                        text = student.elapsedTime,
                        color = NovaTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = NovaTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ResumenList(results: List<MinigameResult>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Nombres de mini juego",
                modifier = Modifier.weight(1f),
                color = NovaTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = "porcentaje por salón",
                color = NovaTextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            results.forEach { result ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = NovaLightPurple
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.name,
                            modifier = Modifier.weight(1f),
                            color = NovaPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${result.percentage}%",
                            color = NovaPurple,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
