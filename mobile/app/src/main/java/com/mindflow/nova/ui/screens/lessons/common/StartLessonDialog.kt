package com.mindflow.nova.ui.screens.lessons.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mindflow.nova.data.model.MissionResponse
import com.mindflow.nova.ui.screens.lessons.mechanicLabel
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

@Composable
fun MechanicChip(mechanic: String?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = NovaLightPurple
    ) {
        Text(
            text = mechanicLabel(mechanic),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = NovaPurple,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Confirmación antes de entrar a una misión: una tarjeta centrada con la
 * pregunta y el botón, para que no se abra una lección por un toque accidental.
 */
@Composable
fun StartLessonDialog(
    mission: MissionResponse,
    isReplay: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MechanicChip(mechanic = mission.mechanic)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isReplay) "¿Quieres repasar la lección?" else "¿Quieres comenzar la lección?",
                    color = NovaText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = mission.title,
                    color = NovaPurple,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                mission.description?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        color = NovaTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isReplay) "Repasar" else "Comenzar",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Ahora no",
                        color = NovaTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
