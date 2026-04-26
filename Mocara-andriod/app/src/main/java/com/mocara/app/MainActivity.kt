package com.mocara.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mocara.app.data.auth.TokenManager
import com.mocara.app.data.remote.RetrofitClient
import com.mocara.app.di.AppContainer
import com.mocara.app.navigation.AppNavGraph
import com.mocara.app.navigation.Screen
import com.mocara.app.ui.theme.MocaraTheme
import kotlinx.coroutines.runBlocking

/**
 * MainActivity - App's single Activity
 * - Hosts Compose Navigation
 * - Entry point for the application
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tokenManager = TokenManager(applicationContext)
        AppContainer.init(tokenManager)
        RetrofitClient.init(tokenManager)
        val hasToken = runBlocking { !tokenManager.getAccessToken().isNullOrBlank() }

        setContent {
            MocaraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        startDestination = if (hasToken) Screen.Scanner.route else Screen.Login.route
                    )
                }
            }
        }
    }
}