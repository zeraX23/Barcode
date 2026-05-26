package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateBarcodeScreen(
    viewModel: MainViewModel,
    productId: Int,
    onNavigateBack: () -> Unit,
    onPreviewBarcodes: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val product = products.find { it.id == productId }

    var quantityStr by remember { mutableStateOf("1") }
    var format by remember { mutableStateOf("CODE_128") } // or QR_CODE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Barcodes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        if (product == null) {
            Text("Product not found.", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Product: ${product.name}", style = MaterialTheme.typography.titleLarge)
            Text(text = "SKU: ${product.sku}", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = quantityStr,
                onValueChange = { quantityStr = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InputChip(
                    selected = format == "CODE_128",
                    onClick = { format = "CODE_128" },
                    label = { Text("CODE 128") }
                )
                InputChip(
                    selected = format == "QR_CODE",
                    onClick = { format = "QR_CODE" },
                    label = { Text("QR Code") }
                )
            }

            Button(
                onClick = {
                    val quantity = quantityStr.toIntOrNull() ?: 1
                    viewModel.generateBarcodes(product, quantity, format)
                    onNavigateBack() // Returning home or maybe straight to history? Let's just pop back.
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate")
            }
        }
    }
}
