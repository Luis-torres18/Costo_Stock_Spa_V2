package com.example.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.costostock.ui.theme.CostoStockTheme
import com.example.costostock.view.*

enum class UserType {
    BUYER,
    SELLER,
    ADMIN
}

enum class AccountStatus {
    ACTIVE,
    PENDING_APPROVAL,
    REJECTED
}

data class User(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,
    val type: UserType,
    var status: AccountStatus,
    val address: String = "",
    val website: String = "",
    val socialMedia: String = "",
    val businessName: String = "",
    val businessType: String = ""
)

data class ProductOffer(
    val id: String,
    val sellerId: String,
    val title: String,
    val description: String,
    val category: String,
    val storeName: String,
    val address: String,
    val distanceKm: Double,
    val originalPrice: Int,
    val offerPrice: Int,
    var stock: Int,
    val initialSecondsRemaining: Long,
    val co2SavedKg: Double,
    val imageUri: String = ""
)

data class CartItem(
    val product: ProductOffer,
    var quantity: Int
)

data class PurchaseVoucher(
    val voucherCode: String,
    val items: List<CartItem>,
    val purchaseDate: String,
    val totalPaid: Int,
    val totalCo2Saved: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CostoStockTheme {
                val usersList = remember {
                    mutableStateListOf(
                        User(
                            id = "USR-ADMIN",
                            fullName = "Costo Stock Administrator",
                            phone = "+56911223344",
                            email = "admin@costostock.cl",
                            password = "admin123",
                            type = UserType.ADMIN,
                            status = AccountStatus.ACTIVE
                        ),
                        User(
                            id = "VEND-001",
                            fullName = "Roberto Díaz",
                            phone = "+56988776655",
                            email = "vendedor@costostock.cl",
                            password = "vendedor123",
                            type = UserType.SELLER,
                            status = AccountStatus.ACTIVE,
                            address = "Av. Pedro Montt 1820",
                            businessName = "Supermercado Central Valparaíso",
                            businessType = "Abarrotes y Alimentos"
                        )
                    )
                }

                var currentUser by remember { mutableStateOf<User?>(null) }
                var showAuthScreen by remember { mutableStateOf(false) }
                var showAdminPanel by remember { mutableStateOf(false) }

                val productsList = remember {
                    mutableStateListOf(
                        ProductOffer(
                            id = "CS-001",
                            sellerId = "VEND-001",
                            title = "Pack Yogur Batido 12x120g",
                            description = "Lote refrigerado en óptimo estado, vencimiento a 5 días.",
                            category = "Lácteos",
                            storeName = "Supermercado Central Valparaíso",
                            address = "Av. Pedro Montt 1820",
                            distanceKm = 0.8,
                            originalPrice = 4990,
                            offerPrice = 1990,
                            stock = 8,
                            initialSecondsRemaining = 3600L,
                            co2SavedKg = 1.45,
                            imageUri = ""
                        ),
                        ProductOffer(
                            id = "CS-002",
                            sellerId = "VEND-001",
                            title = "Caja Galletas Avena & Miel 400g",
                            description = "Excedente de temporada con empaque intacto.",
                            category = "Snacks",
                            storeName = "EcoMarket Viña del Mar",
                            address = "Calle Valparaíso 650",
                            distanceKm = 1.6,
                            originalPrice = 3290,
                            offerPrice = 1290,
                            stock = 4,
                            initialSecondsRemaining = 1800L,
                            co2SavedKg = 0.85,
                            imageUri = ""
                        ),
                        ProductOffer(
                            id = "CS-003",
                            sellerId = "USR-ADMIN",
                            title = "Queso Gauda Laminado 500g",
                            description = "Corte laminado al vacío, conservación en frío.",
                            category = "Lácteos",
                            storeName = "Distribuidora Costa",
                            address = "Av. Libertad 340",
                            distanceKm = 2.3,
                            originalPrice = 5890,
                            offerPrice = 2790,
                            stock = 12,
                            initialSecondsRemaining = 7200L,
                            co2SavedKg = 2.30,
                            imageUri = ""
                        )
                    )
                }

                val cartItems = remember { mutableStateListOf<CartItem>() }
                var currentTab by remember { mutableStateOf(0) }

                fun addToCart(product: ProductOffer) {
                    val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
                    if (existingIndex >= 0) {
                        val currentItem = cartItems[existingIndex]
                        if (currentItem.quantity < product.stock) {
                            cartItems[existingIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
                        }
                    } else {
                        cartItems.add(CartItem(product = product, quantity = 1))
                    }
                }

                if (showAuthScreen) {
                    AuthScreen(
                        users = usersList,
                        onLoginSuccess = { user ->
                            currentUser = user
                            showAuthScreen = false
                        },
                        onRegisterUser = { newUser ->
                            usersList.add(newUser)
                        },
                        onClose = { showAuthScreen = false }
                    )
                } else if (showAdminPanel) {
                    AdminApprovalScreen(
                        users = usersList,
                        onApproveUser = { user ->
                            val index = usersList.indexOf(user)
                            if (index >= 0) usersList[index] = user.copy(status = AccountStatus.ACTIVE)
                        },
                        onRejectUser = { user ->
                            val index = usersList.indexOf(user)
                            if (index >= 0) usersList[index] = user.copy(status = AccountStatus.REJECTED)
                        },
                        onClose = { showAdminPanel = false }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = "Home") },
                                    label = { Text("Inicio") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(Icons.Default.Store, contentDescription = "Products") },
                                    label = { Text("Productos") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (cartItems.isNotEmpty()) {
                                                    Badge { Text(cartItems.sumOf { it.quantity }.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                                        }
                                    },
                                    label = { Text("Carrito") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            when (currentTab) {
                                0 -> HomeScreen(
                                    products = productsList,
                                    currentUser = currentUser,
                                    onOpenAuth = { showAuthScreen = true },
                                    onLogout = { currentUser = null },
                                    onOpenAdminPanel = { showAdminPanel = true },
                                    onAddToCart = { product -> addToCart(product) }
                                )
                                1 -> ProductsScreen(
                                    products = productsList,
                                    currentUser = currentUser,
                                    onAddToCart = { product -> addToCart(product) },
                                    onAddProduct = { newProduct -> productsList.add(0, newProduct) },
                                    onEditProduct = { editedProduct ->
                                        val index = productsList.indexOfFirst { it.id == editedProduct.id }
                                        if (index >= 0) productsList[index] = editedProduct
                                    },
                                    onDeleteProduct = { productToDelete ->
                                        productsList.remove(productToDelete)
                                        cartItems.removeAll { it.product.id == productToDelete.id }
                                    }
                                )
                                2 -> CartScreen(
                                    cartItems = cartItems,
                                    onIncreaseQuantity = { item ->
                                        val index = cartItems.indexOf(item)
                                        if (index >= 0 && item.quantity < item.product.stock) {
                                            cartItems[index] = item.copy(quantity = item.quantity + 1)
                                        }
                                    },
                                    onDecreaseQuantity = { item ->
                                        val index = cartItems.indexOf(item)
                                        if (index >= 0) {
                                            if (item.quantity > 1) {
                                                cartItems[index] = item.copy(quantity = item.quantity - 1)
                                            } else {
                                                cartItems.removeAt(index)
                                            }
                                        }
                                    },
                                    onRemoveItem = { item -> cartItems.remove(item) },
                                    onClearCart = { cartItems.clear() },
                                    onGoToProducts = { currentTab = 1 }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}