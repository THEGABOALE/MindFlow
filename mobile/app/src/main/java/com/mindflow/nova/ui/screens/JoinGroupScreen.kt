package com.mindflow.nova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindflow.nova.data.model.JoinGroupRequest
import com.mindflow.nova.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

private val NovaPurple = Color(0xFF82368C)
private val NovaBackground = Color(0xFFFFF8FD)
private val NovaText = Color(0xFF211A24)
private val NovaTextSecondary = Color(0xFF6F6473)

@Composable
fun JoinGroupScreen(
    onJoinSuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var studentName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unirse a NOVA",
            color = NovaPurple,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ingresá el código que te dio tu docente para acceder a tu grupo.",
            color = NovaTextSecondary,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del estudiante") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Código del grupo") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null

                            try {
                                val response = RetrofitClient.api.joinGroupByCode(
                                    JoinGroupRequest(
                                        code = code.trim(),
                                        studentName = "Gabriel Demo"
                                    )
                                )
                                if (response.isSuccessful) {
                                    val body = response.body()

                                    if (body?.status == "OK") {
                                        message = body.message
                                        onJoinSuccess()
                                    } else {
                                        message = body?.message ?: "No se pudo unir al grupo"
                                    }
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    message = if (errorBody.isNullOrEmpty()) {
                                        try {
                                            val jsonObject = JSONObject(errorBody)
                                            jsonObject.optString("message", "Error HTTP: ${response.code()}")
                                        } catch (e: Exception) {
                                            "Error HTTP: ${response.code()}"
                                        }
                                    } else {
                                        "Error HTTP: ${response.code()}"
                                    }
                                }
                            } catch (e: Exception) {
                                message = "Error de conexión: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    enabled = code.isNotBlank() && !isLoading
                ) {
                    Text(
                        text = if (isLoading) "Validando..." else "Entrar al grupo",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (message != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = message ?: "",
                        color = NovaText,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}