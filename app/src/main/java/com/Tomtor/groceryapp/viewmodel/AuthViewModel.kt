package com.Tomtor.groceryapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Tomtor.groceryapp.data.local.TokenStore
import com.Tomtor.groceryapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val repository = AuthRepository(tokenStore)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        // check if user is already logged in
        viewModelScope.launch {
            tokenStore.token.collect { token ->
                if (token != null) {
                    com.Tomtor.groceryapp.data.api.ApiClient.setToken(token)
                    _state.value = _state.value.copy(isLoggedIn = true)
                }
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.register(username, password)
            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repository.login(username, password)
            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _state.value = AuthState()
        }
    }
}