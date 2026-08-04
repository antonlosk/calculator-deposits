package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateSaved: Long = System.currentTimeMillis(),
    val initialAmount: String,
    val term: String,
    val termUnitName: String, // String representation of TermUnit
    val interestRate: String,
    val isEffectiveRate: Boolean,
    val isCapitalization: Boolean,
    val finalAmount: Double,
    val profit: Double,
    val calculatedEffectiveRate: Double?,
    val startDate: Long // LocalDate epoch day
)
