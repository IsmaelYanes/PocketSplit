package com.develop.pocketsplit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.pocketsplit.data.repository.AuthRepository
import com.develop.pocketsplit.data.repository.toAuthError
import com.develop.pocketsplit.model.AuthError
import com.develop.pocketsplit.model.User
import com.develop.pocketsplit.util.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la sesión y operaciones de usuario. Se comparte entre la pantalla de
 * acceso y Ajustes (se obtiene con el ámbito de la Activity).
 */
class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Perfil del usuario con sesión; `null` si no hay sesión (modo invitado). */
    val currentUser: StateFlow<User?> = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Lectura síncrona para decidir la pantalla inicial sin esperar al Flow. */
    val hasSession: Boolean get() = repository.hasSession()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        AuthValidator.validateLogin(cleanEmail, password)?.let { return fail(it) }
        run(onSuccess) { repository.login(cleanEmail, password) }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        AuthValidator.validateRegister(cleanName, cleanEmail, password)?.let { return fail(it) }
        run(onSuccess) { repository.register(cleanName, cleanEmail, password) }
    }

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()
        AuthValidator.validateEmail(cleanEmail)?.let { return fail(it) }
        if (_uiState.value.isLoading) return
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            repository.sendPasswordReset(cleanEmail).fold(
                onSuccess = { _uiState.value = AuthUiState(passwordResetSent = true) },
                onFailure = { _uiState.value = AuthUiState(error = it.toAuthError()) }
            )
        }
    }

    fun updateName(name: String, onSuccess: () -> Unit) {
        val cleanName = name.trim()
        AuthValidator.validateName(cleanName)?.let { return fail(it) }
        run(onSuccess) { repository.updateName(cleanName) }
    }

    fun deleteAccount(password: String, onSuccess: () -> Unit) {
        AuthValidator.validateLoginPassword(password)?.let { return fail(it) }
        run(onSuccess) { repository.deleteAccount(password) }
    }

    /** Cierra la sesión. Es síncrono: Firebase limpia la sesión local al instante. */
    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    /** Limpia el error o aviso mostrado (al editar un campo o cerrar un diálogo). */
    fun clearMessages() {
        if (_uiState.value.error != null || _uiState.value.passwordResetSent) {
            _uiState.value = _uiState.value.copy(error = null, passwordResetSent = false)
        }
    }

    private fun fail(error: AuthError) {
        _uiState.value = AuthUiState(error = error)
    }

    private fun run(onSuccess: () -> Unit, block: suspend () -> Result<Unit>) {
        if (_uiState.value.isLoading) return
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            block().fold(
                onSuccess = {
                    _uiState.value = AuthUiState()
                    onSuccess()
                },
                onFailure = { _uiState.value = AuthUiState(error = it.toAuthError()) }
            )
        }
    }
}