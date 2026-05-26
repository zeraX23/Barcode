package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.data.BarcodeSerial
import com.example.repository.AppRepository
import com.example.utils.BarcodeGenerator
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    productId: Int,
    repository: AppRepository,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var serials by remember { mutableStateOf<List<BarcodeSerial>>(emptyList()) }

    LaunchedEffect(productId) {
        repository.getSerialsForProduct(productId).collect {
            serials = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serial History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(serials) { serial ->
                BarcodeHistoryCard(serial = serial, repository = repository)
            }
        }
    }
}

@Composable
fun BarcodeHistoryCard(
    serial: BarcodeSerial,
    repository: AppRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(serial) {
        val format = if (serial.barcodeFormat == "QR_CODE") BarcodeFormat.QR_CODE else BarcodeFormat.CODE_128
        bitmap = BarcodeGenerator.generateBarcodeBitmap(serial.serialNumber, format, 600, 200)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Barcode",
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = serial.serialNumber, style = MaterialTheme.typography.titleMedium)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            Text(text = "Generated: ${dateFormat.format(Date(serial.generatedAt))}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Status: ${serial.status}", color = if (serial.status == "AVAILABLE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            
            if (serial.status == "SOLD") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { 
                    coroutineScope.launch {
                        repository.processReturn(serial.id)
                    }
                }) {
                    Text("Process Return")
                }
            }
        }
    }
}
