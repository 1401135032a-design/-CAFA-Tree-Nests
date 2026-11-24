package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ai.ContentModerationService
import com.example.ai.configureAppCheck
import com.example.data.DeviceSessionManager
import com.example.data.YangmeiRepository
import com.example.ui.YangmeiApp
import com.example.viewmodel.YangmeiViewModel
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI, ViewModels, or Firebase operations
        configureAppCheck(this, intent)

        // STEP 2: Enable Edge-to-Edge and launch directly into live real-time campus community
        enableEdgeToEdge()
        setContent {
            val appContext = LocalContext.current.applicationContext
            val db = remember {
                FirebaseFirestore.getInstance(appContext.getString(R.string.firestore_database_id))
            }
            val sessionManager = remember { DeviceSessionManager(appContext) }
            val moderationService = remember { ContentModerationService(appContext) }
            val repository = remember {
                YangmeiRepository(
                    context = appContext,
                    db = db,
                    sessionManager = sessionManager,
                    moderationGateway = moderationService
                )
            }
            val viewModel: YangmeiViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        YangmeiViewModel(repository = repository)
                    }
                }
            )
            YangmeiApp(viewModel = viewModel)
        }
    }
}
