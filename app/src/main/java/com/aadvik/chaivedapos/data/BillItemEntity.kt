package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_items")
data class BillItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val billId: Int,

    val productId: Int,

    val productName: String,

    val category: String,

    val quantity: Int,

    val price: Double,

    val amount: Double
)