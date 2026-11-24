package com.develop.pocketsplit.data.repository

import com.develop.pocketsplit.data.local.PocketSplitDatabase
import com.develop.pocketsplit.model.Finance
import kotlinx.coroutines.flow.Flow

class FinanceRepository(database: PocketSplitDatabase) {

    private val financeDao = database.financeDao()

    fun observeFinances(): Flow<List<Finance>> = financeDao.observeAll()

    fun observeFinance(id: String): Flow<Finance?> = financeDao.observeById(id)

    suspend fun addFinance(name: String, isGroup: Boolean) =
        financeDao.insert(Finance(name = name.trim(), isGroup = isGroup))

    suspend fun renameFinance(finance: Finance, newName: String) =
        financeDao.update(finance.copy(name = newName.trim()))

    suspend fun deleteFinance(id: String) = financeDao.deleteById(id)
}