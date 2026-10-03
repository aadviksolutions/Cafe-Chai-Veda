package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {

    @Query("SELECT * FROM restaurant_tables ORDER BY id")
    fun getAllTables(): Flow<List<TableEntity>>

    @Insert
    suspend fun insertTable(table: TableEntity): Long

    @Update
    suspend fun updateTable(table: TableEntity)

    @Delete
    suspend fun deleteTable(table: TableEntity)

    @Query("DELETE FROM restaurant_tables")
    suspend fun deleteAllTables()
}