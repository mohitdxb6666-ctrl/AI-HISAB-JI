package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ui.AppTab
import com.example.ui.HisaabViewModel
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NewCustomerDialog
import com.example.ui.components.PaperScanDialog
import com.example.ui.components.QrCodeDialog
import com.example.ui.components.SecurityPinDialog
import com.example.ui.components.VoiceInputDialog
import com.example.ui.screens.AiMunimScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HisaabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val uiState by viewModel.uiState.collectAsState()
                val customers by viewModel.customers.collectAsState()
                val transactions by viewModel.transactions.collectAsState()
                val profile by viewModel.shopProfile.collectAsState()
                val chatMessages by viewModel.chatMessages.collectAsState()
                val speechState by viewModel.speechState.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                var showSetPinDialog by remember { mutableStateOf(false) }

                // Audio permission launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.openVoiceDialog()
                    } else {
                        Toast.makeText(context, "आवाज़ से हिसाब के लिए माइक्रोफोन की अनुमति दें", Toast.LENGTH_SHORT).show()
                    }
                }

                // Camera permission launcher for Usha ji & Kamal ji khata photo OCR
                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.openCameraScreen()
                    } else {
                        Toast.makeText(context, "खाता स्कैन करने के लिए कैमरा की अनुमति दें", Toast.LENGTH_SHORT).show()
                    }
                }

                fun launchCameraWithPermission() {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.openCameraScreen()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                // Toast notification trigger
                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                            viewModel.clearToast()
                        }
                    }
                }

                LaunchedEffect(profile.isPinEnabled) {
                    if (profile.isPinEnabled && profile.pin.isNotBlank()) {
                        viewModel.checkPinProtection()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (uiState.selectedCustomerId == null && !uiState.isAppLocked) {
                            BottomNavBar(
                                selectedTab = uiState.currentTab,
                                onSelectTab = { viewModel.selectTab(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MidnightNavy
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MidnightNavy)
                    ) {
                        val selectedCustomer = customers.find { it.id == uiState.selectedCustomerId }

                        if (selectedCustomer != null) {
                            val custTxns = transactions.filter { it.customerId == selectedCustomer.id }
                            val custBal = viewModel.getCustomerBalance(selectedCustomer.id)

                            CustomerDetailScreen(
                                customer = selectedCustomer,
                                balance = custBal,
                                transactions = custTxns,
                                onBack = { viewModel.closeCustomerDetail() },
                                onAddCredit = { viewModel.openAddTransaction(selectedCustomer.id, 1) },
                                onAddDebit = { viewModel.openAddTransaction(selectedCustomer.id, 2) },
                                onShowQr = { viewModel.openQrDialog(selectedCustomer.id) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                onDeleteCustomer = { viewModel.deleteCustomer(it) }
                            )
                        } else {
                            when (uiState.currentTab) {
                                AppTab.HOME -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        customers = customers,
                                        customerBalances = { viewModel.getCustomerBalance(it) },
                                        totalPending = viewModel.calculateTotalPending(),
                                        todayCredit = viewModel.calculateTodayCredit(),
                                        todayDebit = viewModel.calculateTodayDebit(),
                                        onCustomerClick = { viewModel.openCustomerDetail(it) },
                                        onVoiceClick = {
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                viewModel.openVoiceDialog()
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        },
                                        onScanClick = { viewModel.openScanner() },
                                        onNewCustomerClick = { viewModel.openNewCustomerDialog() },
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onViewAllCustomers = { viewModel.selectTab(AppTab.CUSTOMERS) }
                                    )
                                }
                                AppTab.CUSTOMERS -> {
                                    CustomersScreen(
                                        customers = customers,
                                        customerBalances = { viewModel.getCustomerBalance(it) },
                                        onCustomerClick = { viewModel.openCustomerDetail(it) },
                                        onAddCustomerClick = { viewModel.openNewCustomerDialog() }
                                    )
                                }
                                AppTab.AI_MUNIM -> {
                                    AiMunimScreen(
                                        messages = chatMessages,
                                        isThinking = uiState.isAiThinking,
                                        onSendMessage = { viewModel.sendAiMessage(it) },
                                        onSpeakMessage = { viewModel.soundboxManager.speak(it) },
                                        onOpenVoiceDialog = { viewModel.openVoiceDialog() }
                                    )
                                }
                                AppTab.ANALYTICS -> {
                                    AnalyticsScreen(
                                        customers = customers,
                                        transactions = transactions,
                                        customerBalances = { viewModel.getCustomerBalance(it) }
                                    )
                                }
                                AppTab.SETTINGS -> {
                                    SettingsScreen(
                                        profile = profile,
                                        onSaveProfile = { sName, oName, upi, sbox, autoB ->
                                            viewModel.updateShopSettings(sName, oName, upi, sbox, autoB)
                                        },
                                        onOpenSetPinDialog = { showSetPinDialog = true },
                                        onTogglePin = { enabled ->
                                            viewModel.updateSecurityPin(profile.pin, enabled)
                                        },
                                        onResetData = { viewModel.resetDemoData() }
                                    )
                                }
                            }
                        }

                        // App Lock Dialog
                        if (uiState.isAppLocked) {
                            SecurityPinDialog(
                                isSettingPin = false,
                                onPinEntered = { viewModel.unlockAppWithPin(it) },
                                onDismiss = {}
                            )
                        }

                        // Set New PIN Dialog
                        if (showSetPinDialog) {
                            SecurityPinDialog(
                                isSettingPin = true,
                                onPinEntered = { newPin ->
                                    viewModel.updateSecurityPin(newPin, true)
                                    showSetPinDialog = false
                                },
                                onDismiss = { showSetPinDialog = false }
                            )
                        }

                        // Voice Dialog with Android SpeechRecognizer STT & Hands-Free
                        if (uiState.showVoiceDialog) {
                            VoiceInputDialog(
                                speechState = speechState,
                                onStartListening = { viewModel.speechManager.startListening() },
                                onStopListening = { viewModel.speechManager.stopListening() },
                                onToggleHandsFree = { viewModel.speechManager.toggleHandsFreeMode(it) },
                                onDismiss = {
                                    viewModel.speechManager.stopListening()
                                    viewModel.closeVoiceDialog()
                                },
                                onVoiceInputReceived = { viewModel.processVoiceInput(it) },
                                parsedAction = uiState.voiceParsedAction,
                                onConfirmTransaction = { name, amt, type, note ->
                                    viewModel.confirmVoiceTransaction(name, amt, type, note)
                                }
                            )
                        }

                        // Paper Scanner Dialog
                        if (uiState.showScannerDialog) {
                            PaperScanDialog(
                                onDismiss = { viewModel.closeScanner() },
                                isScanning = uiState.isScanningImage,
                                scannedEntries = uiState.scannedEntries,
                                scannedImagePreview = uiState.scannedImagePreview,
                                onImageSelected = { viewModel.scanLedgerImage(it) },
                                onOpenLiveCamera = { launchCameraWithPermission() },
                                onToggleEntry = { viewModel.toggleScannedEntrySelection(it) },
                                onConfirmSave = { viewModel.saveScannedEntriesToLedger() }
                            )
                        }

                        // Fullscreen Live CameraX Viewfinder for Handwritten Khata Capture
                        if (uiState.showCameraScreen) {
                            CameraCaptureScreen(
                                onPhotoCaptured = { capturedBitmap ->
                                    viewModel.onCameraPhotoCaptured(capturedBitmap)
                                },
                                onClose = { viewModel.closeCameraScreen() }
                            )
                        }

                        // Add Transaction Dialog
                        if (uiState.showAddTransactionSheet && uiState.targetCustomerId != null) {
                            val cust = customers.find { it.id == uiState.targetCustomerId }
                            val cName = cust?.name ?: "ग्राहक"
                            AddTransactionSheet(
                                initialType = uiState.addTransactionType,
                                customerName = cName,
                                onDismiss = { viewModel.closeAddTransaction() },
                                onSave = { amt, type, note ->
                                    viewModel.recordTransaction(uiState.targetCustomerId!!, amt, type, note)
                                }
                            )
                        }

                        // New Customer Dialog
                        if (uiState.showNewCustomerDialog) {
                            NewCustomerDialog(
                                onDismiss = { viewModel.closeNewCustomerDialog() },
                                onAdd = { name, hindiName, phone ->
                                    viewModel.addCustomer(name, hindiName, phone)
                                }
                            )
                        }

                        // QR Code Dialog
                        if (uiState.showQrDialog && uiState.qrTargetCustomerId != null) {
                            val cust = customers.find { it.id == uiState.qrTargetCustomerId }
                            val cName = cust?.name ?: "ग्राहक"
                            val amt = viewModel.getCustomerBalance(uiState.qrTargetCustomerId!!)
                            QrCodeDialog(
                                customerName = cName,
                                amount = if (amt > 0) amt else 500.0,
                                upiId = profile.upiId,
                                onDismiss = { viewModel.closeQrDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
