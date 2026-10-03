package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BillItemDao {

    @Insert
    suspend fun insertBillItem(item: BillItemEntity): Long

    @Insert
    suspend fun insertBillItems(items: List<BillItemEntity>)

    @Update
    suspend fun updateBillItem(item: BillItemEntity)

    @Delete
    suspend fun deleteBillItem(item: BillItemEntity)

    @Query("""
        SELECT * FROM bill_items
        WHERE billId = :billId
        ORDER BY id
    """)
    suspend fun getItemsForBill(billId: Int): List<BillItemEntity>

    @Query("""
        DELETE FROM bill_items
        WHERE billId = :billId
    """)
    suspend fun deleteItemsForBill(billId: Int)

    @Query("""
        SELECT * FROM bill_items
        WHERE productId = :productId
        ORDER BY id DESC
    """)
    suspend fun getItemsForProduct(productId: Int): List<BillItemEntity>

    @Query("""
        SELECT productName, SUM(quantity) AS totalQuantity
        FROM bill_items
        GROUP BY productId, productName
        ORDER BY totalQuantity DESC
    """)
    suspend fun getItemWiseSales(): List<ItemSalesResult>
}

data class ItemSalesResult(
    val productName: String,
    val totalQuantity: Int
)