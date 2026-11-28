package com.develop.pocketsplit.data.local

import android.content.Context

/**
 * Preferencias de sesión que Firebase no guarda: recuerda si el usuario eligió
 * "Continuar sin cuenta", para no volver a mostrar la pantalla de acceso al reabrir la app.
 *
 * Se usa SharedPreferences porque la lectura debe ser síncrona: hace falta para elegir
 * la pantalla inicial antes de que se dibuje la primera pantalla.
 */
class SessionPreferences(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var isGuest: Boolean
        get() = prefs.getBoolean(KEY_GUEST, false)
        set(value) = prefs.edit().putBoolean(KEY_GUEST, value).apply()

    private companion object {
        const val FILE_NAME = "session"
        const val KEY_GUEST = "guest_mode"
    }
}