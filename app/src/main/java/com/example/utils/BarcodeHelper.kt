package com.example.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.*

object BarcodeGenerator {

    fun generateBarcodeBitmap(content: String, format: BarcodeFormat, width: Int, height: Int): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            val writer = MultiFormatWriter()
            val matrix: BitMatrix = writer.encode(content, format, width, height)
            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bm.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bm
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

object SerialNumberFormat {
    // Format: {CATEGORY_CODE}-{PRODUCT_CODE}-{YYYYMMDD}-{AUTO_INCREMENT_4DIGIT}
    fun generateSerialNumber(category: String, sku: String, count: Int): String {
        val catCode = category.take(3).uppercase(Locale.getDefault())
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val counterStr = String.format(Locale.getDefault(), "%04d", count + 1)
        return "$catCode-$sku-$dateStr-$counterStr"
    }
}
