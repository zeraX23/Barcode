package com.example.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val productDao: ProductDao,
    private val barcodeSerialDao: BarcodeSerialDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allSerials: Flow<List<BarcodeSerial>> = barcodeSerialDao.getAllSerials()

    fun getProductById(id: Int): Flow<Product?> = productDao.getProductByIdFlow(id)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    fun getSerialsForProduct(productId: Int): Flow<List<BarcodeSerial>> = barcodeSerialDao.getSerialsForProduct(productId)
    
    suspend fun getSerialByNumber(serialNumber: String): BarcodeSerial? = barcodeSerialDao.getSerialByNumber(serialNumber)

    suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)
    
    suspend fun getProduct(id: Int): Product? = productDao.getProductById(id)

    fun getAvailableStockCount(productId: Int): Flow<Int> = barcodeSerialDao.getAvailableStockCount(productId)
    
    fun getSoldStockCount(productId: Int): Flow<Int> = barcodeSerialDao.getSoldStockCount(productId)

    suspend fun insertSerials(serials: List<BarcodeSerial>) = barcodeSerialDao.insertSerials(serials)

    suspend fun insertSerial(serial: BarcodeSerial) = barcodeSerialDao.insertSerial(serial)
    
    suspend fun markAsSold(id: Int, buyerName: String?, soldPrice: Double?) = barcodeSerialDao.markAsSold(id = id, buyerName = buyerName, soldPrice = soldPrice)

    suspend fun processReturn(id: Int) = barcodeSerialDao.processReturn(id)

    suspend fun deleteSerial(serial: BarcodeSerial) = barcodeSerialDao.deleteSerial(serial)

    suspend fun getTotalCountForProduct(productId: Int): Int = barcodeSerialDao.getTotalCountForProduct(productId)
    
    suspend fun getCountForProductToday(productId: Int, start: Long, end: Long): Int = barcodeSerialDao.getCountForProductToday(productId, start, end)
}
