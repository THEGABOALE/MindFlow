package com.mindflow.nova.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.ui.screens.home.HomeScreen
import com.mindflow.nova.ui.screens.teacher.TeacherRoomsScreen
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

private enum class AppRole { STUDENT, TEACHER }

/**
 * Raíz de la app. Todavía no hay pantalla de login (falta el wireframe), así
 * que el rol se elige con [RoleSelectorScreen] como placeholder temporal.
 * Cuando exista el login real, esto se reemplaza por la consulta al backend
 * (por correo/id) que decide si la persona es alumno o docente y la manda
 * directo a su home correspondiente — [HomeScreen] o [TeacherRoomsScreen].
 */
@Composable
fun NovaApp() {
    var role by remember { mutableStateOf<AppRole?>(null) }

    when (role) {
        null -> RoleSelectorScreen(
            onSelectStudent = { role = AppRole.STUDENT },
            onSelectTeacher = { role = AppRole.TEACHER }
        )

        AppRole.STUDENT -> HomeScreen()

        AppRole.TEACHER -> TeacherRoomsScreen(onBack = { role = null })
    }
}

@Composable
private fun RoleSelectorScreen(
    onSelectStudent: () -> Unit,
    onSelectTeacher: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NOVA",
            color = NovaPurple,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "¿Cómo querés entrar? (esto es temporal, hasta que tengamos el login)",
            color = NovaTextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        RoleOptionCard(
            title = "Soy alumno",
            subtitle = "Misiones, lecciones y progreso",
            icon = Icons.Rounded.Person,
            onClick = onSelectStudent
        )

        Spacer(modifier = Modifier.height(14.dp))

        RoleOptionCard(
            title = "Soy docente",
            subtitle = "Salas, alumnos y resultados",
            icon = Icons.Rounded.Groups,
            onClick = onSelectTeacher
        )
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NovaBorder),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = NovaLightPurple
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NovaPurple,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    color = NovaTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
