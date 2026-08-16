package com.mindflow.nova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindflow.nova.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var message by remember { mutableStateOf("Sin probar conexión") }
            val scope = rememberCoroutineScope()

            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = message)

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = RetrofitClient.api.getDatabaseHealth()

                                message = if (response.isSuccessful) {
                                    "Conectado: ${response.body()?.message}"
                                } else {
                                    "Error HTTP: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                message = "Error de conexión: ${e.message}"
                            }
                        }
                    }
                ) {
                    Text("Probar conexión")
                }
            }
        }
    }
}