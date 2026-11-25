package com.develop.pocketsplit.util

import com.develop.pocketsplit.model.AuthError

/** Validación local de credenciales, antes de llamar a Firebase. */
object AuthValidator {

    /** Número mínimo de caracteres que exige Firebase. */
    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$")

    fun validateName(name: String): AuthError? =
        if (name.isBlank()) AuthError.EMPTY_NAME else null

    fun validateEmail(email: String): AuthError? =
        if (EMAIL_REGEX.matches(email.trim())) null else AuthError.INVALID_EMAIL

    /** En login solo se exige que no esté vacía: la comprobación la hace Firebase. */
    fun validateLoginPassword(password: String): AuthError? =
        if (password.isEmpty()) AuthError.EMPTY_PASSWORD else null

    fun validateNewPassword(password: String): AuthError? = when {
        password.isEmpty() -> AuthError.EMPTY_PASSWORD
        password.length < MIN_PASSWORD_LENGTH -> AuthError.WEAK_PASSWORD
        else -> null
    }

    fun validateLogin(email: String, password: String): AuthError? =
        validateEmail(email) ?: validateLoginPassword(password)

    fun validateRegister(name: String, email: String, password: String): AuthError? =
        validateName(name) ?: validateEmail(email) ?: validateNewPassword(password)
}