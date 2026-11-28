package com.develop.pocketsplit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.pocketsplit.data.local.SessionPreferences
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
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val session = SessionPreferences(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Perfil del usuario con sesión; `null` si no hay sesión (modo invitado). */
    val currentUser: StateFlow<User?> = repository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Lectura síncrona para decidir la pantalla inicial sin esperar al Flow:
     * se salta el acceso si hay sesión de Firebase o si se eligió "Continuar sin cuenta".
     */
    val shouldSkipLogin: Boolean get() = repository.hasSession() || session.isGuest

    /** Recuerda que el usuario usa la app sin cuenta. */
    fun continueAsGuest() {
        session.isGuest = true
        _uiState.value = AuthUiState()
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        AuthValidator.validateLogin(cleanEmail, password)?.let { return fail(it) }
        run(onAccountReady(onSuccess)) { repository.login(cleanEmail, password) }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        AuthValidator.validateRegister(cleanName, cleanEmail, password)?.let { return fail(it) }
        run(onAccountReady(onSuccess)) { repository.register(cleanName, cleanEmail, password) }
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

    fun changeEmail(newEmail: String, password: String, onSuccess: () -> Unit) {
        val cleanEmail = newEmail.trim()
        AuthValidator.validateEmail(cleanEmail)?.let { return fail(it) }
        AuthValidator.validateLoginPassword(password)?.let { return fail(it) }
        run(
            onSuccess = onSuccess,
            successState = AuthUiState(emailVerificationSentTo = cleanEmail)
        ) { repository.changeEmail(cleanEmail, password) }
    }

    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) {
        AuthValidator.validateLoginPassword(currentPassword)?.let { return fail(it) }
        AuthValidator.validateNewPassword(newPassword)?.let { return fail(it) }
        run(
            onSuccess = onSuccess,
            successState = AuthUiState(passwordChanged = true)
        ) { repository.changePassword(currentPassword, newPassword) }
    }

    /** Recarga los datos de la cuenta (por si se confirmó un cambio de correo fuera de la app). */
    fun refreshAccount() {
        viewModelScope.launch { repository.refreshUser() }
    }

    fun deleteAccount(password: String, onSuccess: () -> Unit) {
        AuthValidator.validateLoginPassword(password)?.let { return fail(it) }
        run(onAccountReady(onSuccess)) { repository.deleteAccount(password) }
    }

    /** Cierra la sesión. Es síncrono: Firebase limpia la sesión local al instante. */
    fun logout() {
        repository.logout()
        session.isGuest = false
        _uiState.value = AuthUiState()
    }

    /** Limpia el error o aviso mostrado (al editar un campo o cerrar un diálogo). */
    fun clearMessages() {
        val current = _uiState.value
        if (current.error != null || current.passwordResetSent ||
            current.emailVerificationSentTo != null || current.passwordChanged
        ) {
            _uiState.value = current.copy(
                error = null,
                passwordResetSent = false,
                emailVerificationSentTo = null,
                passwordChanged = false
            )
        }
    }

    /**
     * Tras iniciar sesión, registrarse o borrar la cuenta el modo invitado deja de
     * aplicar: al cerrar sesión se vuelve a la pantalla de acceso, donde se puede elegir de nuevo.
     */
    private fun onAccountReady(onSuccess: () -> Unit): () -> Unit = {
        session.isGuest = false
        onSuccess()
    }

    private fun fail(error: AuthError) {
        _uiState.value = AuthUiState(error = error)
    }

    private fun run(
        onSuccess: () -> Unit,
        successState: AuthUiState = AuthUiState(),
        block: suspend () -> Result<Unit>
    ) {
        if (_uiState.value.isLoading) return
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            block().fold(
                onSuccess = {
                    _uiState.value = successState
                    onSuccess()
                },
                onFailure = { _uiState.value = AuthUiState(error = it.toAuthError()) }
            )
        }
    }
}