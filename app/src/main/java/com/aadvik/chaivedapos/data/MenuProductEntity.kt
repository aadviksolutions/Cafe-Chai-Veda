package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_products")
data class MenuProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val price: Double,
    val foodType: String,
    val available: Boolean = true,
    val imageUri: String = ""
)
