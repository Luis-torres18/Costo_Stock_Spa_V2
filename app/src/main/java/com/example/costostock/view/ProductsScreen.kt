package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.costostock.ProductOffer
import com.example.costostock.User
import com.example.costostock.UserType
import kotlinx.coroutines.delay
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    products: List<ProductOffer>,
    currentUser: User?,
    onAddToCart: (ProductOffer) -> Unit,
    onAddProduct: (ProductOffer) -> Unit,
    onEditProduct: (ProductOffer) -> Unit,
    onDeleteProduct: (ProductOffer) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductOffer?>(null) }

    val canPublish = currentUser != null && (currentUser.type == UserType.SELLER || currentUser.type == UserType.ADMIN)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Excedentes", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            if (canPublish) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("Nuevo Producto") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            val availableCategories = listOf("Todos") + products.map { it.category }.distinct()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.take(4).forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            val filteredList = if (selectedFilter == "Todos") {
                products
            } else {
                products.filter { it.category == selectedFilter }
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos disponibles.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { offer ->
                        val hasManagePermission = currentUser != null && (
                                currentUser.type == UserType.ADMIN ||
                                        (currentUser.type == UserType.SELLER && offer.sellerId == currentUser.id)
                                )

                        CatalogCard(
                            offer = offer,
                            hasManagePermission = hasManagePermission,
                            onAddToCart = { onAddToCart(offer) },
                            onEdit = { productToEdit = offer },
                            onDelete = { onDeleteProduct(offer) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ProductManageDialog(
            existingProduct = null,
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onConfirm = { newProduct ->
                onAddProduct(newProduct)
                showAddDialog = false
            }
        )
    }

    if (productToEdit != null) {
        ProductManageDialog(
            existingProduct = productToEdit,
            currentUser = currentUser,
            onDismiss = { productToEdit = null },
            onConfirm = { editedProduct ->
                onEditProduct(editedProduct)
                productToEdit = null
            }
        )
    }
}

@Composable
fun CatalogCard(
    offer: ProductOffer,
    hasManagePermission: Boolean,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(offer.initialSecondsRemaining) }

    LaunchedEffect(key1 = offer.id) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val hours = timeLeft / 3600
    val minutes = (timeLeft % 3600) / 60
    val seconds = timeLeft % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tipo: ${offer.category} • ${offer.storeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasManagePermission) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (offer.description.isNotBlank()) {
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${offer.offerPrice}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (offer.originalPrice > offer.offerPrice) {
                        Text(
                            text = "$${offer.originalPrice}",
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${offer.stock} disp.",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "-${offer.co2SavedKg} kg CO₂",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir al Carrito")
            }
        }
    }
}

@Composable
fun ProductManageDialog(
    existingProduct: ProductOffer?,
    currentUser: User?,
    onDismiss: () -> Unit,
    onConfirm: (ProductOffer) -> Unit
) {
    var categoryInput by remember { mutableStateOf(existingProduct?.category ?: "") }
    var titleInput by remember { mutableStateOf(existingProduct?.title ?: "") }
    var descriptionInput by remember { mutableStateOf(existingProduct?.description ?: "") }
    var priceInput by remember { mutableStateOf(existingProduct?.offerPrice?.toString() ?: "") }
    var quantityInput by remember { mutableStateOf(existingProduct?.stock?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (existingProduct == null) "Nuevo Excedente" else "Editar Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("Tipo / Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) priceInput = it },
                        label = { Text("Valor ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quantityInput = it },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryInput.isBlank() || titleInput.isBlank() || descriptionInput.isBlank() || priceInput.isBlank() || quantityInput.isBlank()) {
                        errorMessage = "Por favor completa todos los campos."
                    } else {
                        val parsedPrice = priceInput.toIntOrNull() ?: 0
                        val parsedStock = quantityInput.toIntOrNull() ?: 1
                        val estimatedOriginalPrice = (parsedPrice * 1.5).toInt()

                        val finalOffer = existingProduct?.copy(
                            title = titleInput.trim(),
                            description = descriptionInput.trim(),
                            category = categoryInput.trim(),
                            offerPrice = parsedPrice,
                            stock = parsedStock
                        ) ?: ProductOffer(
                            id = "CS-${UUID.randomUUID().toString().take(5).uppercase()}",
                            sellerId = currentUser?.id ?: "VEND-DEFAULT",
                            title = titleInput.trim(),
                            description = descriptionInput.trim(),
                            category = categoryInput.trim(),
                            storeName = if (currentUser?.type == UserType.SELLER && currentUser.businessName.isNotBlank()) currentUser.businessName else "Sucursal Costo Stock Local",
                            address = if (currentUser?.type == UserType.SELLER && currentUser.address.isNotBlank()) currentUser.address else "Av. Principal 101",
                            distanceKm = 1.0,
                            originalPrice = estimatedOriginalPrice,
                            offerPrice = parsedPrice,
                            stock = parsedStock,
                            initialSecondsRemaining = 3600L,
                            co2SavedKg = 1.20
                        )
                        onConfirm(finalOffer)
                    }
                }
            ) {
                Text(if (existingProduct == null) "Guardar" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}