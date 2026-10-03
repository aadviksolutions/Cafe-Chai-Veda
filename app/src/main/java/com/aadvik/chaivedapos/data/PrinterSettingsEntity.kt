package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printer_settings")
data class PrinterSettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    val printerName: String = "",

    val paperSize: String = "58mm",

    val connected: Boolean = false,

    val autoPrint: Boolean = false,

    val billLogo: Boolean = true,

    val billHeader: String = "CHAI VEDA",

    val billFooter: String = "Thank you. Visit again!"
)