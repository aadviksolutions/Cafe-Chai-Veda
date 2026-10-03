package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val unit: String,
    val currentStock: Double,
    val minimumStock: Double
)