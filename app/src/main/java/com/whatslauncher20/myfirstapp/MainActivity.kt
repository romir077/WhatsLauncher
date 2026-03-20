package com.whatslauncher20.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.whatslauncher20.myfirstapp.ui.MainScreen
import com.whatslauncher20.myfirstapp.ui.theme.WhatsLauncherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatsLauncherTheme {
                MainScreen()
            }
        }
    }
}
