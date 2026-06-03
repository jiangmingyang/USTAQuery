package com.ustaquery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ustaquery.ui.USTAQueryApp
import com.ustaquery.ui.theme.USTAQueryTheme

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
