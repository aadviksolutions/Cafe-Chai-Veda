package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {

    @Query("SELECT * FROM inventory_items ORDER BY name")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Insert
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAllItems()
}