package com.develop.pocketsplit.model

/** Errores de la gestión de usuarios, independientes de Firebase y de la UI. */
enum class AuthError {
    EMPTY_NAME,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    WEAK_PASSWORD,
    INVALID_CREDENTIALS,
    EMAIL_IN_USE,
    NETWORK,
    TOO_MANY_REQUESTS,
    REQUIRES_RECENT_LOGIN,
    NOT_LOGGED_IN,
    UNKNOWN
}