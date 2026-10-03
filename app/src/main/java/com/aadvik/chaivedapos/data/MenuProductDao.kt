package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuProductDao {

    @Query("SELECT * FROM menu_products ORDER BY category, name")
    fun getAllProducts(): Flow<List<MenuProductEntity>>

    @Query("SELECT * FROM menu_products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): MenuProductEntity?

    @Query("SELECT COUNT(*) FROM menu_products")
    suspend fun getProductCount(): Int

    @Insert
    suspend fun insertProduct(product: MenuProductEntity): Long

    @Update
    suspend fun updateProduct(product: MenuProductEntity)

    @Delete
    suspend fun deleteProduct(product: MenuProductEntity)

    @Query("DELETE FROM menu_products")
    suspend fun deleteAllProducts()
}