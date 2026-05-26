package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeSerialDao {
    @Query("SELECT * FROM barcode_serials WHERE productId = :productId ORDER BY generatedAt DESC")
    fun getSerialsForProduct(productId: Int): Flow<List<BarcodeSerial>>

    @Query("SELECT * FROM barcode_serials ORDER BY generatedAt DESC")
    fun getAllSerials(): Flow<List<BarcodeSerial>>

    @Query("SELECT * FROM barcode_serials WHERE serialNumber = :serialNumber")
    suspend fun getSerialByNumber(serialNumber: String): BarcodeSerial?
    
    @Query("SELECT * FROM barcode_serials WHERE id = :id")
    suspend fun getSerialById(id: Int): BarcodeSerial?

    @Query("SELECT COUNT(*) FROM barcode_serials WHERE productId = :productId AND status = 'AVAILABLE'")
    fun getAvailableStockCount(productId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM barcode_serials WHERE productId = :productId AND status = 'SOLD'")
    fun getSoldStockCount(productId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSerial(serial: BarcodeSerial)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSerials(serials: List<BarcodeSerial>)

    @Update
    suspend fun updateSerial(serial: BarcodeSerial)
    
    @Query("UPDATE barcode_serials SET status = :status, soldAt = :soldAt, buyerName = :buyerName, soldPrice = :soldPrice WHERE id = :id")
    suspend fun markAsSold(id: Int, status: String = "SOLD", soldAt: Long = System.currentTimeMillis(), buyerName: String?, soldPrice: Double?)

    @Query("UPDATE barcode_serials SET status = 'AVAILABLE', soldAt = NULL, buyerName = NULL, soldPrice = NULL WHERE id = :id")
    suspend fun processReturn(id: Int)

    @Delete
    suspend fun deleteSerial(serial: BarcodeSerial)

    // For auto increment format
    @Query("SELECT COUNT(*) FROM barcode_serials WHERE productId = :productId AND generatedAt >= :startOfDay AND generatedAt < :endOfDay")
    suspend fun getCountForProductToday(productId: Int, startOfDay: Long, endOfDay: Long): Int
    
    @Query("SELECT COUNT(*) FROM barcode_serials WHERE productId = :productId")
    suspend fun getTotalCountForProduct(productId: Int): Int
}
