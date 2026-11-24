package com.develop.pocketsplit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.pocketsplit.data.local.PocketSplitDatabase
import com.develop.pocketsplit.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        FinanceRepository(PocketSplitDatabase.getInstance(application))

    val uiState: StateFlow<HomeUiState> = repository.observeFinances()
        .map { HomeUiState(finances = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun addFinance(name: String, isGroup: Boolean) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addFinance(name, isGroup) }
    }
}