package com.mindflow.nova.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.BuildConfig
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaGold
import com.mindflow.nova.ui.theme.NovaGoldLight
import com.mindflow.nova.ui.theme.NovaHeroGradient
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaSoftPurple
import com.mindflow.nova.ui.theme.NovaSurface
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun ProfileScreen(
    progress: StudentProgress?,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    var user by remember { mutableStateOf<SessionUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getMe()
            if (response.isSuccessful) {
                user = response.body()?.user
            }
        } catch (e: Exception) {
            // Sin conexión: se queda con los datos por defecto de abajo.
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Perfil",
                color = NovaPurple,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Tu cuenta y tus preferencias",
                color = NovaTextSecondary,
                fontSize = 14.sp
            )
        }

        ProfileHeaderCard(user = user, isLoading = isLoading)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStat(
                value = progress?.missionsCompleted ?: 0,
                label = "misiones completadas",
                accentColor = NovaPurple,
                backgroundColor = NovaLightPurple,
                modifier = Modifier.weight(1f)
            )

            ProfileStat(
                value = progress?.totalPoints ?: 0,
                label = "semillas",
                accentColor = NovaGold,
                backgroundColor = NovaGoldLight,
                modifier = Modifier.weight(1f)
            )
        }

        user?.let { AccountInfoCard(it) }

        AppearanceCard(darkMode = darkMode, onDarkModeChange = onDarkModeChange)

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NovaText),
            border = BorderStroke(1.dp, NovaBorder)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold)
        }

        Text(
            text = "NOVA · versión ${BuildConfig.VERSION_NAME}",
            modifier = Modifier.fillMaxWidth(),
            color = NovaTextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ProfileHeaderCard(user: SessionUser?, isLoading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = NovaSurface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(NovaHeroGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.let { initialsOf(it.fullName) }.orEmpty(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.fullName ?: if (isLoading) "Cargando..." else "Estudiante",
                color = NovaText,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileChip(text = "Estudiante")
                ProfileChip(text = user?.group?.name ?: "Sin sala asignada")
            }
        }
    }
}

@Composable
private fun ProfileChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NovaLightPurple
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = NovaPurple,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileStat(
    value: Int,
    label: String,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                color = accentColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = label,
                color = NovaTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AccountInfoCard(user: SessionUser) {
    val group = user.group

    val rows = buildList {
        user.loginId?.let { add(InfoRowData(Icons.Rounded.Badge, "Usuario", it)) }
        user.email?.let { add(InfoRowData(Icons.Rounded.Email, "Correo", it)) }
        group?.let {
            add(InfoRowData(Icons.Rounded.Groups, "Sala", it.name))
            add(
                InfoRowData(
                    Icons.Rounded.School,
                    "Grado",
                    listOfNotNull(it.grade, it.section?.let { s -> "Sección $s" }).joinToString(" · ")
                )
            )
            add(InfoRowData(Icons.Rounded.CalendarMonth, "Año escolar", it.schoolYear.toString()))
        }
    }

    if (rows.isEmpty()) return

    SectionTitle("Mi información")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovaSurface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            rows.forEachIndexed { index, row ->
                InfoRow(row)

                if (index < rows.lastIndex) {
                    HorizontalDivider(color = NovaBorder)
                }
            }
        }
    }
}

private data class InfoRowData(val icon: ImageVector, val label: String, val value: String)

@Composable
private fun InfoRow(row: InfoRowData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = row.icon)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.label,
                color = NovaTextSecondary,
                fontSize = 12.sp
            )

            Text(
                text = row.value,
                color = NovaText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AppearanceCard(darkMode: Boolean, onDarkModeChange: (Boolean) -> Unit) {
    SectionTitle("Apariencia")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = NovaSurface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(icon = if (darkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Modo oscuro",
                    color = NovaText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (darkMode) "Activado" else "Desactivado",
                    color = NovaTextSecondary,
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = darkMode,
                onCheckedChange = onDarkModeChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NovaPurple,
                    uncheckedThumbColor = NovaTextSecondary,
                    uncheckedTrackColor = NovaSoftPurple,
                    uncheckedBorderColor = NovaBorder
                )
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = NovaText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black
    )
}

@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NovaLightPurple),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NovaPurple,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Iniciales para el avatar: primera letra de las dos primeras palabras del nombre. */
internal fun initialsOf(fullName: String): String =
    fullName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
