package com.mindflow.nova.ui.screens.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.ui.components.NovaProgressBar
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun ProgressScreen(
    level: LevelResponse,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf<StudentProgress?>(null) }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.api.getMe()
            val userId = me.body()?.user?.id

            if (me.isSuccessful && userId != null) {
                val response = RetrofitClient.api.getStudentProgress(userId)
                if (response.isSuccessful) {
                    progress = response.body()?.student
                }
            }
        } catch (e: Exception) {
            // Sin conexión: se queda con los valores en 0 de abajo.
        }
    }

    val levelProgress = progress?.levels?.firstOrNull { it.id == level.id }
    val progressFraction = ((levelProgress?.progressPercentage ?: 0.0) / 100.0).toFloat()

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
            border = BorderStroke(1.dp, NovaBorder),
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
                    text = "Has iniciado tu ruta de aprendizaje sobre los derechos, igualdad y dignidad.",
                    color = NovaTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                NovaProgressBar(progress = progressFraction)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${(progressFraction * 100).toInt()}% completado",
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
                title = "misiones completadas",
                value = (progress?.missionsCompleted ?: 0).toString(),
                modifier = Modifier.weight(1f)
            )

            ProgressStatCard(
                title = "semillas",
                value = (progress?.totalPoints ?: 0).toString(),
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
        border = BorderStroke(1.dp, NovaBorder)
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
