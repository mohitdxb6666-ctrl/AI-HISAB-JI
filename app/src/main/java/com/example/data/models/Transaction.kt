package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val type: Int, // 1 = Credit / Udhaar (उधार जोड़ा), 2 = Debit / Vasooli (पैसे मिले)
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val source: Int = 0 // 0 = manual, 1 = voice, 2 = camera_scan
)
