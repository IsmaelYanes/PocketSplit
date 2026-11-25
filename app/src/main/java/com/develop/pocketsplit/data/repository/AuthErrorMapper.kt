package com.develop.pocketsplit.data.repository

import com.develop.pocketsplit.model.AuthError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/** Traduce las excepciones de Firebase a errores de dominio. */
fun Throwable.toAuthError(): AuthError = when (this) {
    is NotLoggedInException -> AuthError.NOT_LOGGED_IN
    // WeakPassword hereda de InvalidCredentials: debe comprobarse antes.
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
    is FirebaseAuthRecentLoginRequiredException -> AuthError.REQUIRES_RECENT_LOGIN
    is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
    is FirebaseNetworkException -> AuthError.NETWORK
    else -> AuthError.UNKNOWN
}

/** Se lanza cuando se pide una operación de cuenta sin sesión iniciada. */
class NotLoggedInException : IllegalStateException("No hay ninguna sesión iniciada")