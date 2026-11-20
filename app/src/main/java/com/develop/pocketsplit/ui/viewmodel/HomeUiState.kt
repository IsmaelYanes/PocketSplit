package com.develop.pocketsplit.ui.viewmodel

import com.develop.pocketsplit.model.Finance

data class HomeUiState(
    val finances: List<Finance> = emptyList(),
    val isLoading: Boolean = true
)