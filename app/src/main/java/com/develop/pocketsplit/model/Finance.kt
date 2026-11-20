package com.develop.pocketsplit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "finances")
data class Finance(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isGroup: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)