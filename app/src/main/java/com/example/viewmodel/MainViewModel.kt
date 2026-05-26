package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BarcodeSerial
import com.example.data.Product
import com.example.repository.AppRepository
import com.example.utils.SerialNumberFormat
import com.example.utils.BarcodeGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProduct(name: String, category: String, sku: String, price: Double?) {
        viewModelScope.launch {
            repository.insertProduct(Product(name = name, category = category, sku = sku, price = price))
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch { repository.updateProduct(product) }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    // Stock Summary
    fun getAvailableStockCount(productId: Int) = repository.getAvailableStockCount(productId)
    fun getSoldStockCount(productId: Int) = repository.getSoldStockCount(productId)

    // Barcode Generation
    fun generateBarcodes(product: Product, amount: Int, format: String) {
        viewModelScope.launch {
            val totalCount = repository.getTotalCountForProduct(product.id)
            val newSerials = mutableListOf<BarcodeSerial>()
            for (i in 0 until amount) {
                val serialNumber = SerialNumberFormat.generateSerialNumber(product.category, product.sku, totalCount + i)
                newSerials.add(
                    BarcodeSerial(
                        productId = product.id,
                        serialNumber = serialNumber,
                        barcodeFormat = format
                    )
                )
            }
            repository.insertSerials(newSerials)
        }
    }

    // Scan To Sell State
    sealed class ScanResult {
        object Idle : ScanResult()
        data class Available(val product: Product, val serial: BarcodeSerial) : ScanResult()
        data class AlreadySold(val product: Product, val serial: BarcodeSerial, val soldAt: Long?) : ScanResult()
        data class NotFound(val scannedCode: String) : ScanResult()
        data class Error(val message: String) : ScanResult()
    }

    private val _scanResult = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanResult: StateFlow<ScanResult> = _scanResult.asStateFlow()

    fun resetScanResult() {
        _scanResult.value = ScanResult.Idle
    }

    fun scanAndLookup(serialNumber: String) {
        viewModelScope.launch {
            try {
                val serial = repository.getSerialByNumber(serialNumber)
                if (serial == null) {
                    _scanResult.value = ScanResult.NotFound(serialNumber)
                } else {
                    val product = repository.getProduct(serial.productId)
                    if (product == null) {
                        _scanResult.value = ScanResult.Error("Product not found in DB")
                    } else if (serial.status == "SOLD") {
                        _scanResult.value = ScanResult.AlreadySold(product, serial, serial.soldAt)
                    } else {
                        _scanResult.value = ScanResult.Available(product, serial)
                    }
                }
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun markAsSold(serialId: Int, buyerName: String?, sellPrice: Double?) {
        viewModelScope.launch {
            repository.markAsSold(serialId, buyerName, sellPrice)
            _scanResult.value = ScanResult.Idle // Reset after successful sale
        }
    }
}
