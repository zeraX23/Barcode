package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.repository.AppRepository
import com.example.ui.screens.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { AppRepository(database.productDao(), database.barcodeSerialDao()) }
    val factory = remember { MainViewModelFactory(repository) }
    
    val viewModel: MainViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddProduct = { navController.navigate("add_product") },
                onEditProduct = { productId -> navController.navigate("edit_product/$productId") },
                onGenerateBarcode = { productId -> navController.navigate("generate_barcode/$productId") },
                onViewHistory = { productId -> navController.navigate("history/$productId") },
                onScanToSell = { navController.navigate("scan_to_sell") }
            )
        }
        
        composable("add_product") {
            AddEditProductScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            "edit_product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: -1
            AddEditProductScreen(
                viewModel = viewModel,
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            "generate_barcode/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: -1
            GenerateBarcodeScreen(
                viewModel = viewModel,
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onPreviewBarcodes = { navController.navigate("preview_barcodes/$productId") } // We might preview generated session?
            )
        }
        
        composable(
            "history/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: -1
            HistoryScreen(
                productId = productId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("scan_to_sell") {
            ScanToSellScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
