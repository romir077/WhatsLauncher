package com.whatslauncher20.myfirstapp

import android.content.Intent
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

        val sharedPhone = extractSharedText(intent)

        setContent {
            WhatsLauncherTheme {
                MainScreen(sharedPhoneNumber = sharedPhone)
            }
        }
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            return intent.getStringExtra(Intent.EXTRA_TEXT)
        }
        return null
    }
}
