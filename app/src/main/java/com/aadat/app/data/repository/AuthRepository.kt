package com.aadat.app.data.repository

import com.aadat.app.data.remote.SupabaseClientProvider
import com.aadat.app.domain.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClientProvider: SupabaseClientProvider
) {
    private val auth get() = supabaseClientProvider.client.auth

    suspend fun signIn(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { auth.signOut() }
    }

    suspend fun resetPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { auth.resetPasswordForEmail(email) }
    }

    suspend fun changePassword(newPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { auth.updateUser { password = newPassword } }
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { auth.admin.deleteUser(auth.currentUserOrNull()?.id ?: throw Exception("No user")) }
    }

    fun getCurrentUser(): User? {
        val user = auth.currentUserOrNull() ?: return null
        return User(
            id = user.id,
            email = user.email ?: "",
            isEmailConfirmed = user.emailConfirmedAt != null
        )
    }

    fun isEmailConfirmed(): Boolean = auth.currentUserOrNull()?.emailConfirmedAt != null

    fun isLoggedIn(): Boolean = auth.currentUserOrNull() != null

    suspend fun resendConfirmationEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { auth.resendEmail(Email, email) }
    }
}
