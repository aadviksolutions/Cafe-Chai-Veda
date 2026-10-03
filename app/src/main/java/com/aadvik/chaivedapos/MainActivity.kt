@file:OptIn(ExperimentalMaterial3Api::class)

package com.aadvik.chaivedapos

import android.app.DatePickerDialog
import android.os.Bundle
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build

import android.widget.Toast
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.heightIn
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import com.aadvik.chaivedapos.data.DatabaseProvider
import com.aadvik.chaivedapos.data.MenuProductEntity
import kotlinx.coroutines.launch
import com.aadvik.chaivedapos.R
import com.aadvik.chaivedapos.data.BillEntity
import com.aadvik.chaivedapos.data.BillItemEntity
import com.aadvik.chaivedapos.data.BillDao
import com.aadvik.chaivedapos.data.BillItemDao
import com.aadvik.chaivedapos.data.InventoryItemEntity
import com.aadvik.chaivedapos.data.TableEntity
import com.aadvik.chaivedapos.data.PrinterSettingsEntity
import com.aadvik.chaivedapos.data.RestaurantSettingsEntity
import com.aadvik.chaivedapos.bluetooth.BluetoothPrinterManager

// ============================================================
// COLORS
// ============================================================

private val Cream = Color(0xFFFFFCFA)
private val DeepBrown = Color(0xFF3E2723)
private val TeaBrown = Color(0xFF6D4C41)
private val Peach = Color(0xFFFF8A70)
private val LightPeach = Color(0xFFFFE5DE)
private val Green = Color(0xFF71866B)
private val LightGreen = Color(0xFFEAF1E8)
private val LightBrown = Color(0xFFF4ECE7)
private val White = Color.White
private val Red = Color(0xFFD32F2F)
private val LightRed = Color(0xFFFFEBEE)
private val Blue = Color(0xFF1976D2)
private val LightBlue = Color(0xFFE3F2FD)

// ============================================================
// DATA MODELS
// ============================================================

data class MenuProduct(
    val id: Int,
    val name: String,
    val category: String,
    val price: Int,
    val foodType: String = "Veg",
    val available: Boolean = true,
    val imageUri: String = ""
)

data class CartItem(
    val product: MenuProduct,
    var quantity: Int
)

data class BillData(
    val billNumber: String,
    val customerName: String,
    val mobileNumber: String,
    val orderType: String,
    val tableNumber: String,
    val items: List<CartItem>,
    val subtotal: Int,
    val discount: Int,
    val tax: Int,
    val total: Int,
    var paymentStatus: String = "Unpaid",
    var paidAmount: Int = 0,
    var paymentMode: String = "",
    val dateTime: String = currentDateTime()
) {
    val dueAmount: Int
        get() = max(0, total - paidAmount)

}

data class InventoryItem(
    val id: Int,
    val name: String,
    val unit: String,
    val currentStock: Double,
    val minimumStock: Double
)

data class TableData(
    val id: Int,
    val name: String,
    val capacity: Int,
    val status: String = "Available"
)

data class RestaurantSettingsData(
    val name: String = "CHAI VEDA",
    val address: String = "",
    val mobile: String = "",
    val email: String = "",
    val gstin: String = "",
    val gstEnabled: Boolean = false,
    val gstPercent: Double = 5.0,
    val billHeader: String = "Café • Juice • & More",
    val billFooter: String = "Thank you. Visit again!"
)

data class PrinterSettingsData(
    val printerName: String = "",
    val paperSize: String = "58mm",
    val connected: Boolean = false,
    val autoPrint: Boolean = false,
    val billLogo: Boolean = true,
    val billHeader: String = "CHAI VEDA",
    val billFooter: String = "Thank you. Visit again!"
)

data class PaymentSettingsData(
    val cashEnabled: Boolean = true,
    val upiEnabled: Boolean = true,
    val cardEnabled: Boolean = true,
    val otherEnabled: Boolean = true,
    val upiId: String = "",
    val defaultMode: String = "Cash"
)

// ============================================================
// DEFAULT MENU
// ============================================================

private val defaultMenuProducts = listOf(

    MenuProduct(1, "Masala Chai", "Chai", 40),
    MenuProduct(2, "Ginger Chai", "Chai", 50),
    MenuProduct(3, "Elaichi Chai", "Chai", 50),
    MenuProduct(4, "Kulhad Chai", "Chai", 60),

    MenuProduct(5, "Hot Coffee", "Coffee", 80),
    MenuProduct(6, "Cold Coffee", "Coffee", 120),
    MenuProduct(7, "Cappuccino", "Coffee", 130),
    MenuProduct(8, "Cafe Latte", "Coffee", 140),

    MenuProduct(9, "Veg Sandwich", "Snacks", 100),
    MenuProduct(10, "Cheese Sandwich", "Snacks", 130),
    MenuProduct(11, "French Fries", "Snacks", 110),
    MenuProduct(12, "Paneer Tikka", "Snacks", 180),

    MenuProduct(13, "Fresh Lime", "Juice", 70),
    MenuProduct(14, "Orange Juice", "Juice", 100),
    MenuProduct(15, "Mango Shake", "Juice", 130),
    MenuProduct(16, "Watermelon Juice", "Juice", 90)
)

// ============================================================
// ACTIVITY
// ============================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ChaiVedaTheme {
                ChaiVedaApp()
            }
        }
    }
}

// ============================================================
// THEME
// ============================================================

@Composable
fun ChaiVedaTheme(
    content: @Composable () -> Unit
) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Peach,
            secondary = TeaBrown,
            background = Cream,
            surface = White,
            onPrimary = White,
            onSecondary = White,
            onBackground = DeepBrown,
            onSurface = DeepBrown
        ),
        content = content
    )
}

// ============================================================
// APP SCREENS
// ============================================================

private enum class AppScreen {
    WELCOME,
    DASHBOARD,
    POS,
    BILL_GENERATED,
    EDIT_BILL,
    SETTLEMENT,
    REPORTS,
    SETTINGS,
    MENU_MANAGEMENT,
    INVENTORY,
    TABLE_MANAGEMENT,
    RESTAURANT_DETAILS,
    PRINTER_SETTINGS,
    PAYMENT_SETTINGS,
    BACKUP_RESTORE
}

// ============================================================
// MAIN APP
// ============================================================

@Composable
fun ChaiVedaApp() {

    val context = LocalContext.current

    val database = remember {
        DatabaseProvider.getDatabase(context)
    }

    val menuProductDao = database.menuProductDao()
    val billDao = database.billDao()
    val billItemDao = database.billItemDao()
    val inventoryItemDao = database.inventoryItemDao()
    val tableDao = database.tableDao()
    val printerSettingsDao = database.printerSettingsDao()
    val restaurantSettingsDao = database.restaurantSettingsDao()

    val unpaidBillEntities by billDao
        .getUnpaidBills()
        .collectAsState(initial = emptyList())


    var currentScreen by remember {
        mutableStateOf(AppScreen.WELCOME)
    }

    var selectedBill by remember {
        mutableStateOf<BillData?>(null)
    }

    var editingBill by remember {
        mutableStateOf<BillData?>(null)
    }

    val unpaidBills = remember {
        mutableStateListOf<BillData>()
    }

    val menuProducts = remember {
        mutableStateListOf<MenuProduct>()
    }

    val menuProductsFromDatabase by menuProductDao
        .getAllProducts()
        .collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(menuProductsFromDatabase) {
        menuProducts.clear()
        menuProducts.addAll(
            menuProductsFromDatabase.map { entity ->
                MenuProduct(
                    id = entity.id,
                    name = entity.name,
                    category = entity.category,
                    price = entity.price.toInt(),
                    foodType = entity.foodType,
                    available = entity.available,
                    imageUri = entity.imageUri
                )
            }
        )
    }

    LaunchedEffect(menuProductDao) {
        val count = menuProductDao.getProductCount()

        if (count == 0) {
            defaultMenuProducts.forEach { product ->
                menuProductDao.insertProduct(
                    MenuProductEntity(
                        name = product.name,
                        category = product.category,
                        price = product.price.toDouble(),
                        foodType = product.foodType,
                        available = product.available,
                        imageUri = product.imageUri
                    )
                )
            }
        }
    }

    val inventoryItemsFromDatabase by inventoryItemDao
        .getAllItems()
        .collectAsState(initial = emptyList())

    val inventoryItems = inventoryItemsFromDatabase.map { entity ->
        InventoryItem(
            id = entity.id,
            name = entity.name,
            unit = entity.unit,
            currentStock = entity.currentStock,
            minimumStock = entity.minimumStock
        )
    }
    val restaurantSettingsFromDatabase by restaurantSettingsDao
        .getSettings()
        .collectAsState(initial = null)

    val restaurantSettings = remember { mutableStateOf(RestaurantSettingsData()) }

    LaunchedEffect(restaurantSettingsFromDatabase) {
        val saved = restaurantSettingsFromDatabase
        if (saved != null) {
            restaurantSettings.value = RestaurantSettingsData(
                name = saved.name,
                address = saved.address,
                mobile = saved.mobile,
                email = saved.email,
                gstin = saved.gstin,
                gstEnabled = saved.gstEnabled,
                gstPercent = saved.gstPercent,
                billHeader = saved.billHeader,
                billFooter = saved.billFooter
            )
        }
    }

    LaunchedEffect(unpaidBillEntities) {
        val loadedBills = unpaidBillEntities.map { entity ->
            val itemEntities = billItemDao.getItemsForBill(entity.id)
            BillData(
                billNumber = entity.billNumber,
                customerName = entity.customerName,
                mobileNumber = entity.mobileNumber,
                orderType = entity.orderType,
                tableNumber = entity.tableNumber,
                items = itemEntities.map { item ->
                    CartItem(
                        product = MenuProduct(
                            id = item.productId,
                            name = item.productName,
                            category = item.category,
                            price = item.price.toInt(),
                            foodType = "",
                            available = true
                        ),
                        quantity = item.quantity
                    )
                },
                subtotal = entity.subtotal.toInt(),
                discount = entity.discount.toInt(),
                tax = entity.tax.toInt(),
                total = entity.total.toInt(),
                paymentStatus = entity.paymentStatus,
                paidAmount = entity.paidAmount.toInt(),
                paymentMode = entity.paymentMode,
                dateTime = SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
                ).format(Date(entity.billDateTime))
            )
        }

        unpaidBills.clear()
        unpaidBills.addAll(loadedBills)
    }

    val printerSettingsFromDatabase by printerSettingsDao
        .getSettings()
        .collectAsState(initial = null)

    val printerSettings = remember {
        mutableStateOf(PrinterSettingsData())
    }

    LaunchedEffect(printerSettingsFromDatabase) {
        val saved = printerSettingsFromDatabase

        if (saved != null) {
            printerSettings.value = PrinterSettingsData(
                printerName = saved.printerName,
                paperSize = saved.paperSize,
                connected = saved.connected,
                autoPrint = saved.autoPrint,
                billLogo = saved.billLogo,
                billHeader = saved.billHeader,
                billFooter = saved.billFooter
            )
        }
    }

    val paymentSettings = remember { mutableStateOf(PaymentSettingsData()) }
    val tablesFromDatabase by tableDao
        .getAllTables()
        .collectAsState(initial = emptyList())

    val tables = tablesFromDatabase.map { entity ->
        TableData(
            id = entity.id,
            name = entity.name,
            capacity = entity.capacity,
            status = entity.status
        )
    }

    var showExitDialog by remember {
        mutableStateOf(false)
    }

    BackHandlerForScreen(
        currentScreen = currentScreen,
        onDashboard = {
            currentScreen = AppScreen.DASHBOARD
        },
        onExit = {
            showExitDialog = true
        }
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (currentScreen) {

            AppScreen.WELCOME -> {

                WelcomeScreen(
                    onFinished = {
                        currentScreen = AppScreen.DASHBOARD
                    }
                )
            }

            AppScreen.DASHBOARD -> {

                DashboardScreen(
                    unpaidCount = unpaidBills.count {
                        it.paymentStatus != "Paid"
                    },

                    onNewBill = {
                        currentScreen = AppScreen.POS
                    },

                    onSettlement = {
                        currentScreen = AppScreen.SETTLEMENT
                    },

                    onReports = {
                        currentScreen = AppScreen.REPORTS
                    },

                    onSettings = {
                        currentScreen = AppScreen.SETTINGS
                    },

                    onMenuManagement = {
                        currentScreen = AppScreen.MENU_MANAGEMENT
                    },

                    onInventory = {
                        currentScreen = AppScreen.INVENTORY
                    },

                    onTableManagement = {
                        currentScreen = AppScreen.TABLE_MANAGEMENT
                    }
                )
            }

            AppScreen.POS -> {

                POSScreen(
                    products = menuProducts,
                    tables = tables,

                    onBack = {
                        currentScreen = AppScreen.DASHBOARD
                    },

                    onBillGenerated = { bill ->

                        coroutineScope.launch {

                            val billDateTime = System.currentTimeMillis()
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = billDateTime
                            }

                            val monthStart = (calendar.clone() as Calendar).apply {
                                set(Calendar.DAY_OF_MONTH, 1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis

                            val monthEnd = (calendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                                set(Calendar.DAY_OF_MONTH, 1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                                add(Calendar.MILLISECOND, -1)
                            }.timeInMillis

                            val lastSequence = billDao.getLastBillSequence(
                                monthStart,
                                monthEnd
                            ) ?: 0

                            val finalBill = bill.copy(
                                billNumber = "CV-${(lastSequence + 1).toString().padStart(4, '0')}"
                            )

                            val billId = billDao.insertBill(
                                BillEntity(
                                    billNumber = finalBill.billNumber,
                                    billDateTime = billDateTime,
                                    customerName = finalBill.customerName,
                                    mobileNumber = finalBill.mobileNumber,
                                    orderType = finalBill.orderType,
                                    tableNumber = finalBill.tableNumber,
                                    subtotal = finalBill.subtotal.toDouble(),
                                    discount = finalBill.discount.toDouble(),
                                    tax = finalBill.tax.toDouble(),
                                    total = finalBill.total.toDouble(),
                                    paidAmount = finalBill.paidAmount.toDouble(),
                                    paymentStatus = finalBill.paymentStatus,
                                    paymentMode = finalBill.paymentMode
                                )
                            ).toInt()

                            if (finalBill.items.isNotEmpty()) {
                                billItemDao.insertBillItems(
                                    finalBill.items.map { item ->
                                        BillItemEntity(
                                            billId = billId,
                                            productId = item.product.id,
                                            productName = item.product.name,
                                            category = item.product.category,
                                            quantity = item.quantity,
                                            price = item.product.price.toDouble(),
                                            amount = (item.product.price * item.quantity).toDouble()
                                        )
                                    }
                                )
                            }

                            selectedBill = finalBill

                            if (finalBill.paymentStatus != "Paid") {
                                unpaidBills.add(finalBill)
                            }

                            currentScreen = AppScreen.BILL_GENERATED
                        }
                    }
                )
            }

            AppScreen.BILL_GENERATED -> {

                val bill = selectedBill

                if (bill != null) {

                    BillGeneratedScreen(
                        bill = bill,

                        onBackDashboard = {
                            currentScreen = AppScreen.DASHBOARD
                        },

                        onSettlePayment = {
                            currentScreen = AppScreen.SETTLEMENT
                        },

                        onEditBill = {
                            if (bill.paymentStatus != "Paid") {
                                editingBill = bill
                                currentScreen = AppScreen.EDIT_BILL
                            }
                        },

                        onPrint = {
                            // Printer will be connected later.
                        }
                    )
                }
            }

            AppScreen.EDIT_BILL -> {

                val bill = editingBill

                if (bill != null && bill.paymentStatus != "Paid") {
                    EditBillScreen(
                        bill = bill,
                        products = menuProducts,
                        tables = tables,
                        onBack = {
                            editingBill = null
                            currentScreen = AppScreen.BILL_GENERATED
                        },
                        onSave = { updatedBill ->
                            coroutineScope.launch {
                                val existing = billDao.getBillByNumber(updatedBill.billNumber)

                                val updatedPaid = minOf(updatedBill.paidAmount, updatedBill.total)
                                val updatedStatus = when {
                                    updatedPaid >= updatedBill.total -> "Paid"
                                    updatedPaid > 0 -> "Partially Paid"
                                    else -> "Unpaid"
                                }

                                val finalBill = updatedBill.copy(
                                    paidAmount = updatedPaid,
                                    paymentStatus = updatedStatus
                                )

                                if (existing != null) {
                                    billDao.updateBill(
                                        existing.copy(
                                            customerName = finalBill.customerName,
                                            mobileNumber = finalBill.mobileNumber,
                                            orderType = finalBill.orderType,
                                            tableNumber = finalBill.tableNumber,
                                            subtotal = finalBill.subtotal.toDouble(),
                                            discount = finalBill.discount.toDouble(),
                                            tax = finalBill.tax.toDouble(),
                                            total = finalBill.total.toDouble(),
                                            paidAmount = finalBill.paidAmount.toDouble(),
                                            paymentStatus = finalBill.paymentStatus,
                                            paymentMode = finalBill.paymentMode
                                        )
                                    )

                                    billItemDao.deleteItemsForBill(existing.id)

                                    billItemDao.insertBillItems(
                                        finalBill.items.map { item ->
                                            BillItemEntity(
                                                billId = existing.id,
                                                productId = item.product.id,
                                                productName = item.product.name,
                                                category = item.product.category,
                                                quantity = item.quantity,
                                                price = item.product.price.toDouble(),
                                                amount = (item.product.price * item.quantity).toDouble()
                                            )
                                        }
                                    )
                                }

                                val index = unpaidBills.indexOfFirst { it.billNumber == finalBill.billNumber }
                                if (index >= 0) unpaidBills[index] = finalBill

                                selectedBill = finalBill
                                editingBill = null
                                currentScreen = AppScreen.BILL_GENERATED
                            }
                        }
                    )
                }
            }

            AppScreen.SETTLEMENT -> {

                SettlementScreen(
                    bills = unpaidBills,

                    onBack = {
                        currentScreen = AppScreen.DASHBOARD
                    },

                    onPaymentDone = { bill, amount, mode ->

                        val index = unpaidBills.indexOfFirst {
                            it.billNumber == bill.billNumber
                        }

                        if (index >= 0) {

                            val oldBill = unpaidBills[index]

                            val newPaidAmount =
                                minOf(
                                    oldBill.paidAmount + amount,
                                    oldBill.total
                                )

                            val newStatus =
                                when {
                                    newPaidAmount >= oldBill.total -> "Paid"
                                    newPaidAmount > 0 -> "Partially Paid"
                                    else -> "Unpaid"
                                }

                            val updatedBill = oldBill.copy(
                                paymentStatus = newStatus,
                                paidAmount = newPaidAmount,
                                paymentMode = mode
                            )

                            coroutineScope.launch {
                                billDao.updatePayment(
                                    billNumber = updatedBill.billNumber,
                                    paidAmount = updatedBill.paidAmount.toDouble(),
                                    paymentStatus = updatedBill.paymentStatus,
                                    paymentMode = updatedBill.paymentMode
                                )
                            }

                            unpaidBills[index] = updatedBill
                            selectedBill = updatedBill
                        }
                    }
                )
            }

            AppScreen.REPORTS -> {

                ReportsScreen(
                    billDao = billDao,
                    billItemDao = billItemDao,
                    onBack = {
                        currentScreen = AppScreen.DASHBOARD
                    }
                )
            }

            AppScreen.SETTINGS -> {

                SettingsScreen(
                    onBack = { currentScreen = AppScreen.DASHBOARD },
                    onMenuManagement = { currentScreen = AppScreen.MENU_MANAGEMENT },
                    onInventory = { currentScreen = AppScreen.INVENTORY },
                    onTableManagement = { currentScreen = AppScreen.TABLE_MANAGEMENT },
                    onRestaurantDetails = { currentScreen = AppScreen.RESTAURANT_DETAILS },
                    onPrinterSettings = { currentScreen = AppScreen.PRINTER_SETTINGS },
                    onPaymentSettings = { currentScreen = AppScreen.PAYMENT_SETTINGS },
                    onBackupRestore = { currentScreen = AppScreen.BACKUP_RESTORE }
                )
            }

            AppScreen.MENU_MANAGEMENT -> {

                MenuManagementScreen(
                    products = menuProducts,

                    onBack = {
                        currentScreen = AppScreen.SETTINGS
                    },

                    onAddProduct = { product ->

                        coroutineScope.launch {
                            menuProductDao.insertProduct(
                                MenuProductEntity(
                                    name = product.name,
                                    category = product.category,
                                    price = product.price.toDouble(),
                                    foodType = product.foodType,
                                    available = product.available,
                                    imageUri = product.imageUri
                                )
                            )
                        }
                    },

                    onUpdateProduct = { product ->

                        coroutineScope.launch {
                            menuProductDao.updateProduct(
                                MenuProductEntity(
                                    id = product.id,
                                    name = product.name,
                                    category = product.category,
                                    price = product.price.toDouble(),
                                    foodType = product.foodType,
                                    available = product.available,
                                    imageUri = product.imageUri
                                )
                            )
                        }
                    },

                    onDeleteProduct = { product ->

                        coroutineScope.launch {
                            menuProductDao.deleteProduct(
                                MenuProductEntity(
                                    id = product.id,
                                    name = product.name,
                                    category = product.category,
                                    price = product.price.toDouble(),
                                    foodType = product.foodType,
                                    available = product.available,
                                    imageUri = product.imageUri
                                )
                            )
                        }
                    }
                )
            }

            AppScreen.INVENTORY -> {
                InventoryScreen(
                    items = inventoryItems,
                    onBack = { currentScreen = AppScreen.SETTINGS },
                    onAdd = { item ->
                        coroutineScope.launch {
                            inventoryItemDao.insertItem(
                                InventoryItemEntity(
                                    id = 0,
                                    name = item.name,
                                    unit = item.unit,
                                    currentStock = item.currentStock,
                                    minimumStock = item.minimumStock
                                )
                            )
                        }
                    },
                    onUpdate = { item ->
                        coroutineScope.launch {
                            inventoryItemDao.updateItem(
                                InventoryItemEntity(
                                    id = item.id,
                                    name = item.name,
                                    unit = item.unit,
                                    currentStock = item.currentStock,
                                    minimumStock = item.minimumStock
                                )
                            )
                        }
                    },
                    onDelete = { item ->
                        coroutineScope.launch {
                            inventoryItemDao.deleteItem(
                                InventoryItemEntity(
                                    id = item.id,
                                    name = item.name,
                                    unit = item.unit,
                                    currentStock = item.currentStock,
                                    minimumStock = item.minimumStock
                                )
                            )
                        }
                    }
                )
            }

            AppScreen.TABLE_MANAGEMENT -> {
                TableManagementScreen(
                    tables = tables,
                    onBack = { currentScreen = AppScreen.SETTINGS },
                    onAdd = { table ->
                        coroutineScope.launch {
                            tableDao.insertTable(
                                TableEntity(
                                    id = 0,
                                    name = table.name,
                                    capacity = table.capacity,
                                    status = table.status
                                )
                            )
                        }
                    },
                    onUpdate = { table ->
                        coroutineScope.launch {
                            tableDao.updateTable(
                                TableEntity(
                                    id = table.id,
                                    name = table.name,
                                    capacity = table.capacity,
                                    status = table.status
                                )
                            )
                        }
                    },
                    onDelete = { table ->
                        coroutineScope.launch {
                            tableDao.deleteTable(
                                TableEntity(
                                    id = table.id,
                                    name = table.name,
                                    capacity = table.capacity,
                                    status = table.status
                                )
                            )
                        }
                    }
                )
            }

            AppScreen.RESTAURANT_DETAILS -> {
                RestaurantDetailsScreen(
                    settings = restaurantSettings.value,
                    onBack = { currentScreen = AppScreen.SETTINGS },
                    onSave = { settings ->
                        restaurantSettings.value = settings
                        coroutineScope.launch {
                            restaurantSettingsDao.insertSettings(
                                RestaurantSettingsEntity(
                                    id = 1,
                                    name = settings.name,
                                    address = settings.address,
                                    mobile = settings.mobile,
                                    email = settings.email,
                                    gstin = settings.gstin,
                                    gstEnabled = settings.gstEnabled,
                                    gstPercent = settings.gstPercent,
                                    billHeader = settings.billHeader,
                                    billFooter = settings.billFooter
                                )
                            )
                        }
                    }
                )
            }

            AppScreen.PRINTER_SETTINGS -> {
                PrinterSettingsScreen(
                    settings = printerSettings.value,
                    onBack = { currentScreen = AppScreen.SETTINGS },
                    onSave = { settings ->
                        printerSettings.value = settings

                        coroutineScope.launch {
                            printerSettingsDao.insertSettings(
                                PrinterSettingsEntity(
                                    id = 1,
                                    printerName = settings.printerName,
                                    paperSize = settings.paperSize,
                                    connected = settings.connected,
                                    autoPrint = settings.autoPrint,
                                    billLogo = settings.billLogo,
                                    billHeader = settings.billHeader,
                                    billFooter = settings.billFooter
                                )
                            )
                        }
                    }
                )
            }

            AppScreen.PAYMENT_SETTINGS -> {
                PaymentSettingsScreen(
                    settings = paymentSettings.value,
                    onBack = { currentScreen = AppScreen.SETTINGS },
                    onSave = { paymentSettings.value = it }
                )
            }

            AppScreen.BACKUP_RESTORE -> {
                BackupRestoreScreen(
                    menuCount = menuProducts.size,
                    inventoryCount = inventoryItems.size,
                    tableCount = tables.size,
                    restaurantSettings = restaurantSettings.value,
                    paymentSettings = paymentSettings.value,
                    onBack = { currentScreen = AppScreen.SETTINGS }
                )
            }
        }

        if (showExitDialog) {

            AlertDialog(

                onDismissRequest = {
                    showExitDialog = false
                },

                title = {
                    Text(
                        text = "Exit Chai Veda POS?",
                        fontWeight = FontWeight.Bold
                    )
                },

                text = {
                    Text(
                        text = "Are you sure you want to exit the application?"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            showExitDialog = false
                        }
                    ) {
                        Text(
                            text = "Exit",
                            color = Red
                        )
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showExitDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ============================================================
// WELCOME
// ============================================================

@Composable
fun WelcomeScreen(
    onFinished: () -> Unit
) {

    LaunchedEffect(Unit) {

        delay(3000)

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Image(
            painter = painterResource(
                id = R.drawable.chaiveda_welcome
            ),

            contentDescription = "Chai Veda Welcome Screen",

            modifier = Modifier.fillMaxSize(),

            contentScale = ContentScale.FillBounds
        )
    }
}

// ============================================================
// DASHBOARD
// ============================================================

@Composable
fun DashboardScreen(
    unpaidCount: Int,
    onNewBill: () -> Unit,
    onSettlement: () -> Unit,
    onReports: () -> Unit,
    onSettings: () -> Unit,
    onMenuManagement: () -> Unit,
    onInventory: () -> Unit,
    onTableManagement: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CHAI VEDA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = "Café • Juice • & More",
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 10.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = White.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = "♙",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            fontSize = 21.sp,
                            color = DeepBrown
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = LightPeach)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Good Morning 👋",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBrown
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Ready for today's billing?",
                                fontSize = 15.sp,
                                color = TeaBrown
                            )
                        }
                        Text("☕", fontSize = 64.sp)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNewBill() },
                    shape = RoundedCornerShape(23.dp),
                    colors = CardDefaults.cardColors(containerColor = Peach),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧾", fontSize = 43.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("New Order", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                            Text("Create customer order", fontSize = 14.sp, color = White.copy(alpha = 0.9f))
                        }
                        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = White.copy(alpha = 0.22f)) {
                            Text("→", modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp), fontSize = 25.sp, color = White)
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardQuickCard(
                        title = "Payment Settlement",
                        subtitle = if (unpaidCount > 0) "$unpaidCount pending bill(s)" else "Settle unpaid and partial bills",
                        icon = "₹",
                        background = LightGreen,
                        onClick = onSettlement,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardQuickCard(
                        title = "Reports",
                        subtitle = "Sales and collection reports",
                        icon = "▥",
                        background = White,
                        onClick = onReports,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Access", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
                    Spacer(Modifier.weight(1f))
                    Text("⠿", fontSize = 26.sp, color = TeaBrown)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardSmallCard("Settings", "Menu, printer, GST and more", "⚙", onSettings, Modifier.weight(1f))
                    DashboardSmallCard("Menu", "Add and manage items", "🍴", onMenuManagement, Modifier.weight(1f))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardSmallCard("Tables", "Manage table booking", "▱", onTableManagement, Modifier.weight(1f))
                    DashboardSmallCard("Inventory", "Stock and ingredients", "▣", onInventory, Modifier.weight(1f))
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreen)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("●", color = Green, fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Offline Mode", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepBrown)
                            Text("All data saved on device", fontSize = 12.sp, color = TeaBrown)
                        }
                        Text("☁̸", fontSize = 25.sp, color = TeaBrown)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardQuickCard(
    title: String,
    subtitle: String,
    icon: String,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (background == LightGreen) White.copy(alpha = 0.7f) else LightPeach
            ) {
                Text(icon, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 25.sp, color = if (background == LightGreen) Green else Peach)
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 12.sp, color = TeaBrown, maxLines = 2)
        }
    }
}

@Composable
private fun DashboardSmallCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = LightBrown) {
                Text(icon, modifier = Modifier.padding(11.dp), fontSize = 22.sp, color = DeepBrown)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
                Text(subtitle, fontSize = 10.sp, color = TeaBrown, maxLines = 2)
            }
            Text("→", fontSize = 20.sp, color = TeaBrown)
        }
    }
}

// ============================================================
// DASHBOARD BUTTON
// ============================================================

@Composable
fun DashboardButton(
    title: String,
    subtitle: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(
            containerColor = background
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = if (textColor == White) {
                    White.copy(alpha = 0.85f)
                } else {
                    TeaBrown
                }
            )
        }
    }
}

// ============================================================
// SMALL DASHBOARD CARD
// ============================================================

@Composable
fun SmallDashboardCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TeaBrown
            )
        }
    }
}

// ============================================================
// REPORTS
// ============================================================

private enum class ReportPeriod(
    val title: String
) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Range")
}

private data class ReportItemSummary(
    val quantity: Int,
    val amount: Double
)

@Composable
fun ReportsScreen(
    billDao: BillDao,
    billItemDao: BillItemDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }
    var fromDate by remember { mutableStateOf(Calendar.getInstance()) }
    var toDate by remember { mutableStateOf(Calendar.getInstance()) }

    fun startOfDay(calendar: Calendar): Calendar =
        (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    fun endOfDay(calendar: Calendar): Calendar =
        (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

    fun applyPreset(period: ReportPeriod) {
        val now = Calendar.getInstance()
        when (period) {
            ReportPeriod.TODAY -> {
                fromDate = startOfDay(now)
                toDate = endOfDay(now)
            }
            ReportPeriod.YESTERDAY -> {
                val day = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                fromDate = startOfDay(day)
                toDate = endOfDay(day)
            }
            ReportPeriod.THIS_WEEK -> {
                val start = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                fromDate = startOfDay(start)
                val end = (start.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 6)
                }
                toDate = endOfDay(end)
            }
            ReportPeriod.THIS_MONTH -> {
                val start = (now.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                fromDate = startOfDay(start)
                val end = (start.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }
                toDate = endOfDay(end)
            }
            ReportPeriod.CUSTOM -> Unit
        }
    }

    fun openDatePicker(isFrom: Boolean) {
        val selected = if (isFrom) fromDate else toDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                if (isFrom) {
                    fromDate = startOfDay(picked)
                    if (fromDate.timeInMillis > toDate.timeInMillis) {
                        toDate = endOfDay(picked)
                    }
                } else {
                    toDate = endOfDay(picked)
                    if (toDate.timeInMillis < fromDate.timeInMillis) {
                        fromDate = startOfDay(picked)
                    }
                }
                selectedPeriod = ReportPeriod.CUSTOM
            },
            selected.get(Calendar.YEAR),
            selected.get(Calendar.MONTH),
            selected.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(selectedPeriod) {
        if (selectedPeriod != ReportPeriod.CUSTOM) {
            applyPreset(selectedPeriod)
        }
    }

    val startTime = fromDate.timeInMillis
    val endTime = toDate.timeInMillis

    val bills by billDao
        .getBillsBetween(startTime, endTime)
        .collectAsState(initial = emptyList())

    val totalSales = bills.sumOf { it.total }
    val totalCollection = bills.sumOf { it.paidAmount }
    val totalDue = bills.sumOf { max(0.0, it.total - it.paidAmount) }
    val billCount = bills.size
    val paidBills = bills.count { it.paymentStatus == "Paid" }
    val partialBills = bills.count { it.paymentStatus == "Partially Paid" }
    val unpaidBillsCount = bills.count { it.paymentStatus == "Unpaid" }

    var itemSales by remember {
        mutableStateOf(emptyMap<String, ReportItemSummary>())
    }

    var paymentSummary by remember {
        mutableStateOf(emptyMap<String, Pair<Int, Double>>())
    }

    LaunchedEffect(bills) {
        val itemsResult = mutableMapOf<String, ReportItemSummary>()
        val paymentResult = mutableMapOf<String, Pair<Int, Double>>()

        bills.forEach { bill ->
            val items = billItemDao.getItemsForBill(bill.id)
            items.forEach { item ->
                val old = itemsResult[item.productName]
                itemsResult[item.productName] = ReportItemSummary(
                    quantity = (old?.quantity ?: 0) + item.quantity,
                    amount = (old?.amount ?: 0.0) + item.amount
                )
            }

            val mode = bill.paymentMode.trim().ifBlank { "Not Paid / Other" }
            val oldPayment = paymentResult[mode]
            paymentResult[mode] = Pair(
                (oldPayment?.first ?: 0) + 1,
                (oldPayment?.second ?: 0.0) + bill.paidAmount
            )
        }

        itemSales = itemsResult
        paymentSummary = paymentResult
    }

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    val periodText = if (selectedPeriod == ReportPeriod.CUSTOM) {
        "${dateFormat.format(fromDate.time)} - ${dateFormat.format(toDate.time)}"
    } else {
        when (selectedPeriod) {
            ReportPeriod.TODAY -> dateFormat.format(fromDate.time)
            ReportPeriod.YESTERDAY -> dateFormat.format(fromDate.time)
            ReportPeriod.THIS_WEEK ->
                "${dateFormat.format(fromDate.time)} - ${dateFormat.format(toDate.time)}"
            ReportPeriod.THIS_MONTH ->
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(fromDate.time)
            ReportPeriod.CUSTOM -> ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reports", fontWeight = FontWeight.Bold)
                        Text(periodText, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Report Period",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ReportPeriod.values().toList()) { period ->
                                FilterChip(
                                    selected = selectedPeriod == period,
                                    onClick = {
                                        selectedPeriod = period
                                        if (period != ReportPeriod.CUSTOM) applyPreset(period)
                                    },
                                    label = { Text(period.title, fontSize = 12.sp) }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { openDatePicker(true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column {
                                    Text("From", fontSize = 11.sp)
                                    Text(dateFormat.format(fromDate.time), fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = { openDatePicker(false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Column {
                                    Text("To", fontSize = 11.sp)
                                    Text(dateFormat.format(toDate.time), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Business Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportSummaryCard("Sales", "₹${formatReportAmount(totalSales)}", Modifier.weight(1f), LightPeach)
                    ReportSummaryCard("Collection", "₹${formatReportAmount(totalCollection)}", Modifier.weight(1f), LightGreen)
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportSummaryCard("Bills", billCount.toString(), Modifier.weight(1f), White)
                    ReportSummaryCard("Due", "₹${formatReportAmount(totalDue)}", Modifier.weight(1f), LightRed)
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Payment Status", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepBrown)
                        Spacer(Modifier.height(8.dp))
                        ReportStatusRow("Paid Bills", paidBills)
                        ReportStatusRow("Partially Paid", partialBills)
                        ReportStatusRow("Unpaid Bills", unpaidBillsCount)
                    }
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Payment Category", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepBrown)
                        Spacer(Modifier.height(8.dp))
                        if (paymentSummary.isEmpty()) {
                            Text("No payment data for this period.", color = TeaBrown, fontSize = 13.sp)
                        } else {
                            paymentSummary.entries
                                .sortedByDescending { it.value.second }
                                .forEach { entry ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(entry.key, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = DeepBrown)
                                        Text("${entry.value.first} bills", fontSize = 12.sp, color = TeaBrown)
                                        Spacer(Modifier.width(10.dp))
                                        Text("₹${formatReportAmount(entry.value.second)}", fontWeight = FontWeight.Bold, color = DeepBrown)
                                    }
                                    HorizontalDivider()
                                }
                        }
                    }
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Item-wise Sales", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepBrown)
                        Spacer(Modifier.height(8.dp))
                        if (itemSales.isEmpty()) {
                            Text("No item sales recorded for this period.", color = TeaBrown, fontSize = 13.sp)
                        } else {
                            itemSales.entries
                                .sortedByDescending { it.value.amount }
                                .forEach { entry ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(entry.key, fontWeight = FontWeight.SemiBold, color = DeepBrown)
                                            Text("${entry.value.quantity} sold", fontSize = 11.sp, color = TeaBrown)
                                        }
                                        Text("₹${formatReportAmount(entry.value.amount)}", fontWeight = FontWeight.Bold, color = DeepBrown)
                                    }
                                    HorizontalDivider()
                                }
                        }
                    }
                }
            }

            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Bill-wise Report", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DeepBrown)
                        Spacer(Modifier.height(8.dp))
                        if (bills.isEmpty()) {
                            Text("No bills generated for this period.", color = TeaBrown, fontSize = 13.sp)
                        } else {
                            bills.forEach { bill ->
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                    Column(Modifier.weight(1f)) {
                                        Text(bill.billNumber, fontWeight = FontWeight.Bold, color = DeepBrown)
                                        Text(
                                            if (bill.customerName.isBlank()) "Walk-in Customer" else bill.customerName,
                                            fontSize = 12.sp,
                                            color = TeaBrown
                                        )
                                        Text(
                                            buildString {
                                                append(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.billDateTime)))
                                                append(" • ")
                                                append(bill.orderType)
                                                if (bill.tableNumber.isNotBlank()) append(" • Table ${bill.tableNumber}")
                                            },
                                            fontSize = 11.sp,
                                            color = TeaBrown
                                        )
                                        Text(
                                            "Payment: ${bill.paymentMode.ifBlank { "Not Paid" }}",
                                            fontSize = 11.sp,
                                            color = TeaBrown
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${formatReportAmount(bill.total)}", fontWeight = FontWeight.Bold, color = DeepBrown)
                                        Text("Paid ₹${formatReportAmount(bill.paidAmount)}", fontSize = 11.sp, color = Green)
                                        Text("Due ₹${formatReportAmount(max(0.0, bill.total - bill.paidAmount))}", fontSize = 11.sp, color = Red)
                                        Text(bill.paymentStatus, fontSize = 11.sp, color = when (bill.paymentStatus) {
                                            "Paid" -> Green
                                            "Partially Paid" -> Blue
                                            else -> Red
                                        })
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

@Composable
fun ReportSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    background: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = TeaBrown)
            Spacer(Modifier.height(5.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
        }
    }
}

@Composable
fun ReportStatusRow(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), color = TeaBrown)
        Text(count.toString(), fontWeight = FontWeight.Bold, color = DeepBrown)
    }
}

fun formatReportAmount(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}

// ============================================================
// SETTINGS
// ============================================================

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onMenuManagement: () -> Unit,
    onInventory: () -> Unit,
    onTableManagement: () -> Unit,
    onRestaurantDetails: () -> Unit,
    onPrinterSettings: () -> Unit,
    onPaymentSettings: () -> Unit,
    onBackupRestore: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    TextButton(
                        onClick = onBack
                    ) {
                        Text(
                            text = "Back",
                            color = White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    text = "Restaurant Settings",
                    fontSize = 14.sp,
                    color = TeaBrown,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                SettingsOption(
                    title = "Menu Management",
                    subtitle = "Add, edit, disable or delete menu items",
                    onClick = onMenuManagement
                )
            }

            item {

                SettingsOption(
                    title = "Inventory",
                    subtitle = "Stock in, stock out, units and low-stock control",
                    onClick = onInventory
                )
            }

            item {

                SettingsOption(
                    title = "Table Management",
                    subtitle = "Add tables, capacity and Available / Occupied / Reserved",
                    onClick = onTableManagement
                )
            }

            item {

                SettingsOption(
                    title = "Restaurant Details",
                    subtitle = "Café name, address, contact, GST and bill header/footer",
                    onClick = onRestaurantDetails
                )
            }

            item {

                SettingsOption(
                    title = "Printer Settings",
                    subtitle = "Printer name, paper size, connect, auto-print and test print",
                    onClick = onPrinterSettings
                )
            }

            item {

                SettingsOption(
                    title = "Payment Settings",
                    subtitle = "Enable modes, UPI ID and default payment mode",
                    onClick = onPaymentSettings
                )
            }

            item {

                SettingsOption(
                    title = "Backup & Restore",
                    subtitle = "Create a local backup summary and restore-ready data view",
                    onClick = onBackupRestore
                )
            }
        }
    }
}

// ============================================================
// SETTINGS OPTION
// ============================================================

@Composable
fun SettingsOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TeaBrown
            )
        }
    }
}

// ============================================================
// INVENTORY MANAGEMENT
// ============================================================

@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    onBack: () -> Unit,
    onAdd: (InventoryItem) -> Unit,
    onUpdate: (InventoryItem) -> Unit,
    onDelete: (InventoryItem) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<InventoryItem?>(null) }
    var stockItem by remember { mutableStateOf<InventoryItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = White) } },
                actions = { TextButton(onClick = { showAdd = true }) { Text("+ Add", color = White, fontWeight = FontWeight.Bold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Peach, titleContentColor = White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Cream).padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text("${items.size} stock item(s)", color = TeaBrown, fontSize = 13.sp) }
            items(items, key = { it.id }) { item ->
                InventoryCard(item, onEdit = { editing = item }, onStock = { stockItem = item }, onDelete = { onDelete(item) })
            }
            if (items.isEmpty()) item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
                    Text("No inventory items yet. Tap + Add to create one.", modifier = Modifier.padding(20.dp), color = TeaBrown)
                }
            }
        }
    }

    if (showAdd) InventoryEditorDialog("Add Stock Item", null, { showAdd = false }, { onAdd(it); showAdd = false })
    editing?.let { item -> InventoryEditorDialog("Edit Stock Item", item, { editing = null }, { onUpdate(it); editing = null }) }
    stockItem?.let { item -> StockAdjustmentDialog(item, { stockItem = null }, { updated -> onUpdate(updated); stockItem = null }) }
}

@Composable
fun InventoryCard(item: InventoryItem, onEdit: () -> Unit, onStock: () -> Unit, onDelete: () -> Unit) {
    val low = item.currentStock <= item.minimumStock
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBrown)
                    Text("Unit: ${item.unit}", fontSize = 12.sp, color = TeaBrown)
                }
                Surface(color = if (low) LightRed else LightGreen, shape = RoundedCornerShape(8.dp)) {
                    Text(if (low) "LOW STOCK" else "OK", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), color = if (low) Red else Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Current Stock: ${formatStock(item.currentStock)} ${item.unit}", fontWeight = FontWeight.Bold, color = DeepBrown)
            Text("Minimum Stock: ${formatStock(item.minimumStock)} ${item.unit}", fontSize = 12.sp, color = TeaBrown)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onStock) { Text("Stock In/Out", color = Green) }
                TextButton(onClick = onEdit) { Text("Edit", color = Blue) }
                TextButton(onClick = onDelete) { Text("Delete", color = Red) }
            }
        }
    }
}

@Composable
fun InventoryEditorDialog(title: String, item: InventoryItem?, onDismiss: () -> Unit, onSave: (InventoryItem) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "Kg") }
    var stock by remember { mutableStateOf(item?.currentStock?.toString() ?: "0") }
    var minimum by remember { mutableStateOf(item?.minimumStock?.toString() ?: "0") }
    val units = listOf("Kg", "Gram", "Litre", "Ml", "Piece")
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontWeight = FontWeight.Bold) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(name, { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Text("Unit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { units.forEach { u -> FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u, fontSize = 11.sp) }) } }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(stock, { stock = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Current Stock") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(minimum, { minimum = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Minimum Stock") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
    }, confirmButton = { TextButton(onClick = {
        val n = name.trim(); val st = stock.toDoubleOrNull() ?: 0.0; val min = minimum.toDoubleOrNull() ?: 0.0
        if (n.isNotEmpty()) onSave(InventoryItem(item?.id ?: generateInventoryId(), n, unit, st, min))
    }) { Text("Save", color = Green, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
fun StockAdjustmentDialog(item: InventoryItem, onDismiss: () -> Unit, onSave: (InventoryItem) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Stock In") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Stock Adjustment", fontWeight = FontWeight.Bold) }, text = {
        Column {
            Text("${item.name}: ${formatStock(item.currentStock)} ${item.unit}", color = TeaBrown)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = type == "Stock In", onClick = { type = "Stock In" }, label = { Text("Stock In") })
                FilterChip(selected = type == "Stock Out", onClick = { type = "Stock Out" }, label = { Text("Stock Out") })
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
    }, confirmButton = { TextButton(onClick = {
        val value = amount.toDoubleOrNull() ?: 0.0
        if (value > 0) {
            val newStock = if (type == "Stock In") item.currentStock + value else max(0.0, item.currentStock - value)
            onSave(item.copy(currentStock = newStock))
        }
    }) { Text("Apply", color = Green, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

// ============================================================
// TABLE MANAGEMENT
// ============================================================

@Composable
fun TableManagementScreen(tables: List<TableData>, onBack: () -> Unit, onAdd: (TableData) -> Unit, onUpdate: (TableData) -> Unit, onDelete: (TableData) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TableData?>(null) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Table Management", fontWeight = FontWeight.Bold) }, navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = White) } }, actions = { TextButton(onClick = { showAdd = true }) { Text("+ Add Table", color = White, fontWeight = FontWeight.Bold) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Peach, titleContentColor = White))
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().background(Cream).padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tables, key = { it.id }) { table ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(table.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DeepBrown); Text("Capacity: ${table.capacity}", fontSize = 12.sp, color = TeaBrown) }
                            Surface(color = tableStatusColor(table.status), shape = RoundedCornerShape(8.dp)) { Text(table.status, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { onUpdate(table.copy(status = nextTableStatus(table.status))) }) { Text("Change Status", color = Green) }
                            TextButton(onClick = { editing = table }) { Text("Edit", color = Blue) }
                            TextButton(onClick = { onDelete(table) }) { Text("Delete", color = Red) }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) TableEditorDialog("Add Table", null, { showAdd = false }, { onAdd(it); showAdd = false })
    editing?.let { t -> TableEditorDialog("Edit Table", t, { editing = null }, { onUpdate(it); editing = null }) }
}

@Composable
fun TableEditorDialog(title: String, table: TableData?, onDismiss: () -> Unit, onSave: (TableData) -> Unit) {
    var name by remember { mutableStateOf(table?.name ?: "") }
    var capacity by remember { mutableStateOf(table?.capacity?.toString() ?: "2") }
    var status by remember { mutableStateOf(table?.status ?: "Available") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, fontWeight = FontWeight.Bold) }, text = {
        Column {
            OutlinedTextField(name, { name = it }, label = { Text("Table Number / Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(capacity, { capacity = it.filter(Char::isDigit) }, label = { Text("Capacity") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Available", "Occupied", "Reserved").forEach { s -> FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s, fontSize = 11.sp) }) } }
        }
    }, confirmButton = { TextButton(onClick = { val n=name.trim(); val c=capacity.toIntOrNull() ?: 2; if(n.isNotEmpty() && c>0) onSave(TableData(table?.id ?: generateTableId(), n, c, status)) }) { Text("Save", color = Green, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

fun nextTableStatus(status: String) = when(status) { "Available" -> "Occupied"; "Occupied" -> "Reserved"; else -> "Available" }
fun tableStatusColor(status: String) = when(status) { "Occupied" -> LightRed; "Reserved" -> LightBlue; else -> LightGreen }

// ============================================================
// RESTAURANT DETAILS
// ============================================================

@Composable
fun RestaurantDetailsScreen(settings: RestaurantSettingsData, onBack: () -> Unit, onSave: (RestaurantSettingsData) -> Unit) {
    var name by remember { mutableStateOf(settings.name) }
    var address by remember { mutableStateOf(settings.address) }
    var mobile by remember { mutableStateOf(settings.mobile) }
    var email by remember { mutableStateOf(settings.email) }
    var gstin by remember { mutableStateOf(settings.gstin) }
    var gstEnabled by remember { mutableStateOf(settings.gstEnabled) }
    var gstPercent by remember { mutableStateOf(settings.gstPercent.toString()) }
    var header by remember { mutableStateOf(settings.billHeader) }
    var footer by remember { mutableStateOf(settings.billFooter) }
    Scaffold(topBar = { TopAppBar(title={Text("Restaurant Details", fontWeight=FontWeight.Bold)}, navigationIcon={TextButton(onClick=onBack){Text("Back",color=White)}}, colors=TopAppBarDefaults.topAppBarColors(containerColor=Peach,titleContentColor=White)) }) { padding ->
        Column(Modifier.fillMaxSize().background(Cream).padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(9.dp)) {
            OutlinedTextField(name,{name=it},label={Text("Café Name")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(address,{address=it},label={Text("Address")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(email,{email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(gstin,{gstin=it},label={Text("GSTIN")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Enable GST",Modifier.weight(1f),fontWeight=FontWeight.Bold);Switch(gstEnabled,{gstEnabled=it})}
            if(gstEnabled) OutlinedTextField(gstPercent,{gstPercent=it.filter{c->c.isDigit()||c=='.'}},label={Text("GST %")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(header,{header=it},label={Text("Bill Header")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(footer,{footer=it},label={Text("Bill Footer")},modifier=Modifier.fillMaxWidth())
            Button(onClick={onSave(RestaurantSettingsData(name.trim(),address.trim(),mobile.trim(),email.trim(),gstin.trim(),gstEnabled,gstPercent.toDoubleOrNull()?:5.0,header,footer));onBack()},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Peach)){Text("Save Restaurant Details")}
        }
    }
}

// ============================================================
// PRINTER SETTINGS
// ============================================================

@Composable
fun PrinterSettingsScreen(
    settings: PrinterSettingsData,
    onBack: () -> Unit,
    onSave: (PrinterSettingsData) -> Unit
) {
    val context = LocalContext.current
    val manager = remember { BluetoothPrinterManager(context.applicationContext) }

    var printerName by remember { mutableStateOf(settings.printerName) }
    var paperSize by remember { mutableStateOf(settings.paperSize) }
    var connected by remember { mutableStateOf(false) }
    var autoPrint by remember { mutableStateOf(settings.autoPrint) }
    var logo by remember { mutableStateOf(settings.billLogo) }
    var header by remember { mutableStateOf(settings.billHeader) }
    var footer by remember { mutableStateOf(settings.billFooter) }

    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var statusMessage by remember { mutableStateOf("Printer not connected") }
    var isWorking by remember { mutableStateOf(false) }

    val permissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            pairedDevices = manager.getPairedDevices()
            if (printerName.isNotBlank()) {
                val device = pairedDevices.firstOrNull { it.name == printerName }
                if (device != null) {
                    isWorking = true
                    manager.connect(
                        device = device,
                        onSuccess = {
                            connected = true
                            statusMessage = "Connected to ${device.name ?: "printer"}"
                            isWorking = false
                        },
                        onError = {
                            connected = false
                            statusMessage = it
                            isWorking = false
                        }
                    )
                }
            }
        }

    fun ensureBluetoothAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )

            val missing = permissions.filter {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }

            if (missing.isNotEmpty()) {
                permissionLauncher.launch(missing.toTypedArray())
                return
            }
        }

        pairedDevices = manager.getPairedDevices()
    }

    LaunchedEffect(Unit) {
        ensureBluetoothAccess()
    }

    DisposableEffect(Unit) {
        onDispose {
            manager.disconnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Printer Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                "Bluetooth Thermal Printer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepBrown
            )

            Text(
                "Printer ko phone ke Bluetooth settings me pehle pair karna zaroori hai.",
                fontSize = 12.sp,
                color = TeaBrown
            )

            Button(
                onClick = {
                    ensureBluetoothAccess()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Peach)
            ) {
                Text("Refresh Paired Printers")
            }

            if (pairedDevices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightBrown)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "No paired Bluetooth printer found.",
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Android Bluetooth settings me thermal printer pair karke yahan Refresh dabao.",
                            fontSize = 12.sp,
                            color = TeaBrown
                        )
                    }
                }
            } else {
                Text(
                    "Paired Printers",
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )

                pairedDevices.forEach { device ->
                    val deviceName = try {
                        device.name ?: "Unnamed Printer"
                    } catch (_: SecurityException) {
                        "Bluetooth Printer"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                printerName = deviceName
                            },
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (printerName == deviceName) LightPeach else White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text(
                                    deviceName,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepBrown
                                )

                                Text(
                                    try {
                                        device.address
                                    } catch (_: SecurityException) {
                                        "Bluetooth device"
                                    },
                                    fontSize = 11.sp,
                                    color = TeaBrown
                                )
                            }

                            if (printerName == deviceName) {
                                Text(
                                    "Selected",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Peach
                                )
                            }
                        }
                    }
                }
            }

            if (printerName.isNotBlank()) {
                Text(
                    "Selected Printer: $printerName",
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (printerName.isBlank()) {
                            statusMessage = "Pehle printer select karo."
                            return@Button
                        }

                        val device = pairedDevices.firstOrNull {
                            try {
                                it.name == printerName
                            } catch (_: SecurityException) {
                                false
                            }
                        }

                        if (device == null) {
                            statusMessage = "Selected printer paired list me nahi mila."
                            return@Button
                        }

                        isWorking = true

                        manager.connect(
                            device = device,
                            onSuccess = {
                                connected = true
                                statusMessage = "Connected to $printerName"
                                isWorking = false
                            },
                            onError = {
                                connected = false
                                statusMessage = it
                                isWorking = false
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isWorking && !connected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
                ) {
                    Text("Connect")
                }

                OutlinedButton(
                    onClick = {
                        manager.disconnect()
                        connected = false
                        statusMessage = "Printer disconnected"
                    },
                    modifier = Modifier.weight(1f),
                    enabled = connected
                ) {
                    Text("Disconnect")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (connected) LightGreen else LightRed
                )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (connected) "●" else "●",
                        color = if (connected) Green else Red,
                        fontSize = 20.sp
                    )

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(
                            if (connected) "Printer Connected"
                            else "Printer Disconnected",
                            fontWeight = FontWeight.Bold,
                            color = DeepBrown
                        )

                        Text(
                            statusMessage,
                            fontSize = 12.sp,
                            color = TeaBrown
                        )
                    }
                }
            }

            Text(
                "Paper Size",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DeepBrown
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("58mm", "80mm").forEach { p ->
                    FilterChip(
                        selected = paperSize == p,
                        onClick = { paperSize = p },
                        label = { Text(p) }
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Auto Print",
                    Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = autoPrint,
                    onCheckedChange = { autoPrint = it }
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Print Logo",
                    Modifier.weight(1f)
                )
                Checkbox(
                    checked = logo,
                    onCheckedChange = { logo = it }
                )
            }

            OutlinedTextField(
                value = header,
                onValueChange = { header = it },
                label = { Text("Bill Header") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = footer,
                onValueChange = { footer = it },
                label = { Text("Bill Footer") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    if (!connected) {
                        Toast.makeText(
                            context,
                            "Printer pehle connect karo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        isWorking = true
                        manager.testPrint(
                            paperSize = paperSize,
                            header = header,
                            footer = footer,
                            onSuccess = {
                                isWorking = false
                                Toast.makeText(
                                    context,
                                    "Test print successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = {
                                isWorking = false
                                connected = false
                                statusMessage = it
                                Toast.makeText(
                                    context,
                                    it,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isWorking
            ) {
                Text(if (isWorking) "Please Wait..." else "Test Print")
            }

            Button(
                onClick = {
                    onSave(
                        PrinterSettingsData(
                            printerName = printerName.trim(),
                            paperSize = paperSize,
                            connected = connected,
                            autoPrint = autoPrint,
                            billLogo = logo,
                            billHeader = header,
                            billFooter = footer
                        )
                    )

                    Toast.makeText(
                        context,
                        "Printer settings saved",
                        Toast.LENGTH_SHORT
                    ).show()

                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Peach
                )
            ) {
                Text("Save Printer Settings")
            }
        }
    }
}

// ============================================================
// PAYMENT SETTINGS
// ============================================================

@Composable
fun PaymentSettingsScreen(settings: PaymentSettingsData, onBack: () -> Unit, onSave: (PaymentSettingsData) -> Unit) {
    var cash by remember { mutableStateOf(settings.cashEnabled) }; var upi by remember { mutableStateOf(settings.upiEnabled) }; var card by remember { mutableStateOf(settings.cardEnabled) }; var other by remember { mutableStateOf(settings.otherEnabled) }; var upiId by remember { mutableStateOf(settings.upiId) }; var defaultMode by remember { mutableStateOf(settings.defaultMode) }
    val modes = listOf("Cash" to cash, "UPI" to upi, "Card" to card, "Other" to other)
    Scaffold(topBar={TopAppBar(title={Text("Payment Settings",fontWeight=FontWeight.Bold)},navigationIcon={TextButton(onClick=onBack){Text("Back",color=White)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Peach,titleContentColor=White))}){padding->
        Column(Modifier.fillMaxSize().background(Cream).padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Cash",Modifier.weight(1f));Switch(cash,{cash=it})}
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("UPI",Modifier.weight(1f));Switch(upi,{upi=it})}
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Card",Modifier.weight(1f));Switch(card,{card=it})}
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Other",Modifier.weight(1f));Switch(other,{other=it})}
            OutlinedTextField(upiId,{upiId=it},label={Text("UPI ID")},modifier=Modifier.fillMaxWidth(),singleLine=true)
            Text("Default Payment Mode",fontWeight=FontWeight.Bold,fontSize=13.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){modes.filter{it.second}.forEach{(m,_)->FilterChip(selected=defaultMode==m,onClick={defaultMode=m},label={Text(m)})}}
            Button(onClick={onSave(PaymentSettingsData(cash,upi,card,other,upiId.trim(),defaultMode));onBack()},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Peach)){Text("Save Payment Settings")}
        }
    }
}

// ============================================================
// BACKUP & RESTORE
// ============================================================

@Composable
fun BackupRestoreScreen(menuCount:Int, inventoryCount:Int, tableCount:Int, restaurantSettings:RestaurantSettingsData, paymentSettings:PaymentSettingsData, onBack:()->Unit){
    val context=LocalContext.current
    var showBackup by remember{mutableStateOf(false)}
    val backupText="""CHAI VEDA BACKUP\nRestaurant=${restaurantSettings.name}\nAddress=${restaurantSettings.address}\nMobile=${restaurantSettings.mobile}\nGSTIN=${restaurantSettings.gstin}\nGST Enabled=${restaurantSettings.gstEnabled}\nGST Percent=${restaurantSettings.gstPercent}\nMenu Items=$menuCount\nInventory Items=$inventoryCount\nTables=$tableCount\nCash=${paymentSettings.cashEnabled}\nUPI=${paymentSettings.upiEnabled}\nCard=${paymentSettings.cardEnabled}\nOther=${paymentSettings.otherEnabled}\nUPI ID=${paymentSettings.upiId}\nDefault Payment=${paymentSettings.defaultMode}"""
    Scaffold(topBar={TopAppBar(title={Text("Backup & Restore",fontWeight=FontWeight.Bold)},navigationIcon={TextButton(onClick=onBack){Text("Back",color=White)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Peach,titleContentColor=White))}){padding->
        Column(Modifier.fillMaxSize().background(Cream).padding(padding).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=White)){Column(Modifier.padding(16.dp)){Text("Current Data",fontWeight=FontWeight.Bold,fontSize=18.sp);Text("Menu: $menuCount items");Text("Inventory: $inventoryCount items");Text("Tables: $tableCount")}}
            Button(onClick={showBackup=true},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Peach)){Text("Create Backup")}
            OutlinedButton(onClick={Toast.makeText(context,"Restore is ready for the saved backup format; permanent file restore will be connected with database storage.",Toast.LENGTH_LONG).show()},modifier=Modifier.fillMaxWidth()){Text("Restore Backup")}
            Text("Note: This version keeps data in the current app session. Permanent backup/restore will be connected when Room database is enabled.",fontSize=12.sp,color=TeaBrown)
        }
    }
    if(showBackup) AlertDialog(onDismissRequest={showBackup=false},title={Text("Backup Data")},text={Text(backupText,modifier=Modifier.verticalScroll(rememberScrollState()),fontSize=11.sp)},confirmButton={TextButton(onClick={showBackup=false}){Text("Close")}})
}

private var nextInventoryId = 1000
fun generateInventoryId(): Int { nextInventoryId++; return nextInventoryId }
private var nextTableId = 1000
fun generateTableId(): Int { nextTableId++; return nextTableId }
fun formatStock(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US,"%.2f",value)

// ============================================================
// MENU MANAGEMENT
// ============================================================

@Composable
fun MenuManagementScreen(
    products: List<MenuProduct>,
    onBack: () -> Unit,
    onAddProduct: (MenuProduct) -> Unit,
    onUpdateProduct: (MenuProduct) -> Unit,
    onDeleteProduct: (MenuProduct) -> Unit
) {

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<MenuProduct?>(null) }
    var deletingProduct by remember { mutableStateOf<MenuProduct?>(null) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All") + products.map { it.category }.distinct().sorted()
    val filteredProducts = if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Peach, titleContentColor = White)
            )
        },
        floatingActionButton = {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Peach, contentColor = White)
            ) { Text("+ Add Item") }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Cream).padding(padding).padding(horizontal = 12.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    MenuManagementCard(
                        product = product,
                        onEdit = { editingProduct = product },
                        onToggle = { onUpdateProduct(product.copy(available = !product.available)) },
                        onDelete = { deletingProduct = product }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ProductEditorDialog(
            title = "Add Menu Item",
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = { product ->
                onAddProduct(product)
                showAddDialog = false
            }
        )
    }

    editingProduct?.let { product ->
        ProductEditorDialog(
            title = "Edit Menu Item",
            product = product,
            onDismiss = { editingProduct = null },
            onSave = { updated ->
                onUpdateProduct(updated)
                editingProduct = null
            }
        )
    }

    deletingProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { deletingProduct = null },
            title = { Text("Delete Item?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${product.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDeleteProduct(product); deletingProduct = null }) { Text("Delete", color = Red) }
            },
            dismissButton = { TextButton(onClick = { deletingProduct = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun MenuManagementCard(
    product: MenuProduct,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ProductImage(
                imageUri = product.imageUri,
                modifier = Modifier.size(68.dp),
                cornerRadius = 12.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
                Spacer(Modifier.height(3.dp))
                Text("${product.category} • ${product.foodType}", fontSize = 12.sp, color = TeaBrown)
                Spacer(Modifier.height(4.dp))
                Text("₹${product.price}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
                Spacer(Modifier.height(5.dp))
                Surface(
                    color = if (product.available) LightGreen else LightRed,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (product.available) "Available" else "Disabled",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (product.available) Green else Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEdit) { Text("Edit", color = Blue) }
                TextButton(onClick = onToggle) { Text(if (product.available) "Disable" else "Enable", color = if (product.available) Red else Green) }
                TextButton(onClick = onDelete) { Text("Delete", color = Red) }
            }
        }
    }
}

@Composable
fun ProductEditorDialog(
    title: String,
    product: MenuProduct?,
    onDismiss: () -> Unit,
    onSave: (MenuProduct) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Tea Collection") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var foodType by remember { mutableStateOf(product?.foodType ?: "Veg") }
    var available by remember { mutableStateOf(product?.available ?: true) }
    var imageUri by remember { mutableStateOf(product?.imageUri ?: "") }

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) { }
            imageUri = uri.toString()
        }
    }

    val categories = listOf("Tea Collection", "Hot Coffee Selection", "Iced Coffee & Shakes", "Juices", "Salad", "Premium Milkshakes", "Refreshing Mojitos", "Butter & Toasts", "Burgers", "Sandwiches & Breads", "Fries & Nachos", "Paratha", "All Time Fav.", "Wraps", "Vada Pav", "Maggie & Noodles", "Pasta", "Pizza", "Chinese Starters", "Fried Rice", "Soups", "Desserts")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProductImage(imageUri = imageUri, modifier = Modifier.size(110.dp), cornerRadius = 16.dp)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (imageUri.isBlank()) "Upload Item Image" else "Change Item Image")
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("Category", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(categories) { item ->
                        FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item, fontSize = 11.sp) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = price, onValueChange = { price = it.filter(Char::isDigit) }, label = { Text("Selling Price") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("Food Type", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FilterChip(selected = foodType == "Veg", onClick = { foodType = "Veg" }, label = { Text("Veg") })
                    FilterChip(selected = foodType == "Non-Veg", onClick = { foodType = "Non-Veg" }, label = { Text("Non-Veg") })
                }
                Spacer(Modifier.height(10.dp))
                FilterChip(selected = available, onClick = { available = !available }, label = { Text(if (available) "Item Available" else "Item Disabled") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cleanName = name.trim()
                val cleanPrice = price.toIntOrNull() ?: 0
                if (cleanName.isNotEmpty() && cleanPrice > 0) {
                    onSave(MenuProduct(product?.id ?: generateProductId(), cleanName, category, cleanPrice, foodType, available, imageUri))
                }
            }) { Text("Save", color = Green, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ProductImage(
    imageUri: String,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        if (imageUri.isBlank()) null else try {
            context.contentResolver.openInputStream(android.net.Uri.parse(imageUri))?.use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        } catch (_: Exception) { null }
    }

    Box(
        modifier = modifier
            .background(LightBrown, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Menu item image",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("☕", fontSize = 28.sp)
        }
    }
}

private var nextProductId = 100

fun generateProductId(): Int {
    nextProductId++
    return nextProductId
}

// ============================================================
// POS
// ============================================================

@Composable
fun POSScreen(
    products: List<MenuProduct>,
    tables: List<TableData>,
    onBack: () -> Unit,
    onBillGenerated: (BillData) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var orderType by remember { mutableStateOf("Dine-In") }
    var tableNumber by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showOrderSheet by remember { mutableStateOf(false) }
    val cart = remember { mutableStateListOf<CartItem>() }

    val categories = listOf("All") + products.map { it.category }.distinct().sorted()
    val filteredProducts = products.filter { product ->
        product.available &&
                (selectedCategory == "All" || product.category.equals(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isBlank() ||
                        product.name.contains(searchQuery.trim(), ignoreCase = true) ||
                        product.category.contains(searchQuery.trim(), ignoreCase = true))
    }
    val subtotal = cart.sumOf { it.product.price * it.quantity }
    val discount = 0
    val tax = 0
    val total = subtotal - discount + tax
    val availableTables = tables.filter { it.status.trim().equals("Available", ignoreCase = true) }

    fun addToCart(product: MenuProduct) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) cart[index] = cart[index].copy(quantity = cart[index].quantity + 1)
        else cart.add(CartItem(product, 1))
    }

    fun increase(item: CartItem) {
        val index = cart.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) cart[index] = cart[index].copy(quantity = cart[index].quantity + 1)
    }

    fun decrease(item: CartItem) {
        val index = cart.indexOfFirst { it.product.id == item.product.id }
        if (index >= 0) {
            if (cart[index].quantity > 1) cart[index] = cart[index].copy(quantity = cart[index].quantity - 1)
            else cart.removeAt(index)
        }
    }

    fun generateOrder() {
        if (cart.isEmpty()) return
        onBillGenerated(
            BillData(
                billNumber = generateBillNumber(),
                customerName = customerName.ifBlank { "Walk-in Customer" },
                mobileNumber = mobileNumber,
                orderType = orderType,
                tableNumber = tableNumber,
                items = cart.map { it.copy() },
                subtotal = subtotal,
                discount = discount,
                tax = tax,
                total = total
            )
        )
        showOrderSheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Order", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("‹", fontSize = 38.sp, color = White)
                    }
                },
                actions = {
                    Text("⋮", modifier = Modifier.padding(horizontal = 14.dp), fontSize = 28.sp, color = White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Peach, titleContentColor = White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(Cream).padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer Name", fontSize = 12.sp) },
                                placeholder = { Text("Walk-in Customer") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(13.dp)
                            )
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = { mobileNumber = it.filter(Char::isDigit).take(15) },
                                label = { Text("Mobile", fontSize = 12.sp) },
                                placeholder = { Text("Enter mobile no.") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(13.dp)
                            )
                        }

                        Spacer(Modifier.height(9.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OrderTypeButton(
                                text = "🍴  Dine-In",
                                selected = orderType == "Dine-In",
                                onClick = { orderType = "Dine-In" },
                                modifier = Modifier.weight(1f)
                            )
                            OrderTypeButton(
                                text = "▣  Takeaway",
                                selected = orderType == "Takeaway",
                                onClick = { orderType = "Takeaway"; tableNumber = "" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (orderType == "Dine-In") {
                            Spacer(Modifier.height(9.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("▱", fontSize = 20.sp, color = DeepBrown)
                                Spacer(Modifier.width(6.dp))
                                Text("Table", fontSize = 13.sp, color = DeepBrown, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(5.dp))
                            if (availableTables.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(end = 4.dp)) {
                                    items(availableTables, key = { it.id }) { table ->
                                        Surface(
                                            modifier = Modifier.clickable { tableNumber = table.name },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (tableNumber == table.name) Peach else White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (tableNumber == table.name) Peach else Color(0xFFD8D8D8))
                                        ) {
                                            Text(
                                                table.name,
                                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                                color = if (tableNumber == table.name) White else DeepBrown,
                                                fontWeight = if (tableNumber == table.name) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text("No available tables. Add/set tables from Settings → Tables.", fontSize = 11.sp, color = TeaBrown)
                            }
                        }

                        Spacer(Modifier.height(9.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Search menu items...", fontSize = 14.sp) },
                            leadingIcon = { Text("⌕", fontSize = 26.sp, color = TeaBrown) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    TextButton(onClick = { searchQuery = "" }) { Text("×", fontSize = 22.sp, color = TeaBrown) }
                                } else {
                                    Text("☷", modifier = Modifier.padding(end = 13.dp), fontSize = 22.sp, color = TeaBrown)
                                }
                            },
                            shape = RoundedCornerShape(28.dp)
                        )

                        Spacer(Modifier.height(7.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(end = 8.dp)) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No menu items found", fontWeight = FontWeight.Bold, color = DeepBrown)
                            Spacer(Modifier.height(4.dp))
                            Text("Try another item name or category.", color = TeaBrown, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 94.dp)
                    ) {
                        gridItems(filteredProducts, key = { it.id }) { product ->
                            ProductCard(product = product, onAdd = { addToCart(product) })
                        }
                    }
                }
            }

            if (cart.isNotEmpty()) {
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(containerColor = Peach),
                    elevation = CardDefaults.cardElevation(7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🛒", fontSize = 34.sp)
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${cart.sumOf { it.quantity }} Items", color = White, fontSize = 12.sp)
                            Text("₹$total", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showOrderSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Peach),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 17.dp, vertical = 11.dp)
                        ) { Text("View Order →", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    if (showOrderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = White
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(Modifier.width(42.dp).height(5.dp).background(Color.LightGray, RoundedCornerShape(5.dp)))
                }
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("Your Order", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBrown)
                            Spacer(Modifier.width(7.dp))
                            Text("(${cart.sumOf { it.quantity }} Items)", fontSize = 14.sp, color = TeaBrown)
                        }
                    }
                    TextButton(onClick = { cart.clear() }) {
                        Text("▣  Clear", color = Red, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 390.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(cart, key = { it.product.id }) { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProductImage(item.product.imageUri, Modifier.size(70.dp), 12.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                                Text(item.product.category, fontSize = 12.sp, color = TeaBrown)
                                Spacer(Modifier.height(2.dp))
                                Text("₹${item.product.price}", fontSize = 12.sp, color = TeaBrown)
                                Spacer(Modifier.height(5.dp))
                                IconQuantityControls(item = item, onIncrease = { increase(item) }, onDecrease = { decrease(item) })
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("₹${item.product.price * item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        HorizontalDivider()
                    }
                }
                SummaryRow("Subtotal", subtotal)
                SummaryRow("Discount", discount)
                SummaryRow("Tax (GST)", tax)
                Spacer(Modifier.height(5.dp))
                Surface(color = LightPeach, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = DeepBrown, modifier = Modifier.weight(1f))
                        Text("₹$total", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A1717))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { generateOrder() },
                    enabled = cart.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Peach, contentColor = White)
                ) { Text("▣  Generate Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun OrderTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFFF4ECFF) else White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Color(0xFF9B5DE5) else Color(0xFFD8D8D8))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text, color = if (selected) Color(0xFF5E2A99) else DeepBrown, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
            if (selected) {
                Spacer(Modifier.width(6.dp))
                Text("✓", color = Color(0xFF6B35C8), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun IconQuantityControls(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.background(LightBrown, RoundedCornerShape(18.dp)).padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        TextButton(onClick = onDecrease, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("−", fontSize = 18.sp, color = DeepBrown) }
        Text(item.quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
        TextButton(onClick = onIncrease, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("+", fontSize = 18.sp, color = DeepBrown) }
    }
}

@Composable
fun ProductCard(
    product: MenuProduct,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAdd() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(9.dp)) {
            ProductImage(product.imageUri, Modifier.fillMaxWidth().height(112.dp), 13.dp)
            Spacer(Modifier.height(7.dp))
            Text(product.name, fontWeight = FontWeight.Bold, color = DeepBrown, maxLines = 1)
            Text(product.category, fontSize = 11.sp, color = TeaBrown)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("₹${product.price}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepBrown, modifier = Modifier.weight(1f))
                Button(
                    onClick = onAdd,
                    modifier = Modifier.size(38.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Peach, contentColor = White)
                ) { Text("+") }
            }
        }
    }
}

// ============================================================
// CART ITEM
// ============================================================

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = "₹${item.product.price}",
                    fontSize = 11.sp,
                    color = TeaBrown
                )
            }

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                SmallQuantityButton(
                    text = "−",
                    onClick = onDecrease
                )

                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.padding(
                        horizontal = 8.dp
                    ),
                    fontWeight = FontWeight.Bold
                )

                SmallQuantityButton(
                    text = "+",
                    onClick = onIncrease
                )
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "₹${item.product.price * item.quantity}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// QUANTITY BUTTON
// ============================================================

@Composable
fun SmallQuantityButton(
    text: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .size(28.dp)
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(7.dp),

        color = LightPeach
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = TeaBrown
            )
        }
    }
}

// ============================================================
// SUMMARY
// ============================================================

@Composable
fun SummaryRow(
    label: String,
    amount: Int,
    bold: Boolean = false
) {

    Row(
        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,

            fontWeight =
                if (bold)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )

        Text(
            text = "₹$amount",

            fontWeight =
                if (bold)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )
    }
}

// ============================================================
// BILL GENERATED
// ============================================================

@Composable
fun BillGeneratedScreen(
    bill: BillData,
    onBackDashboard: () -> Unit,
    onSettlePayment: () -> Unit,
    onEditBill: () -> Unit,
    onPrint: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Bill Generated",
                        fontWeight = FontWeight.Bold
                    )
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(18.dp),

                colors = CardDefaults.cardColors(
                    containerColor = White
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Text(
                        text = "CHAI VEDA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown
                    )

                    Text(
                        text = "Café • Juice • & More",
                        color = TeaBrown
                    )

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    BillInfoRow(
                        "Bill No.",
                        bill.billNumber
                    )

                    BillInfoRow(
                        "Date",
                        bill.dateTime
                    )

                    BillInfoRow(
                        "Customer",
                        if (bill.customerName.isBlank())
                            "Walk-in Customer"
                        else
                            bill.customerName
                    )

                    if (bill.mobileNumber.isNotBlank()) {

                        BillInfoRow(
                            "Mobile",
                            bill.mobileNumber
                        )
                    }

                    BillInfoRow(
                        "Order",
                        bill.orderType
                    )

                    if (
                        bill.orderType == "Dine-In" &&
                        bill.tableNumber.isNotBlank()
                    ) {

                        BillInfoRow(
                            "Table",
                            bill.tableNumber
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    bill.items.forEach { item ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                text =
                                    "${item.product.name} × ${item.quantity}"
                            )

                            Text(
                                text =
                                    "₹${item.product.price * item.quantity}"
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    SummaryRow(
                        label = "Subtotal",
                        amount = bill.subtotal
                    )

                    SummaryRow(
                        label = "Discount",
                        amount = bill.discount
                    )

                    SummaryRow(
                        label = "Tax",
                        amount = bill.tax
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    SummaryRow(
                        label = "TOTAL",
                        amount = bill.total,
                        bold = true
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    PaymentStatusBadge(
                        status = bill.paymentStatus
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            if (bill.paymentStatus != "Paid") {

                OutlinedButton(
                    onClick = onEditBill,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Bill")
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            Button(
                onClick = onPrint,
                modifier = Modifier.fillMaxWidth(),

                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBrown
                )
            ) {

                Text("Print Bill")
            }

            if (bill.paymentStatus != "Paid") {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = onSettlePayment,
                    modifier = Modifier.fillMaxWidth(),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
                ) {

                    Text("Settle Payment")
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedButton(
                onClick = onBackDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Back to Dashboard")
            }
        }
    }
}

// ============================================================
// EDIT BILL
// ============================================================

@Composable
fun EditBillScreen(
    bill: BillData,
    products: List<MenuProduct>,
    tables: List<TableData>,
    onBack: () -> Unit,
    onSave: (BillData) -> Unit
) {

    var customerName by remember { mutableStateOf(bill.customerName) }
    var mobileNumber by remember { mutableStateOf(bill.mobileNumber) }
    var orderType by remember { mutableStateOf(bill.orderType) }
    var tableNumber by remember { mutableStateOf(bill.tableNumber) }
    var cart by remember { mutableStateOf(bill.items.map { it.copy() }) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All") + products.map { it.category }.distinct().sorted()
    val filteredProducts = products.filter {
        it.available && (selectedCategory == "All" || it.category == selectedCategory)
    }

    val subtotal = cart.sumOf { it.product.price * it.quantity }
    val discount = bill.discount
    val tax = bill.tax
    val total = subtotal - discount + tax

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Bill", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(14.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text("Bill No.: ${bill.billNumber}", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Mobile") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = orderType == "Dine-In",
                    onClick = { orderType = "Dine-In" },
                    label = { Text("Dine-In") }
                )
                FilterChip(
                    selected = orderType == "Takeaway",
                    onClick = {
                        orderType = "Takeaway"
                        tableNumber = ""
                    },
                    label = { Text("Takeaway") }
                )
            }

            if (orderType == "Dine-In" && tables.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Table", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tables) { table ->
                        FilterChip(
                            selected = tableNumber == table.name,
                            onClick = { tableNumber = table.name },
                            label = { Text(table.name) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Current Items", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            if (cart.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("No items in bill", color = TeaBrown)
            }

            cart.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.name, fontWeight = FontWeight.Bold)
                            Text("₹${item.product.price} × ${item.quantity}", color = TeaBrown)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                val copy = cart.toMutableList()
                                val current = copy[index]
                                if (current.quantity > 1) {
                                    copy[index] = current.copy(quantity = current.quantity - 1)
                                } else {
                                    copy.removeAt(index)
                                }
                                cart = copy
                            }) { Text("−") }

                            Text("${item.quantity}", fontWeight = FontWeight.Bold)

                            TextButton(onClick = {
                                val copy = cart.toMutableList()
                                val current = copy[index]
                                copy[index] = current.copy(quantity = current.quantity + 1)
                                cart = copy
                            }) { Text("+") }

                            TextButton(onClick = {
                                val copy = cart.toMutableList()
                                copy.removeAt(index)
                                cart = copy
                            }) {
                                Text("Remove", color = Red, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Add Item", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            filteredProducts.forEach { product ->
                OutlinedButton(
                    onClick = {
                        val copy = cart.toMutableList()
                        val existingIndex = copy.indexOfFirst { it.product.id == product.id }
                        if (existingIndex >= 0) {
                            val existing = copy[existingIndex]
                            copy[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                        } else {
                            copy.add(CartItem(product, 1))
                        }
                        cart = copy
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(product.name)
                        Text("₹${product.price}")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            SummaryRow("Subtotal", subtotal)
            SummaryRow("Discount", discount)
            SummaryRow("Tax", tax)
            SummaryRow("TOTAL", total, bold = true)

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    onSave(
                        bill.copy(
                            customerName = customerName.trim(),
                            mobileNumber = mobileNumber.trim(),
                            orderType = orderType,
                            tableNumber = tableNumber,
                            items = cart,
                            subtotal = subtotal,
                            discount = discount,
                            tax = tax,
                            total = total
                        )
                    )
                },
                enabled = cart.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("Save Changes")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

// ============================================================
// BILL INFO
// ============================================================

@Composable
fun BillInfoRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {

        Text(
            text = label,
            modifier = Modifier.width(90.dp),
            color = TeaBrown,
            fontSize = 13.sp
        )

        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

// ============================================================
// PAYMENT STATUS
// ============================================================

@Composable
fun PaymentStatusBadge(
    status: String
) {

    val background =
        when (status) {
            "Paid" -> LightGreen
            "Partially Paid" -> LightBlue
            else -> LightRed
        }

    val textColor =
        when (status) {
            "Paid" -> Green
            "Partially Paid" -> Blue
            else -> Red
        }

    Surface(
        color = background,
        shape = RoundedCornerShape(10.dp)
    ) {

        Text(
            text = "Payment: $status",

            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),

            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 13.sp
        )
    }
}

// ============================================================
// SETTLEMENT
// ============================================================

@Composable
fun SettlementScreen(
    bills: List<BillData>,
    onBack: () -> Unit,
    onPaymentDone: (
        BillData,
        Int,
        String
    ) -> Unit
) {

    var selectedBill by remember {
        mutableStateOf<BillData?>(null)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Payment Settlement",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    TextButton(
                        onClick = onBack
                    ) {

                        Text(
                            text = "Back",
                            color = White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }

    ) { padding ->

        if (
            bills.none {
                it.paymentStatus != "Paid"
            }
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(padding),

                contentAlignment =
                    Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "No Pending Payments",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "All bills are settled.",
                        color = TeaBrown
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Cream)
                    .padding(padding)
                    .padding(14.dp),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    items = bills.filter {
                        it.paymentStatus != "Paid"
                    },

                    key = {
                        it.billNumber
                    }
                ) { bill ->

                    SettlementBillCard(
                        bill = bill,
                        onClick = {
                            selectedBill = bill
                        }
                    )
                }
            }
        }
    }

    selectedBill?.let { bill ->

        SettlementDialog(
            bill = bill,

            onDismiss = {
                selectedBill = null
            },

            onConfirm = { amount, mode ->

                onPaymentDone(
                    bill,
                    amount,
                    mode
                )

                selectedBill = null
            }
        )
    }
}

// ============================================================
// SETTLEMENT CARD
// ============================================================

@Composable
fun SettlementBillCard(
    bill: BillData,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = bill.billNumber,
                    fontWeight = FontWeight.Bold,
                    color = DeepBrown
                )

                PaymentStatusBadge(
                    status = bill.paymentStatus
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    if (bill.customerName.isBlank())
                        "Walk-in Customer"
                    else
                        bill.customerName
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Total: ₹${bill.total}",
                    color = TeaBrown
                )

                Text(
                    text = "Due: ₹${bill.dueAmount}",
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
            }
        }
    }
}

// ============================================================
// SETTLEMENT DIALOG
// ============================================================

@Composable
fun SettlementDialog(
    bill: BillData,
    onDismiss: () -> Unit,
    onConfirm: (
        Int,
        String
    ) -> Unit
) {

    var amount by remember {
        mutableStateOf(
            bill.dueAmount.toString()
        )
    }

    var paymentMode by remember {
        mutableStateOf("Cash")
    }

    val modes = listOf(
        "Cash",
        "UPI",
        "Card",
        "Other"
    )

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Settle ${bill.billNumber}",
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column {

                Text(
                    text = "Due Amount: ₹${bill.dueAmount}",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                OutlinedTextField(
                    value = amount,

                    onValueChange = {
                        amount =
                            it.filter { char ->
                                char.isDigit()
                            }
                    },

                    label = {
                        Text("Payment Amount")
                    },

                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Payment Mode",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {

                    modes.forEach { mode ->

                        FilterChip(
                            selected =
                                paymentMode == mode,

                            onClick = {
                                paymentMode = mode
                            },

                            label = {
                                Text(
                                    text = mode,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val entered =
                        amount.toIntOrNull() ?: 0

                    if (
                        entered > 0 &&
                        entered <= bill.dueAmount
                    ) {

                        onConfirm(
                            entered,
                            paymentMode
                        )
                    }
                }
            ) {

                Text(
                    text = "Confirm Payment",
                    color = Green
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}

// ============================================================
// PLACEHOLDER
// ============================================================

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    TextButton(
                        onClick = onBack
                    ) {

                        Text(
                            text = "Back",
                            color = White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Peach,
                    titleContentColor = White
                )
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
                .padding(20.dp),

            contentAlignment =
                Alignment.Center
        ) {

            Card(
                shape = RoundedCornerShape(18.dp),

                colors = CardDefaults.cardColors(
                    containerColor = White
                )
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBrown
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = subtitle,
                        textAlign = TextAlign.Center,
                        color = TeaBrown
                    )
                }
            }
        }
    }
}

// ============================================================
// BILL NUMBER
// ============================================================

fun generateBillNumber(): String {
    // Compatibility fallback. The POS assigns the real monthly sequence from Room.
    return "CV-0001"
}

// ============================================================
// DATE TIME
// ============================================================

fun currentDateTime(): String {

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date())
}

// ============================================================
// BACK HANDLER
// ============================================================

@Composable
private fun BackHandlerForScreen(
    currentScreen: AppScreen,
    onDashboard: () -> Unit,
    onExit: () -> Unit
) {

    BackHandler(

        enabled = currentScreen != AppScreen.WELCOME,

        onBack = {

            when (currentScreen) {

                AppScreen.DASHBOARD -> {
                    onExit()
                }

                AppScreen.POS,
                AppScreen.SETTLEMENT,
                AppScreen.EDIT_BILL,
                AppScreen.REPORTS,
                AppScreen.SETTINGS,
                AppScreen.MENU_MANAGEMENT,
                AppScreen.BILL_GENERATED -> {
                    onDashboard()
                }

                else -> {
                    onDashboard()
                }
            }
        }
    )
}


