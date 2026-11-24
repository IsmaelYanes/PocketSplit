package com.develop.pocketsplit.data

import com.develop.pocketsplit.model.Category
import com.develop.pocketsplit.model.CategoryType
import com.develop.pocketsplit.model.Transaction
import com.develop.pocketsplit.model.User

object PocketSplitDatabaseLocal {

    // Listas mutables que simulan la base de datos local para el MVP
    val users = mutableListOf<User>(
        User(name = "Propietario MVP", email = "user@pocketsplit.com")
    )

    val categories = mutableListOf<Category>(
        Category(name = "Salario / Ingresos", type = CategoryType.INCOME),
        Category(name = "Alimentación", type = CategoryType.EXPENSE),
        Category(name = "Transporte", type = CategoryType.EXPENSE),
        Category(name = "Entretenimiento", type = CategoryType.EXPENSE)
    )

    val transactions = mutableListOf<Transaction>()

    // Funciones auxiliares básicas para la gestión de finanzas individuales
    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    fun getBalance(): Double {
        val totalIncome = transactions.filter { it.type == CategoryType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == CategoryType.EXPENSE }.sumOf { it.amount }
        return totalIncome - totalExpense
    }
}