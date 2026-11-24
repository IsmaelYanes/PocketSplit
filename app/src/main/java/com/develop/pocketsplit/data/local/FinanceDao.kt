package com.develop.pocketsplit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.develop.pocketsplit.model.Finance
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finances ORDER BY createdAt")
    fun observeAll(): Flow<List<Finance>>

    @Query("SELECT * FROM finances WHERE id = :id")
    fun observeById(id: String): Flow<Finance?>

    @Insert
    suspend fun insert(finance: Finance)

    @Update
    suspend fun update(finance: Finance)

    @Query("DELETE FROM finances WHERE id = :id")
    suspend fun deleteById(id: String)
}