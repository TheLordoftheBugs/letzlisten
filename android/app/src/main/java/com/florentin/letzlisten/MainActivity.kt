package com.florentin.letzlisten

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.florentin.letzlisten.player.RadioViewModel
import com.florentin.letzlisten.ui.MainScreen
import com.florentin.letzlisten.ui.theme.LetzListenTheme

class MainActivity : ComponentActivity() {

    private val radioViewModel: RadioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetzListenTheme {
                MainScreen(viewModel = radioViewModel)
            }
        }
    }
}
