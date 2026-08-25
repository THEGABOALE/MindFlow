package com.mindflow.nova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mindflow.nova.ui.screens.HomeScreen
import com.mindflow.nova.ui.screens.JoinGroupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isJoined by remember { mutableStateOf(false) }

            if (isJoined) {
                HomeScreen()
            } else {
                JoinGroupScreen(
                    onJoinSuccess = {
                        isJoined = true
                    }
                )
            }
        }
    }
}