package com.mindflow.nova.ui.screens.lessons.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mindflow.nova.ui.components.NovaProgressBar
import com.mindflow.nova.ui.theme.NovaBlue
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary

/**
 * Piezas compartidas por las distintas mecánicas de lección (opción múltiple,
 * relación de conceptos, verdadero/falso): barra superior con plumas y progreso,
 * diálogo de salida, y las pantallas de cierre (nivel completado / bloqueado).
 */

const val LESSON_MAX_PLUMAS = 3
const val LESSON_SEMILLAS_REWARD = 50

@Composable
fun MascotaPlaceholder(modifier: Modifier = Modifier, label: String = "Mascota") {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = NovaLightPurple
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = NovaPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlumasIndicator(plumas: Int, maxPlumas: Int = LESSON_MAX_PLUMAS) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(maxPlumas) { index ->
            val filled = index < plumas
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (filled) NovaPurple else Color.Transparent)
                    .border(1.5.dp, NovaPurple, CircleShape)
            )
        }
    }
}

@Composable
fun LessonTopBar(
    progress: Float,
    plumas: Int,
    justLostPluma: Boolean,
    onClose: () -> Unit,
    maxPlumas: Int = LESSON_MAX_PLUMAS,
    trailingBadge: (@Composable () -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Salir de la lección",
                tint = NovaText
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (trailingBadge != null) {
            trailingBadge()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            NovaProgressBar(progress = progress.coerceIn(0f, 1f))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            PlumasIndicator(plumas = plumas, maxPlumas = maxPlumas)

            Text(
                text = "plumas",
                color = NovaTextSecondary,
                fontSize = 10.sp
            )

            if (justLostPluma) {
                Text(
                    text = "-1 pluma",
                    color = NovaBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    onStay: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onStay) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Seguro que quieres salir?",
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Perderás el progreso de esta lección",
                    color = NovaTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Salir", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onStay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaText,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Seguir aquí", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LessonCompletedScreen(
    subtitle: String,
    rewardAmount: Int,
    onContinue: () -> Unit,
    title: String = "¡Nivel completado!",
    rewardLabel: String = "semillas",
    extraContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MascotaPlaceholder(modifier = Modifier.size(140.dp), label = "Mascota\n(celebrando)")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            color = NovaText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = NovaTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        if (extraContent != null) {
            Spacer(modifier = Modifier.height(16.dp))
            extraContent()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = NovaLightPurple
        ) {
            Text(
                text = "+$rewardAmount",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = NovaPurple,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = rewardLabel,
            color = NovaTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaPurple,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "Continuar", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Pantalla de cierre "bloqueante": sin plumas o se acabó el tiempo. Misma
 * estructura visual, solo cambia el texto y las acciones.
 */
@Composable
fun LessonEndScreen(
    title: String,
    message: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MascotaPlaceholder(modifier = Modifier.size(140.dp), label = "Mascota\n(sin plumas)")

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = title,
            color = NovaText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            color = NovaTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onPrimary,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovaPurple,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = primaryLabel, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSecondary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = secondaryLabel, fontWeight = FontWeight.Bold)
        }
    }
}
