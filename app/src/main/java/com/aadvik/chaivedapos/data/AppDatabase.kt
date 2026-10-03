package com.aadvik.chaivedapos.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MenuProductEntity::class,
        BillEntity::class,
        BillItemEntity::class,
        InventoryItemEntity::class,
        TableEntity::class,
        RestaurantSettingsEntity::class,
        PrinterSettingsEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuProductDao(): MenuProductDao
    abstract fun billDao(): BillDao
    abstract fun billItemDao(): BillItemDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun tableDao(): TableDao
    abstract fun restaurantSettingsDao(): RestaurantSettingsDao
    abstract fun printerSettingsDao(): PrinterSettingsDao
}
