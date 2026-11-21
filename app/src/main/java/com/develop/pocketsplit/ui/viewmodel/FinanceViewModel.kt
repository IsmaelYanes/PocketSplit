package com.develop.pocketsplit.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.develop.pocketsplit.data.PocketSplitDatabaseLocal
import com.develop.pocketsplit.data.local.PocketSplitDatabase
import com.develop.pocketsplit.data.repository.FinanceRepository
import com.develop.pocketsplit.model.Category
import com.develop.pocketsplit.model.CategoryType
import com.develop.pocketsplit.model.Finance
import com.develop.pocketsplit.model.Transaction
import com.develop.pocketsplit.ui.navigation.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FinanceViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val financeId: String = checkNotNull(savedStateHandle[Routes.FINANCE_ID_ARG])
    private val repository = FinanceRepository(PocketSplitDatabase.getInstance(application))

    val finance: StateFlow<Finance?> = repository.observeFinance(financeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Estados observables para la interfaz
    var transactions by mutableStateOf(PocketSplitDatabaseLocal.transactions.toList())
        private set

    var categories: List<Category> by mutableStateOf(PocketSplitDatabaseLocal.categories.toList())
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

    // Renombrar la finanza
    fun renameFinance(newName: String) {
        val current = finance.value ?: return
        if (newName.isBlank()) return
        viewModelScope.launch { repository.renameFinance(current, newName) }
    }
}