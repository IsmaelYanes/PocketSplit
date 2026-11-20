package com.develop.pocketsplit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.develop.pocketsplit.model.Finance

@Database(entities = [Finance::class], version = 1, exportSchema = false)
abstract class PocketSplitDatabase : RoomDatabase() {

    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile private var INSTANCE: PocketSplitDatabase? = null

        fun getInstance(context: Context): PocketSplitDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PocketSplitDatabase::class.java,
                    "pocketsplit.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}