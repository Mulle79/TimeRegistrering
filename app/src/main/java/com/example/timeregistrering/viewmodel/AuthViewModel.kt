package com.example.timeregistrering.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timeregistrering.model.UiState
import com.example.timeregistrering.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<AuthState>>(UiState.Loading)
    val authState: StateFlow<UiState<AuthState>> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val token = authRepository.getAccessToken()
                _authState.value = if (token != null) {
                    UiState.Success(AuthState.Authenticated(token))
                } else {
                    UiState.Success(AuthState.NotAuthenticated)
                }
            } catch (e: Exception) {
                _authState.value = UiState.Error("Kunne ikke tjekke login status: ${e.message}")
            }
        }
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                _authState.value = UiState.Loading
                if (account != null) {
                    authRepository.saveAccessToken(account.idToken ?: "")
                    _authState.value = UiState.Success(AuthState.Authenticated(account.idToken ?: ""))
                } else {
                    _authState.value = UiState.Error("Google login fejlede")
                }
            } catch (e: Exception) {
                _authState.value = UiState.Error("Login fejlede: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _authState.value = UiState.Loading
                authRepository.clearAccessToken()
                _authState.value = UiState.Success(AuthState.NotAuthenticated)
            } catch (e: Exception) {
                _authState.value = UiState.Error("Logout fejlede: ${e.message}")
            }
        }
    }

    sealed class AuthState {
        object NotAuthenticated : AuthState()
        data class Authenticated(val token: String) : AuthState()
    }
}
