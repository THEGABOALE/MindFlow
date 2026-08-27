package com.mindflow.nova.ui.screens.lessons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun MiniGamePlaceholderScreen(
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
                border = BorderStroke(1.dp, NovaBorder)
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
