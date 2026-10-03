package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrinterSettingsDao {

    @Query("SELECT * FROM printer_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<PrinterSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PrinterSettingsEntity)

    @Update
    suspend fun updateSettings(settings: PrinterSettingsEntity)

    @Query("DELETE FROM printer_settings")
    suspend fun deleteSettings()
}