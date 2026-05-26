package com.example.ui.screens

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.viewmodel.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanToSellScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreview(
                    onBarcodeScanned = { barcodeValue ->
                        viewModel.scanAndLookup(barcodeValue)
                    }
                )
                
                ScanOverlay(viewModel = viewModel, onDismiss = { viewModel.resetScanResult() })
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Camera permission is required to scan barcodes.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

@Composable
fun ScanOverlay(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val scanResult by viewModel.scanResult.collectAsState()
    
    if (scanResult !is MainViewModel.ScanResult.Idle) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(when(scanResult) {
                    is MainViewModel.ScanResult.Available -> "Mark as Sold?"
                    is MainViewModel.ScanResult.AlreadySold -> "Already Sold"
                    is MainViewModel.ScanResult.NotFound -> "Not Found"
                    is MainViewModel.ScanResult.Error -> "Error"
                    else -> ""
                })
            },
            text = {
                when (val result = scanResult) {
                    is MainViewModel.ScanResult.Available -> {
                        var buyerName by remember { mutableStateOf("") }
                        var sellPrice by remember { mutableStateOf(result.product.price?.toString() ?: "") }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Product: ${result.product.name}")
                            Text("Serial: ${result.serial.serialNumber}")
                            OutlinedTextField(
                                value = buyerName,
                                onValueChange = { buyerName = it },
                                label = { Text("Buyer Name (Optional)") }
                            )
                            OutlinedTextField(
                                value = sellPrice,
                                onValueChange = { sellPrice = it },
                                label = { Text("Sell Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            Button(onClick = {
                                viewModel.markAsSold(result.serial.id, buyerName.ifBlank { null }, sellPrice.toDoubleOrNull())
                            }) {
                                Text("Confirm Sale")
                            }
                        }
                    }
                    is MainViewModel.ScanResult.AlreadySold -> {
                        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val date = result.soldAt?.let { dateFmt.format(Date(it)) } ?: "Unknown"
                        Text("This item was already sold on $date.")
                    }
                    is MainViewModel.ScanResult.NotFound -> {
                        Text("Barcode ${result.scannedCode} is not registered in the database.")
                    }
                    is MainViewModel.ScanResult.Error -> {
                        Text(result.message)
                    }
                    else -> {}
                }
            },
            confirmButton = {
                if (scanResult !is MainViewModel.ScanResult.Available) {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            }
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraPreview(onBarcodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val executor = Executors.newSingleThreadExecutor()
                
                val scanner = BarcodeScanning.getClient()

                var isScanningEnabled = true

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    if (!isScanningEnabled) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let {
                                        isScanningEnabled = false
                                        onBarcodeScanned(it)
                                        // Simple throttle, ideally handle this with state
                                        it
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
