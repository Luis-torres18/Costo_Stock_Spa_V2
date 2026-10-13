# Costo Stock (SpA) — Marketplace Móvil de Rescate de Alimentos

> **"Nada se pierde, todos ganamos"**  
> Aplicación móvil nativa en Android desarrollada con Jetpack Compose orientada a la reducción del desperdicio alimentario y a la valorización de excedentes comerciales bajo el marco de la economía circular (Ley REP en Chile).

---

## 1. Descripción del Proyecto

**Costo Stock** es una plataforma móvil tipo marketplace que conecta a comercios (supermercados, distribuidoras, panaderías, fruterías) con consumidores finales para comercializar excedentes de inventario o lotes con fecha de vencimiento próxima a precios rebajados. 

El sistema busca generar un triple impacto:
* **Económico:** Ahorro sustancial para el consumidor y recuperación de costos para el vendedor.
* **Ambiental:** Prevención del desecho orgánico y reducción medible de emisiones de gases de efecto invernadero ($\text{kg de CO}_2$ evitados).
* **Social:** Consumo responsable y acceso a alimentos de primera calidad en óptimas condiciones de consumo.

---

## 2. Pila Tecnológica y Arquitectura

* **Lenguaje:** Kotlin
* **Framework UI:** Jetpack Compose (Material 3)
* **Gestión de Carga de Imágenes:** [Coil Compose](https://coil-kt.github.io/coil/) (`AsyncImage`)
* **Integración de Hardware:** Camera API (`ActivityResultContracts.TakePicture`) y Media Storage (`ActivityResultContracts.GetContent`) mediante Android `FileProvider`.
* **Manejo de Estado reactivo:** `remember`, `mutableStateOf`, `mutableStateListOf`, corrutinas Compose (`LaunchedEffect`, `delay`).
* **Patrón de Nomenclatura:** Convención estándar internacional en inglés para entidades, modelos y controladores (`User`, `ProductOffer`, `CartItem`, `PurchaseVoucher`), conservando etiquetas e interfaz de usuario en español (CLP / Formato regional chileno).

---

## 3. Matriz de Roles y Permisos

La plataforma cuenta con un esquema de control de acceso basado en roles (**RBAC**):

| Rol | Registro y Acceso | Permisos en Catálogo | Carrito y Compra |
| :--- | :--- | :--- | :--- |
| **`BUYER` (Comprador)** | Registro simple. Activación inmediata al crear la cuenta. | Solo lectura. Visualiza ofertas en Inicio y Catálogo general. No puede crear ni editar productos. | Añade al carrito, reserva stock en tiempo real y emite vouchers digitales de retiro. |
| **`SELLER` (Vendedor)** | Registro extendido (datos comerciales). Requiere aprobación del administrador. | CRUD limitado: Solo puede editar, eliminar y activar ofertas sobre **sus propios productos** (`sellerId`). Su dirección y nombre de negocio se asocian automáticamente. | Puede interactuar como usuario consumidor. |
| **`ADMIN` (Administrador)** | Cuenta de control maestro. Capacidad para autorizar nuevos administradores o vendedores. | Acceso irrestricto: Crea, modifica y elimina cualquier producto del marketplace. | Acceso completo a todas las funciones y panel de aprobaciones. |

### Cuentas de Prueba Preconfiguradas

| Rol | Correo Electrónico | Contraseña | Estado | Características |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | `admin@costostock.cl` | `admin123` | `ACTIVE` | Acceso al panel de aprobaciones y control global |
| **Vendedor** | `vendedor@costostock.cl` | `vendedor123` | `ACTIVE` | *Supermercado Central Valparaíso* (`Av. Pedro Montt 1820`) |
| **Comprador** | `comprador@costostock.cl` | `comprador123` | `ACTIVE` | *Camila Soto* (`+56977665544`) |

---

## 4. Módulos y Funcionalidades Desarrolladas

### 4.1. Identidad Visual y Splash Screen Nativo
* **Splash Screen Adaptativo:** Configurado mediante `windowSplashScreenAnimatedIcon` en `themes.xml` y centrado vectorial con `layer-list` (`splash_logo.xml`) para prevenir cortes de bordes en pantallas circulares de Android 12+.
* **Launcher Icon:** Ícono corporativo adaptativo con imagotipo monetario (`$`) integrado con estética circular ecológica.
* **Paleta de Colores:** Fondo cálido corporativo (`#FFF9F5`), acentos primarios en naranja corporativo, verde sustentable (`#2E7D32`) y contenedores neutros de Material 3.

### 4.2. Autenticación y Flujo de Aprobaciones (`AuthScreen.kt`)
* Formulario modal que alterna entre inicio de sesión y registro de usuario.
* **Registro Comprador:** Nombre completo, teléfono, correo electrónico y contraseña.
* **Registro Vendedor:** Incluye además nombre del negocio, tipo de comercio, dirección de retiro, página web y redes sociales.
* **Panel Administrador:** Vista exclusiva para revisar, aprobar o rechazar solicitudes de vendedores pendientes (`PENDING_APPROVAL`).

### 4.3. Inicio y Ofertas Relámpago con Expiración (`HomeScreen.kt`)
* Sección de **Ofertas Destacadas** con contadores regresivos dinámicos (`HH:mm:ss`).
* Porcentaje de descuento calculado en tiempo real (`-% OFF`).
* Métricas de impacto: Indicador de $\text{kg de CO}_2$ prevenidos por unidad rescatada.
* **Expiración de Ofertas:** Cuando el temporizador llega a cero (`00:00:00`), el producto se retira automáticamente de la vitrina de ofertas de Inicio y vuelve a figurar como producto estándar en el catálogo general.

### 4.4. Catálogo, Gestión de Inventario y Cámara (`ProductsScreen.kt`)
* **Asociación Automática de Dirección:** Al crear un producto, la dirección de retiro y el nombre comercial se asignan automáticamente a partir de la ficha del vendedor autenticado.
* **Subida Multimedia Dual:**
  * **Cámara periférica:** Captura de fotografías en el momento mediante el contrato `TakePicture` y generación de URIs temporales seguras con `FileProvider`.
  * **Galería:** Selección de fotos locales con `GetContent("image/*")`.
  * **Renderizado Remoto:** Soporte para enlaces HTTPS de Google / bancos de imágenes mediante Coil.
* **Módulo de Oferta al Editar:** Al editar un producto, el vendedor puede activar el switch de oferta, ingresar el precio rebajado y definir la duración de la promoción **en minutos**. El sistema calcula el porcentaje de descuento y publica el ítem en la pantalla de inicio.

### 4.5. Carrito con Reserva de Stock y Emisión de Voucher (`CartScreen.kt`)
* **Reserva de Stock en Tiempo Real:**
  * Al agregar un ítem o presionar `+`, se descuenta 1 unidad del stock disponible en la base de datos local.
  * Al presionar `-` o eliminar un ítem, el stock reservado se reintegra de inmediato al inventario.
  * Al vaciar el carrito, se liberan todas las unidades reservadas.
* **Formateo de Moneda Local:** Todos los valores monetarios se formatean con separador de miles chileno (`$10.000`, `$1.990`).
* **Voucher Digital Consolidado:**
  * Generación de código único alfanumérico (`CS-XXXXXXXX`) y código QR simulado.
  * **Datos del Comprador:** Nombre, correo electrónico y teléfono del usuario en sesión.
  * **Puntos de Retiro:** Dirección exacta del comercio para cada lote adquirido.
  * **Consolidación Ambiental:** Sumatoria total de huella de carbono ahorrada.

---

## 5. Estructura del Código Fuente

```text
app/src/main/
├── AndroidManifest.xml                  # Permisos de red, cámara y configuración de FileProvider
├── java/com/example/costostock/
│   ├── MainActivity.kt                  # Punto de entrada, Scaffold, estados globales y datos semilla
│   ├── ui/theme/                        # Temas, tipografías y paleta de colores Material 3
│   └── view/
│       ├── AuthScreen.kt                # Login, registro segmentado y panel de aprobación Admin
│       ├── HomeScreen.kt                # Carrusel/listado de ofertas destacadas con cuenta regresiva
│       ├── ProductsScreen.kt            # Catálogo CRUD, toma de fotografías y configuración de ofertas
│       └── CartScreen.kt                # Carrito interactivo con control de reserva y emisión de voucher
└── res/
    ├── drawable/                        # splash_logo.xml, recursos vectoriales y logotipos
    ├── mipmap/                          # Iconos adaptativos de la aplicación
    ├── values/                          # strings.xml, themes.xml (Splash API)
    └── xml/
        └── file_paths.xml               # Rutas de almacenamiento temporal seguro para fotos de cámara
```

---

## 6. Configuración y Ejecución

1. **Requisitos:**
   * Android Studio Ladybug / Iguana o superior.
   * JDK 17 o superior.
   * Dispositivo físico o emulador con Android 8.0 (API 26) o superior (recomendado Android 12+ para Splash Screen).

2. **Dependencias clave en `app/build.gradle.kts`:**
   ```kotlin
   dependencies {
       implementation("io.coil-kt:coil-compose:2.6.0")
       implementation("androidx.compose.material3:material3:1.2.1")
       implementation("androidx.compose.material:material-icons-extended:1.6.4")
   }
   ```

3. **Ejecutar el proyecto:**
   * Abrir el proyecto en Android Studio.
   * Sincronizar Gradle (`Sync Project with Gradle Files`).
   * Ejecutar en el dispositivo objetivo (`Shift + F10`).
