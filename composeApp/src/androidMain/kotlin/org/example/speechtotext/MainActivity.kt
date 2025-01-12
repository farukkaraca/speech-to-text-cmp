package org.example.speechtotext

import App
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadKoinModules(
            module {
                single<Activity> { this@MainActivity }
            }
        )

        setContent {
            App(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false,
            )
        }
    }
}