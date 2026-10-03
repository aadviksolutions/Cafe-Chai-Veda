package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_settings")
data class RestaurantSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "CHAI VEDA",
    val address: String = "",
    val mobile: String = "",
    val email: String = "",
    val gstin: String = "",
    val fssaiNumber: String = "",
    val gstEnabled: Boolean = false,
    val gstPercent: Double = 5.0,
    val billHeader: String = "Café • Juice • & More",
    val billFooter: String = "Thank you. Visit again!",
    val logoUri: String = ""
)
