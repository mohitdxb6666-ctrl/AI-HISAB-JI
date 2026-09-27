package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "Mohit Kirana Store",
    val ownerName: String = "Mohit Ji",
    val phone: String = "9876543210",
    val upiId: String = "mohit@upi",
    val pin: String = "",
    val isPinEnabled: Boolean = false,
    val soundboxEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = true,
    val lastBackupTime: Long = 0L
)
