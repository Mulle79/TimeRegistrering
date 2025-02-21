package com.example.timeregistrering.repository

import android.content.Context
import android.content.Intent
import com.example.timeregistrering.common.security.SecurityManager
import com.example.timeregistrering.util.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityManager: SecurityManager
) {
    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .requestScopes(Scope(CalendarScopes.CALENDAR_READONLY))
                .requestServerAuthCode(Constants.GOOGLE_CLIENT_ID)
                .build()
        )
    }

    val isSignedIn: Flow<Boolean> = flow {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        emit(account != null && !account.isExpired)
    }.flowOn(Dispatchers.IO)

    // Google Auth funktioner
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun handleSignInResult(authCode: String): AuthResult {
        return try {
            // Exchange auth code for tokens
            val tokenResponse = exchangeAuthCodeForTokens(authCode)
            
            // Gem access token sikkert
            securityManager.storeSecureString(KEY_ACCESS_TOKEN, tokenResponse.accessToken)
            if (tokenResponse.refreshToken != null) {
                securityManager.storeSecureString(KEY_REFRESH_TOKEN, tokenResponse.refreshToken)
            }
            
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to handle sign in")
        }
    }

    suspend fun signOut(): AuthResult {
        return try {
            googleSignInClient.signOut().await()
            clearTokens()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    fun getAccessToken(): String? = securityManager.getSecureString(KEY_ACCESS_TOKEN)
    
    fun getRefreshToken(): String? = securityManager.getSecureString(KEY_REFRESH_TOKEN)

    fun clearTokens() {
        securityManager.removeSecureValue(KEY_ACCESS_TOKEN)
        securityManager.removeSecureValue(KEY_REFRESH_TOKEN)
    }

    fun createGoogleCredential(accessToken: String): GoogleCredential {
        return GoogleCredential().setAccessToken(accessToken)
    }

    private suspend fun exchangeAuthCodeForTokens(authCode: String): TokenResponse {
        // TODO: Implementer token exchange med Google OAuth2 endpoint
        // Dette er en placeholder implementation
        return TokenResponse(
            accessToken = "dummy_access_token",
            refreshToken = "dummy_refresh_token",
            expiresIn = 3600
        )
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "google_access_token"
        private const val KEY_REFRESH_TOKEN = "google_refresh_token"
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Int
)
