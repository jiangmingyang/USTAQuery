package com.usta.query

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.usta.query.ui.USTAQueryApp
import com.usta.query.ui.theme.USTAQueryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            USTAQueryTheme {
                USTAQueryApp()
            }
        }
    }
}
