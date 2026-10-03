package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val billNumber: String,

    val billDateTime: Long,

    val customerName: String,

    val mobileNumber: String,

    val orderType: String,

    val tableNumber: String,

    val subtotal: Double,

    val discount: Double,

    val tax: Double,

    val total: Double,

    val paidAmount: Double,

    val paymentStatus: String,

    val paymentMode: String
)