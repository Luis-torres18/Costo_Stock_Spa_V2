package com.example.costostock.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.costostock.ProductOffer
import com.example.costostock.User
import com.example.costostock.UserType
import com.example.costostock.formatPrice
import java.io.File
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (offer.imageUri.isNotBlank()) {
                AsyncImage(
                    model = offer.imageUri,
                    contentDescription = offer.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

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
                    Text(
                        text = "Punto de retiro: ${offer.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                        text = formatPrice(offer.offerPrice),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (offer.isOfferActive && offer.originalPrice > offer.offerPrice) {
                        Text(
                            text = formatPrice(offer.originalPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (offer.stock > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (offer.stock > 0) "${offer.stock} disp." else "Agotado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (offer.stock > 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (offer.isOfferActive) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "¡En Oferta Destacada!",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Precio Estándar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
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
                enabled = offer.stock > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (offer.stock > 0) "Añadir al Carrito" else "Sin Stock Disponible")
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
    val context = LocalContext.current

    var categoryInput by remember { mutableStateOf(existingProduct?.category ?: "") }
    var titleInput by remember { mutableStateOf(existingProduct?.title ?: "") }
    var descriptionInput by remember { mutableStateOf(existingProduct?.description ?: "") }
    var regularPriceInput by remember { mutableStateOf(existingProduct?.originalPrice?.toString() ?: "") }
    var quantityInput by remember { mutableStateOf(existingProduct?.stock?.toString() ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(existingProduct?.imageUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var enableOfferSection by remember { mutableStateOf(existingProduct?.isOfferActive ?: false) }
    var offerPriceInput by remember { mutableStateOf(existingProduct?.offerPrice?.takeIf { existingProduct.isOfferActive }?.toString() ?: "") }
    var offerMinutesInput by remember {
        mutableStateOf(
            if (existingProduct != null && existingProduct.initialSecondsRemaining > 0) {
                (existingProduct.initialSecondsRemaining / 60).toString()
            } else "60"
        )
    }
    var errorMessage by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    fun prepareCameraAndLaunch() {
        val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (existingProduct == null) "Nuevo Excedente" else "Editar Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { prepareCameraAndLaunch() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería", fontSize = 12.sp)
                    }
                }

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
                        value = regularPriceInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) regularPriceInput = it },
                        label = { Text("Precio Regular ($)") },
                        placeholder = { Text("Ej: 10000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) quantityInput = it },
                        label = { Text("Cantidad / Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📍 Dirección de retiro (Automática del vendedor):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentUser?.address?.isNotBlank() == true) currentUser.address else "Av. Pedro Montt 1820, Valparaíso",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (existingProduct != null) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Poner en Oferta Destacada",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = enableOfferSection,
                            onCheckedChange = { enableOfferSection = it }
                        )
                    }

                    if (enableOfferSection) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = offerPriceInput,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) offerPriceInput = it },
                                    label = { Text("Precio en Oferta ($)") },
                                    placeholder = { Text("Menor al regular") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = offerMinutesInput,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) offerMinutesInput = it },
                                    label = { Text("Duración de oferta (en minutos)") },
                                    placeholder = { Text("Ej: 60 = 1 hr, 120 = 2 hrs") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val mins = offerMinutesInput.toLongOrNull() ?: 0L
                                val hoursCalc = mins / 60
                                val minsRest = mins % 60
                                val tiempoTexto = if (hoursCalc > 0) "${hoursCalc}h ${minsRest}m" else "${minsRest} min"

                                Text(
                                    text = "⏱️ Tiempo configurado: $mins minutos ($tiempoTexto)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val regP = regularPriceInput.toIntOrNull() ?: 0
                                val offP = offerPriceInput.toIntOrNull() ?: 0
                                if (regP > 0 && offP > 0 && offP < regP) {
                                    val disc = ((1.0 - (offP.toDouble() / regP)) * 100).toInt()
                                    Text(
                                        text = "🏷️ Descuento calculado: -$disc% (Precio final: ${formatPrice(offP)})",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
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
                    val parsedRegPrice = regularPriceInput.toIntOrNull() ?: 0
                    val parsedStock = quantityInput.toIntOrNull() ?: 1

                    if (categoryInput.isBlank() || titleInput.isBlank() || descriptionInput.isBlank() || regularPriceInput.isBlank() || quantityInput.isBlank()) {
                        errorMessage = "Por favor completa todos los campos principales."
                    } else if (enableOfferSection && ((offerPriceInput.toIntOrNull() ?: 0) >= parsedRegPrice || (offerPriceInput.toIntOrNull() ?: 0) <= 0)) {
                        errorMessage = "El precio de oferta debe ser mayor a 0 y menor que el precio regular."
                    } else if (enableOfferSection && (offerMinutesInput.toLongOrNull() ?: 0L) <= 0L) {
                        errorMessage = "Ingresa una cantidad de minutos válida mayor a 0."
                    } else {
                        val isOffer = enableOfferSection
                        val finalOfferPrice = if (isOffer) (offerPriceInput.toIntOrNull() ?: parsedRegPrice) else parsedRegPrice
                        val durationSeconds = if (isOffer) ((offerMinutesInput.toLongOrNull() ?: 60L) * 60L) else 0L

                        val sellerStore = if (currentUser?.businessName?.isNotBlank() == true) currentUser.businessName else (existingProduct?.storeName ?: "Sucursal Local")
                        val sellerAddr = if (currentUser?.address?.isNotBlank() == true) currentUser.address else (existingProduct?.address ?: "Av. Principal 101, Valparaíso")

                        val finalOffer = existingProduct?.copy(
                            title = titleInput.trim(),
                            description = descriptionInput.trim(),
                            category = categoryInput.trim(),
                            storeName = sellerStore,
                            address = sellerAddr,
                            originalPrice = parsedRegPrice,
                            offerPrice = finalOfferPrice,
                            stock = parsedStock,
                            initialSecondsRemaining = durationSeconds,
                            isOfferActive = isOffer,
                            imageUri = selectedImageUri?.toString() ?: existingProduct.imageUri
                        ) ?: ProductOffer(
                            id = "CS-${UUID.randomUUID().toString().take(5).uppercase()}",
                            sellerId = currentUser?.id ?: "VEND-DEFAULT",
                            title = titleInput.trim(),
                            description = descriptionInput.trim(),
                            category = categoryInput.trim(),
                            storeName = sellerStore,
                            address = sellerAddr,
                            distanceKm = 1.0,
                            originalPrice = parsedRegPrice,
                            offerPrice = parsedRegPrice,
                            stock = parsedStock,
                            initialSecondsRemaining = 0L,
                            co2SavedKg = 1.20,
                            imageUri = selectedImageUri?.toString() ?: "",
                            isOfferActive = false
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