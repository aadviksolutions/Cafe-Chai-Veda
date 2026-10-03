package com.aadvik.chaivedapos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_tables")
data class TableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val capacity: Int,
    val status: String = "Available"
)