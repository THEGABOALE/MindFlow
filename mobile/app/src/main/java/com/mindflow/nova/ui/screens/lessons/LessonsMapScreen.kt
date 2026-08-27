package com.mindflow.nova.ui.screens.lessons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.ui.theme.NovaBlue
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaDark
import com.mindflow.nova.ui.theme.NovaLocked
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaSoftPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun LessonsMapScreen(
    level: LevelResponse,
    onMissionSelected: (MissionResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    val missions = level.missions
    val total = missions.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lecciones",
                color = NovaText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Contenido disponible para ${level.name}",
                color = NovaTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        itemsIndexed(missions.reversed()) { reversedIndex, mission ->
            val index = total - 1 - reversedIndex
            LessonPathNodeRow(
                mission = mission,
                index = index,
                total = total,
                onMissionSelected = onMissionSelected
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LessonPathNodeRow(
    mission: MissionResponse,
    index: Int,
    total: Int,
    onMissionSelected: (MissionResponse) -> Unit
) {
    val isCompleted = index == 0
    val isCurrent = index == 1
    val isLocked = index > 1
    val alignRight = index % 2 == 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 142.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignRight) Arrangement.End else Arrangement.Start
    ) {
        val node = @Composable {
            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(142.dp),
                contentAlignment = Alignment.Center
            ) {
                if (index < total - 1) {
                    VerticalConnector(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .height(48.dp)
                    )
                }

                if (index > 0) {
                    VerticalConnector(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(48.dp)
                    )
                }

                LessonPathNode(
                    number = index + 1,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    isLocked = isLocked,
                    onClick = if (!isLocked) {
                        { onMissionSelected(mission) }
                    } else {
                        null
                    }
                )
            }
        }

        val card = @Composable {
            LessonPathInfoCard(
                mission = mission,
                index = index,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                isLocked = isLocked,
                modifier = Modifier.weight(1f)
            )
        }

        if (alignRight) {
            card()
            node()
        } else {
            node()
            card()
        }
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
private fun LessonPathNode(
    number: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    onClick: (() -> Unit)?
) {
    val nodeSize = if (isCurrent) 82.dp else 70.dp

    val background = when {
        isCompleted -> NovaPurple
        isCurrent -> NovaBlue
        isLocked -> NovaLocked
        else -> NovaDark
    }

    val border = if (isCurrent) NovaPurple else Color.White

    Box(
        modifier = Modifier
            .size(nodeSize)
            .clip(CircleShape)
            .background(background)
            .border(5.dp, border, CircleShape)
            .let { base ->
                if (onClick != null) base.clickable(onClick = onClick) else base
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                isCompleted -> "✓"
                isLocked -> "•"
                else -> "%02d".format(number)
            },
            color = Color.White,
            fontSize = if (isCurrent) 24.sp else 20.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LessonPathInfoCard(
    mission: MissionResponse,
    index: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        isCompleted -> "Completada"
        isCurrent -> "En curso"
        else -> "Próxima misión"
    }

    val statusBackground = when {
        isCompleted -> NovaSoftPurple
        isCurrent -> Color(0xFFEDEAFF)
        else -> Color(0xFFF0EDF2)
    }

    val statusColor = when {
        isCompleted -> NovaPurple
        isCurrent -> NovaBlue
        else -> NovaTextSecondary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(
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
                color = if (isLocked) NovaTextSecondary else NovaText,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = mission.description ?: "Misión educativa de NOVA",
                color = NovaTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
