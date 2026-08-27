package com.mindflow.nova.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.ui.theme.NovaBorder
import com.mindflow.nova.ui.theme.NovaLightPurple
import com.mindflow.nova.ui.theme.NovaPurple
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import androidx.compose.ui.graphics.Color

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Perfil",
            color = NovaPurple,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "Información básica del estudiante",
            color = NovaTextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(1.dp, NovaBorder),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = NovaLightPurple
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = NovaPurple,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estudiante DEMO",
                    color = NovaText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ruta activa Primaria Alta",
                    color = NovaTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
