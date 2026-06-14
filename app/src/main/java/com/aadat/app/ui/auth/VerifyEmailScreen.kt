package com.aadat.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    onEmailConfirmed: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var resendCooldown by remember { mutableStateOf(0) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    val email = remember { viewModel.getCurrentUserEmail() }

    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📧", fontSize = 64.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Check your inbox",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We sent a confirmation link to $email. Tap it to activate your account.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            confirmError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (viewModel.checkEmailConfirmed()) {
                        onEmailConfirmed()
                    } else {
                        confirmError = "Email not confirmed yet. Please check your inbox."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("I've confirmed — Continue", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    viewModel.resendConfirmationEmail(email)
                    resendCooldown = 60
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = resendCooldown == 0 && !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend email")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToSignIn) {
                Text("Sign in with a different account")
            }
        }
    }
}
