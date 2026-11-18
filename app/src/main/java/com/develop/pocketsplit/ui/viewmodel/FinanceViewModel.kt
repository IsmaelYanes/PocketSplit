package com.develop.pocketsplit.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.pocketsplit.data.PocketSplitDatabaseLocal
import com.develop.pocketsplit.model.Category
import com.develop.pocketsplit.model.CategoryType
import com.develop.pocketsplit.model.Transaction

class FinanceViewModel : ViewModel() {

    // Estados observables para la interfaz
    var transactions by mutableStateOf(PocketSplitDatabaseLocal.transactions.toList())
        private set

    var categories by mutableStateOf(PocketSplitDatabaseLocal.categories.toList())
        private set

    var currentBalance by mutableStateOf(PocketSplitDatabaseLocal.getBalance())
        private set

    // Registrar una nueva transacción
    fun addTransaction(categoryId: String, amount: Double, type: CategoryType, description: String) {
        val currentUser = PocketSplitDatabaseLocal.users.firstOrNull() ?: return

        val newTransaction = Transaction(
            userId = currentUser.id,
            categoryId = categoryId,
            amount = amount,
            type = type,
            description = description
        )

        PocketSplitDatabaseLocal.addTransaction(newTransaction)

        // Actualizar estados para refrescar la UI
        transactions = PocketSplitDatabaseLocal.transactions.toList()
        currentBalance = PocketSplitDatabaseLocal.getBalance()
    }
}