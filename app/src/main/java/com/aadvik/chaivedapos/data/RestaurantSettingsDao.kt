package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantSettingsDao {

    @Query("SELECT * FROM restaurant_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<RestaurantSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: RestaurantSettingsEntity)

    @Update
    suspend fun updateSettings(settings: RestaurantSettingsEntity)

    @Query("DELETE FROM restaurant_settings")
    suspend fun deleteSettings()
}