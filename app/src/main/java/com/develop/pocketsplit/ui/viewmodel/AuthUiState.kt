package com.develop.pocketsplit.ui.viewmodel

import androidx.annotation.StringRes
import com.develop.pocketsplit.R
import com.develop.pocketsplit.model.AuthError

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val passwordResetSent: Boolean = false,
    /** Correo al que se ha enviado el enlace de verificación tras pedir un cambio de correo. */
    val emailVerificationSentTo: String? = null,
    val passwordChanged: Boolean = false
)

@StringRes
fun AuthError.messageRes(): Int = when (this) {
    AuthError.EMPTY_NAME -> R.string.auth_error_empty_name
    AuthError.INVALID_EMAIL -> R.string.auth_error_invalid_email
    AuthError.EMPTY_PASSWORD -> R.string.auth_error_empty_password
    AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
    AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
    AuthError.EMAIL_IN_USE -> R.string.auth_error_email_in_use
    AuthError.NETWORK -> R.string.auth_error_network
    AuthError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
    AuthError.REQUIRES_RECENT_LOGIN -> R.string.auth_error_recent_login
    AuthError.NOT_LOGGED_IN -> R.string.auth_error_not_logged_in
    AuthError.UNKNOWN -> R.string.auth_error_unknown
}