package com.mindflow.nova.ui.screens.lessons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindflow.nova.data.offline.EXTRA_TIME_COST
import com.mindflow.nova.data.offline.EXTRA_TIME_SECONDS
import com.mindflow.nova.ui.theme.NovaGold
import com.mindflow.nova.ui.theme.NovaGoldLight
import com.mindflow.nova.ui.theme.NovaNeutralCard
import com.mindflow.nova.ui.theme.NovaTextSecondary

// Potenciador "+30 s" de las misiones con reloj: se compra con semillas de la
// cuenta, una vez por intento, cuando el tiempo ya se está acabando.

/** Segundos que quedan cuando aparece el botón: antes no hace falta. */
private const val SHOW_EXTRA_TIME_AT_SECONDS = 15

internal fun showExtraTime(secondsLeft: Int, alreadyUsed: Boolean): Boolean =
    !alreadyUsed && secondsLeft in 1..SHOW_EXTRA_TIME_AT_SECONDS

/** El precio si le alcanza; si no, cuántas semillas le faltan, para que tenga una meta. */
internal fun extraTimeLabel(balance: Int): String =
    if (balance >= EXTRA_TIME_COST) {
        "+$EXTRA_TIME_SECONDS s · $EXTRA_TIME_COST semillas"
    } else {
        "Te faltan ${EXTRA_TIME_COST - balance} semillas para +$EXTRA_TIME_SECONDS s"
    }

/** Nota de los cierres cuando el intento usó el potenciador; null si no gastó nada. */
internal fun spentNotice(seedsSpent: Int): String? =
    if (seedsSpent > 0) "Usaste +$EXTRA_TIME_SECONDS s: −$seedsSpent semillas" else null

@Composable
internal fun ExtraTimeButton(balance: Int, onBuy: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onBuy,
        enabled = balance >= EXTRA_TIME_COST,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NovaGoldLight,
            contentColor = NovaGold,
            disabledContainerColor = NovaNeutralCard,
            disabledContentColor = NovaTextSecondary
        )
    ) {
        Text(text = extraTimeLabel(balance), fontWeight = FontWeight.Bold)
    }
}
