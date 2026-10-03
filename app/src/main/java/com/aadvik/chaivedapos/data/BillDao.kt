package com.aadvik.chaivedapos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Query("""
        SELECT * FROM bills
        ORDER BY billDateTime DESC
    """)
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bills
        WHERE id = :billId
        LIMIT 1
    """)
    suspend fun getBillById(billId: Int): BillEntity?

    @Query("""
        SELECT * FROM bills
        WHERE billNumber = :billNumber
        LIMIT 1
    """)
    suspend fun getBillByNumber(billNumber: String): BillEntity?

    @Query("""
        SELECT * FROM bills
        WHERE paymentStatus != 'Paid'
        ORDER BY billDateTime DESC
    """)
    fun getUnpaidBills(): Flow<List<BillEntity>>

    @Query("""
        SELECT * FROM bills
        WHERE billDateTime BETWEEN :startTime AND :endTime
        ORDER BY billDateTime DESC
    """)
    fun getBillsBetween(
        startTime: Long,
        endTime: Long
    ): Flow<List<BillEntity>>

    @Query("""
        SELECT COALESCE(SUM(total), 0)
        FROM bills
        WHERE billDateTime BETWEEN :startTime AND :endTime
    """)
    fun getTotalSales(
        startTime: Long,
        endTime: Long
    ): Flow<Double>

    @Query("""
        SELECT COUNT(*)
        FROM bills
        WHERE billDateTime BETWEEN :startTime AND :endTime
    """)
    fun getBillCount(
        startTime: Long,
        endTime: Long
    ): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(paidAmount), 0)
        FROM bills
        WHERE billDateTime BETWEEN :startTime AND :endTime
    """)
    fun getTotalCollection(
        startTime: Long,
        endTime: Long
    ): Flow<Double>

    @Query("""
        UPDATE bills
        SET paidAmount = :paidAmount,
            paymentStatus = :paymentStatus,
            paymentMode = :paymentMode
        WHERE billNumber = :billNumber
    """)
    suspend fun updatePayment(
        billNumber: String,
        paidAmount: Double,
        paymentStatus: String,
        paymentMode: String
    )
    @Query("""
        SELECT MAX(CAST(SUBSTR(billNumber, 4) AS INTEGER))
        FROM bills
        WHERE billDateTime BETWEEN :startTime AND :endTime
          AND billNumber GLOB 'CV-[0-9][0-9][0-9][0-9]'
          AND LENGTH(billNumber) = 7
    """)
    suspend fun getLastBillSequence(
        startTime: Long,
        endTime: Long
    ): Int?

    @Query("""
        UPDATE bills
        SET customerName = :customerName,
            mobileNumber = :mobileNumber,
            orderType = :orderType,
            tableNumber = :tableNumber,
            subtotal = :subtotal,
            discount = :discount,
            tax = :tax,
            total = :total,
            paidAmount = :paidAmount,
            paymentStatus = :paymentStatus,
            paymentMode = :paymentMode
        WHERE billNumber = :billNumber
    """)
    suspend fun updateBillAfterEdit(
        billNumber: String,
        customerName: String,
        mobileNumber: String,
        orderType: String,
        tableNumber: String,
        subtotal: Double,
        discount: Double,
        tax: Double,
        total: Double,
        paidAmount: Double,
        paymentStatus: String,
        paymentMode: String
    )

}
