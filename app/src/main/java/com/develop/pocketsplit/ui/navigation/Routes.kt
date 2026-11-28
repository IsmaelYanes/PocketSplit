package com.develop.pocketsplit.ui.navigation

/** Rutas de navegación. */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val FINANCE_ID_ARG = "financeId"
    const val FINANCE = "finance/{$FINANCE_ID_ARG}"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val SETTINGS_ACCOUNT = "settings/account"

    fun finance(id: String) = "finance/$id"
}