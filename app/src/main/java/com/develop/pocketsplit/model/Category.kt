package com.develop.pocketsplit.model

import java.util.UUID

enum class CategoryType {
    INCOME, EXPENSE
}

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: CategoryType
)