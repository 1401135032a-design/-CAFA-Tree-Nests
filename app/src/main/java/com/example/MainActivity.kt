package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.ui.YangmeiApp
import com.example.viewmodel.YangmeiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase operations
        configureAppCheck(this, intent)

        // STEP 2: Enable Edge-to-Edge and render Compose UI directly without Google login gate
        enableEdgeToEdge()
        setContent {
            val viewModel: YangmeiViewModel = viewModel()
            YangmeiApp(viewModel = viewModel)
        }
    }
}
