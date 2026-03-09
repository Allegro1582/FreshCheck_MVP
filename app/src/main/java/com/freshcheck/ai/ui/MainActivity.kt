package com.freshcheck.ai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.freshcheck.ai.data.ProductEntity
import com.freshcheck.ai.ui.components.CameraScanner
import com.freshcheck.ai.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: ProductViewModel by viewModels()

    // Camera permission request
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Show bottom sheet to scan product
            isShowingAddSheet.value = true
        }
    }

    private val isShowingAddSheet = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val products by viewModel.allProducts.collectAsState(initial = emptyList())
                    val sheetState = rememberModalBottomSheetState()

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("FreshCheck AI", fontWeight = FontWeight.Bold) },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        this, Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    isShowingAddSheet.value = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Product")
                            }
                        }
                    ) { padding ->
                        Column(modifier = Modifier.padding(padding)) {
                            ProductList(
                                products = products,
                                onDelete = { viewModel.deleteProduct(it) },
                                getStatus = { viewModel.getProductStatus(it) }
                            )
                        }

                        if (isShowingAddSheet.value) {
                            ModalBottomSheet(
                                onDismissRequest = { isShowingAddSheet.value = false },
                                sheetState = sheetState
                            ) {
                                AddProductSheetContent(
                                    onAdd = { name, date ->
                                        viewModel.addProduct(name, date)
                                        isShowingAddSheet.value = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductList(
    products: List<ProductEntity>,
    onDelete: (ProductEntity) -> Unit,
    getStatus: (ProductEntity) -> ProductViewModel.ProductStatus
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products tracked. Tap + to scan.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductItem(
                    product = product,
                    status = getStatus(product),
                    onDelete = { onDelete(product) }
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    product: ProductEntity,
    status: ProductViewModel.ProductStatus,
    onDelete: () -> Unit
) {
    val backgroundColor = when (status) {
        ProductViewModel.ProductStatus.EXPIRED -> Color(0xFFFFEBEE) // Light Red
        ProductViewModel.ProductStatus.EXPIRING_SOON -> Color(0xFFFFFDE7) // Light Yellow
        ProductViewModel.ProductStatus.FRESH -> Color.White
    }

    val borderColor = when (status) {
        ProductViewModel.ProductStatus.EXPIRED -> Color.Red
        ProductViewModel.ProductStatus.EXPIRING_SOON -> Color(0xFFFBC02D) // Dark Yellow
        ProductViewModel.ProductStatus.FRESH -> Color.LightGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Expiry: ${formatDate(product.expiryDate)}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    "Days Left: ${product.getDaysLeft()}",
                    fontSize = 14.sp,
                    color = if (status == ProductViewModel.ProductStatus.EXPIRED) Color.Red else Color.Black
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun AddProductSheetContent(onAdd: (String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var scannedDate by remember { mutableStateOf<Long?>(null) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add New Product", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name (e.g., Milk)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Point camera at Expiry Date:", fontSize = 14.sp, color = Color.Gray)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black)
        ) {
            CameraScanner(onDateDetected = { date ->
                if (scannedDate == null) {
                    scannedDate = date
                }
            })
            
            if (scannedDate != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Date Scanned: ${dateFormat.format(Date(scannedDate!!))}",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (name.isNotBlank() && scannedDate != null) onAdd(name, scannedDate!!) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && scannedDate != null
        ) {
            Text("Add to List")
        }
        
        if (scannedDate != null) {
            TextButton(onClick = { scannedDate = null }) {
                Text("Rescan Date")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
