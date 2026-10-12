package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.costostock.AccountStatus
import com.example.costostock.User
import com.example.costostock.UserType
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    users: List<User>,
    onLoginSuccess: (User) -> Unit,
    onRegisterUser: (User) -> Unit,
    onClose: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    var selectedType by remember { mutableStateOf<UserType?>(null) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var regFullName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var regAddress by remember { mutableStateOf("") }
    var regWebsite by remember { mutableStateOf("") }
    var regSocialMedia by remember { mutableStateOf("") }
    var regBusinessName by remember { mutableStateOf("") }
    var regBusinessType by remember { mutableStateOf("") }

    var regError by remember { mutableStateOf("") }
    var regSuccessMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isRegisterMode) "Registro de Usuario" else "Iniciar Sesión", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isRegisterMode) {
                        Text(
                            text = "Bienvenido a Costo Stock",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ingresa tus credenciales para continuar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Correo Electrónico") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (loginError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = loginError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val user = users.find { it.email.trim().equals(loginEmail.trim(), ignoreCase = true) && it.password == loginPassword }
                                when {
                                    user == null -> loginError = "Credenciales incorrectas o usuario no registrado."
                                    user.status == AccountStatus.PENDING_APPROVAL -> loginError = "Tu cuenta está pendiente de aprobación por el Administrador."
                                    user.status == AccountStatus.REJECTED -> loginError = "Esta cuenta ha sido rechazada por el Administrador."
                                    else -> onLoginSuccess(user)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ingresar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = {
                            isRegisterMode = true
                            loginError = ""
                        }) {
                            Text("¿No tienes cuenta? Regístrate aquí")
                        }

                    } else {
                        Text(
                            text = "Registro de Usuario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = typeMenuExpanded,
                            onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = when (selectedType) {
                                    UserType.BUYER -> "Comprador"
                                    UserType.SELLER -> "Vendedor"
                                    UserType.ADMIN -> "Administrador"
                                    null -> "Selecciona tipo de cuenta"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Usuario") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = typeMenuExpanded,
                                onDismissRequest = { typeMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Comprador (Acceso a ofertas y carrito)") },
                                    onClick = {
                                        selectedType = UserType.BUYER
                                        typeMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Vendedor (Publica y administra sus lotes)") },
                                    onClick = {
                                        selectedType = UserType.SELLER
                                        typeMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Administrador (Control total y aprobaciones)") },
                                    onClick = {
                                        selectedType = UserType.ADMIN
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Nombre Completo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Número de Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Correo Electrónico") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Confirmar Contraseña") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedType == UserType.SELLER) {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Text(
                                text = "Datos Comerciales del Vendedor",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regBusinessName,
                                onValueChange = { regBusinessName = it },
                                label = { Text("Nombre del Negocio") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regBusinessType,
                                onValueChange = { regBusinessType = it },
                                label = { Text("Tipo de Negocio") },
                                placeholder = { Text("Ej: Supermercado, Panadería, Frutería") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regAddress,
                                onValueChange = { regAddress = it },
                                label = { Text("Dirección de Retiro") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regWebsite,
                                onValueChange = { regWebsite = it },
                                label = { Text("Página Web (Opcional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = regSocialMedia,
                                onValueChange = { regSocialMedia = it },
                                label = { Text("Redes Sociales (Opcional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (regError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = regError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        if (regSuccessMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = regSuccessMessage, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (selectedType == null || regFullName.isBlank() || regPhone.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    regError = "Por favor completa todos los campos requeridos."
                                } else if (regPassword != regConfirmPassword) {
                                    regError = "Las contraseñas no coinciden."
                                } else if (users.any { it.email.equals(regEmail.trim(), ignoreCase = true) }) {
                                    regError = "El correo ya se encuentra registrado."
                                } else if (selectedType == UserType.SELLER && (regBusinessName.isBlank() || regAddress.isBlank() || regBusinessType.isBlank())) {
                                    regError = "Completa el nombre, tipo de negocio y dirección del comercio."
                                } else {
                                    regError = ""
                                    val initialStatus = if (selectedType == UserType.BUYER) AccountStatus.ACTIVE else AccountStatus.PENDING_APPROVAL

                                    val newUser = User(
                                        id = "USR-${UUID.randomUUID().toString().take(6).uppercase()}",
                                        fullName = regFullName.trim(),
                                        phone = regPhone.trim(),
                                        email = regEmail.trim(),
                                        password = regPassword,
                                        type = selectedType!!,
                                        status = initialStatus,
                                        address = regAddress.trim(),
                                        website = regWebsite.trim(),
                                        socialMedia = regSocialMedia.trim(),
                                        businessName = regBusinessName.trim(),
                                        businessType = regBusinessType.trim()
                                    )

                                    onRegisterUser(newUser)

                                    if (initialStatus == AccountStatus.ACTIVE) {
                                        regSuccessMessage = "¡Registro exitoso! Ya puedes iniciar sesión."
                                    } else {
                                        regSuccessMessage = "¡Solicitud enviada! Tu cuenta será revisada y aprobada por el Administrador."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Registrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            isRegisterMode = false
                            regError = ""
                            regSuccessMessage = ""
                        }) {
                            Text("¿Ya tienes una cuenta? Inicia sesión")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApprovalScreen(
    users: List<User>,
    onApproveUser: (User) -> Unit,
    onRejectUser: (User) -> Unit,
    onClose: () -> Unit
) {
    val pendingUsers = users.filter { it.status == AccountStatus.PENDING_APPROVAL }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador: Aprobaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Solicitudes Pendientes (${pendingUsers.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (pendingUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay cuentas pendientes por evaluar.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pendingUsers) { u ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(text = u.fullName, fontWeight = FontWeight.Bold)
                                Text(text = "Rol solicitado: ${u.type.name} • Tel: ${u.phone}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Correo: ${u.email}", style = MaterialTheme.typography.bodySmall)

                                if (u.type == UserType.SELLER) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Negocio: ${u.businessName} (${u.businessType})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    Text(text = "Dirección: ${u.address}", style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onApproveUser(u) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Aprobar")
                                    }
                                    OutlinedButton(
                                        onClick = { onRejectUser(u) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}