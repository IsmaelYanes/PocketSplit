package com.develop.pocketsplit.model

import java.time.LocalDate
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val categoryId: String,
    val amount: Double,
    val type: CategoryType,
    val description: String,
    val date: String = LocalDate.now().toString()
)