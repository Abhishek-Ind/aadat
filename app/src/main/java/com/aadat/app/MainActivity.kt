package com.aadat.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.aadat.app.data.repository.AuthRepository
import com.aadat.app.navigation.AppNavGraph
import com.aadat.app.navigation.Routes
import com.aadat.app.ui.theme.AadatTheme
import com.aadat.app.ui.theme.AppTheme
import com.aadat.app.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val theme by themeViewModel.theme.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()

            val isDark = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemDark
            }

            AadatTheme(darkTheme = isDark) {
                val navController = rememberNavController()

                val startDestination = remember {
                    when {
                        !authRepository.isLoggedIn() -> Routes.SIGN_IN
                        !authRepository.isEmailConfirmed() -> Routes.VERIFY_EMAIL
                        else -> Routes.DASHBOARD
                    }
                }

                AppNavGraph(
                    navController = navController,
                    authRepository = authRepository,
                    startDestination = startDestination
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
