package com.aadat.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aadat.app.data.repository.AuthRepository
import com.aadat.app.ui.auth.ForgotPasswordScreen
import com.aadat.app.ui.auth.SignInScreen
import com.aadat.app.ui.auth.SignUpScreen
import com.aadat.app.ui.auth.VerifyEmailScreen
import com.aadat.app.ui.dashboard.DashboardScreen
import com.aadat.app.ui.habit.HabitDetailScreen
import com.aadat.app.ui.profile.ProfileScreen

object Routes {
    const val SIGN_IN = "auth/signin"
    const val SIGN_UP = "auth/signup"
    const val VERIFY_EMAIL = "auth/verify-email"
    const val FORGOT_PASSWORD = "auth/forgot-password"
    const val DASHBOARD = "dashboard"
    const val HABIT_DETAIL = "habit/{habitId}"
    const val PROFILE = "profile"

    fun habitDetail(habitId: String) = "habit/$habitId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authRepository: AuthRepository,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Routes.VERIFY_EMAIL) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                },
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }

        composable(Routes.VERIFY_EMAIL) {
            VerifyEmailScreen(
                onEmailConfirmed = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToHabitDetail = { habitId ->
                    navController.navigate(Routes.habitDetail(habitId))
                },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onSignOut = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HABIT_DETAIL) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitDetailScreen(
                habitId = habitId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
