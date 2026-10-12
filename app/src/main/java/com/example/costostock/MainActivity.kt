package com.example.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.costostock.ui.theme.CostoStockTheme
import com.example.costostock.view.*
import java.text.NumberFormat
import java.util.Locale

// Formato de moneda con punto (CLP: $10.000)
fun formatPrice(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "CL"))
    return "$" + formatter.format(amount)
}

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
    var offerPrice: Int,
    var stock: Int,
    var initialSecondsRemaining: Long,
    val co2SavedKg: Double,
    val imageUri: String = "",
    var isOfferActive: Boolean = false
)

data class CartItem(
    val product: ProductOffer,
    var quantity: Int
)

data class PurchaseVoucher(
    val voucherCode: String,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
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
                            fullName = "Administrador Costo Stock",
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
                            address = "Av. Pedro Montt 1820, Valparaíso",
                            website = "https://centralvalpo.cl",
                            socialMedia = "@centralvalpo_oficial",
                            businessName = "Supermercado Central Valparaíso",
                            businessType = "Abarrotes y Alimentos"
                        ),
                        User(
                            id = "BUY-001",
                            fullName = "Camila Soto",
                            phone = "+56977665544",
                            email = "comprador@costostock.cl",
                            password = "comprador123",
                            type = UserType.BUYER,
                            status = AccountStatus.ACTIVE
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
                            address = "Av. Pedro Montt 1820, Valparaíso",
                            distanceKm = 0.8,
                            originalPrice = 4990,
                            offerPrice = 1990,
                            stock = 8,
                            initialSecondsRemaining = 3600L,
                            co2SavedKg = 1.45,
                            imageUri = "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=600&q=80",
                            isOfferActive = true
                        ),
                        ProductOffer(
                            id = "CS-002",
                            sellerId = "VEND-001",
                            title = "Caja Galletas Avena & Miel 400g",
                            description = "Excedente de temporada con empaque intacto.",
                            category = "Snacks",
                            storeName = "Supermercado Central Valparaíso",
                            address = "Av. Pedro Montt 1820, Valparaíso",
                            distanceKm = 1.6,
                            originalPrice = 3290,
                            offerPrice = 1290,
                            stock = 4,
                            initialSecondsRemaining = 1800L,
                            co2SavedKg = 0.85,
                            imageUri = "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=600&q=80",
                            isOfferActive = true
                        ),
                        ProductOffer(
                            id = "CS-003",
                            sellerId = "USR-ADMIN",
                            title = "Queso Gauda Laminado 500g",
                            description = "Corte laminado al vacío, conservación en frío.",
                            category = "Lácteos",
                            storeName = "Distribuidora Costa",
                            address = "Av. Libertad 340, Viña del Mar",
                            distanceKm = 2.3,
                            originalPrice = 5890,
                            offerPrice = 5890,
                            stock = 12,
                            initialSecondsRemaining = 0L,
                            co2SavedKg = 2.30,
                            imageUri = "https://images.unsplash.com/photo-1624806992066-5ffcf7ca186b?auto=format&fit=crop&w=600&q=80",
                            isOfferActive = false
                        )
                    )
                }

                val cartItems = remember { mutableStateListOf<CartItem>() }
                var currentTab by remember { mutableStateOf(0) }

                // Lógica de Reserva dinámica de Stock
                fun addToCart(product: ProductOffer) {
                    val pIndex = productsList.indexOfFirst { it.id == product.id }
                    if (pIndex >= 0 && productsList[pIndex].stock > 0) {
                        productsList[pIndex] = productsList[pIndex].copy(stock = productsList[pIndex].stock - 1)
                        val existingCartIdx = cartItems.indexOfFirst { it.product.id == product.id }
                        if (existingCartIdx >= 0) {
                            val cur = cartItems[existingCartIdx]
                            cartItems[existingCartIdx] = cur.copy(quantity = cur.quantity + 1)
                        } else {
                            cartItems.add(CartItem(product = productsList[pIndex], quantity = 1))
                        }
                    }
                }

                fun increaseCartQuantity(item: CartItem) {
                    val pIndex = productsList.indexOfFirst { it.id == item.product.id }
                    if (pIndex >= 0 && productsList[pIndex].stock > 0) {
                        productsList[pIndex] = productsList[pIndex].copy(stock = productsList[pIndex].stock - 1)
                        val cIndex = cartItems.indexOf(item)
                        if (cIndex >= 0) {
                            cartItems[cIndex] = item.copy(quantity = item.quantity + 1)
                        }
                    }
                }

                fun decreaseCartQuantity(item: CartItem) {
                    val pIndex = productsList.indexOfFirst { it.id == item.product.id }
                    if (pIndex >= 0) {
                        productsList[pIndex] = productsList[pIndex].copy(stock = productsList[pIndex].stock + 1)
                    }
                    val cIndex = cartItems.indexOf(item)
                    if (cIndex >= 0) {
                        if (item.quantity > 1) {
                            cartItems[cIndex] = item.copy(quantity = item.quantity - 1)
                        } else {
                            cartItems.removeAt(cIndex)
                        }
                    }
                }

                fun removeCartItem(item: CartItem) {
                    val pIndex = productsList.indexOfFirst { it.id == item.product.id }
                    if (pIndex >= 0) {
                        productsList[pIndex] = productsList[pIndex].copy(stock = productsList[pIndex].stock + item.quantity)
                    }
                    cartItems.remove(item)
                }

                fun clearCart() {
                    cartItems.forEach { item ->
                        val pIndex = productsList.indexOfFirst { it.id == item.product.id }
                        if (pIndex >= 0) {
                            productsList[pIndex] = productsList[pIndex].copy(stock = productsList[pIndex].stock + item.quantity)
                        }
                    }
                    cartItems.clear()
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
                                    onAddToCart = { product -> addToCart(product) },
                                    onOfferExpired = { expiredOffer ->
                                        val idx = productsList.indexOfFirst { it.id == expiredOffer.id }
                                        if (idx >= 0) {
                                            productsList[idx] = expiredOffer.copy(isOfferActive = false)
                                        }
                                    }
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
                                    currentUser = currentUser,
                                    onIncreaseQuantity = { item -> increaseCartQuantity(item) },
                                    onDecreaseQuantity = { item -> decreaseCartQuantity(item) },
                                    onRemoveItem = { item -> removeCartItem(item) },
                                    onClearCart = { clearCart() },
                                    onCheckoutCompleted = {
                                        cartItems.clear()
                                    },
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