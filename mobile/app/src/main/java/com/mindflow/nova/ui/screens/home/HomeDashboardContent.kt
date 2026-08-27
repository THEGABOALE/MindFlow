package com.mindflow.nova.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.ui.components.NovaProgressBar
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBlue
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaDark
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun HomeDashboardContent(
    level: LevelResponse,
    modifier: Modifier = Modifier
) {
    val previewMissions = level.missions.take(2)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Bienvenido a NOVA",
                color = NovaText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
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
                subtitle = "Completa retos y desbloquea nuevas lecciones"
            )
        }

        previewMissions.forEachIndexed { index, mission ->
            item {
                MissionPreviewRow(
                    mission = mission,
                    index = index,
                    isCompleted = index == 0
                )
            }
        }

        item {
            MotivationCard()
            Spacer(modifier = Modifier.height(22.dp))
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

            MascotaBadge()
        }
    }
}

@Composable
private fun MascotaBadge() {
    Surface(
        modifier = Modifier.size(86.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Mascota",
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
        border = BorderStroke(1.dp, NovaBorder),
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
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = NovaText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = subtitle,
            color = NovaTextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MissionPreviewRow(
    mission: com.mindflow.nova.data.model.MissionResponse,
    index: Int,
    isCompleted: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NovaBorder),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MissionPreviewIndicator(index = index, isCompleted = isCompleted)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${index + 1}. ${mission.title}",
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mission.description ?: "Misión educativa de NOVA",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun MissionPreviewIndicator(index: Int, isCompleted: Boolean) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isCompleted) NovaLightPurple else NovaDark,
        border = if (isCompleted) BorderStroke(1.5.dp, NovaPurple) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Misión completada",
                    tint = NovaPurple
                )
            } else {
                Text(
                    text = "%02d".format(index + 1),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
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
        border = BorderStroke(1.dp, NovaBorder),
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
