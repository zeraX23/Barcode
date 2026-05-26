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
import com.example.data.Product
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: MainViewModel,
    productId: Int = -1,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.allProducts.collectAsState()
    val product = products.find { it.id == productId }

    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    
    val popularCategories = listOf("Electronics", "Food & Beverage", "Clothing", "Home", "Toys")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == -1) "Add Product" else "Edit Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                popularCategories.take(3).forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Or Type Category") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text("SKU / Code") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Price (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank() && category.isNotBlank() && sku.isNotBlank()) {
                        val price = priceStr.toDoubleOrNull()
                        if (productId == -1) {
                            viewModel.addProduct(name, category, sku, price)
                        } else {
                            viewModel.updateProduct(Product(productId, name, category, sku, price))
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && category.isNotBlank() && sku.isNotBlank()
            ) {
                Text(if (productId == -1) "Save Product" else "Update Product")
            }
        }
    }
}

