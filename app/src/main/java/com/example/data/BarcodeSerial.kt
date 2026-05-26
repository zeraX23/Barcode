package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "barcode_serials",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productId"),
        Index("serialNumber", unique = true)
    ]
)
data class BarcodeSerial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val serialNumber: String,
    val barcodeFormat: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val isPrinted: Boolean = false,
    val status: String = "AVAILABLE", // ""AVAILABLE", "SOLD", "RETURNED"
    val soldAt: Long? = null,
    val soldPrice: Double? = null,
    val buyerName: String? = null,
    val transactionId: String? = null
) : Serializable
