package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiService
import com.example.ai.ParsedVoiceAction
import com.example.ai.ScannedLedgerEntry
import com.example.ai.SoundboxManager
import com.example.ai.SpeechRecognizerState
import com.example.ai.SpeechToTextManager
import com.example.ai.VoiceCommandType
import com.example.ai.VoiceParser
import com.example.data.database.AppDatabase
import com.example.data.models.Customer
import com.example.data.models.ShopProfile
import com.example.data.models.Transaction
import com.example.data.repository.HisaabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME,
    CUSTOMERS,
    ANALYTICS,
    AI_MUNIM,
    SETTINGS
}

data class HisaabUiState(
    val currentTab: AppTab = AppTab.HOME,
    val selectedCustomerId: Long? = null,
    val searchQuery: String = "",
    val showVoiceDialog: Boolean = false,
    val voiceParsedAction: ParsedVoiceAction? = null,
    val showScannerDialog: Boolean = false,
    val showCameraScreen: Boolean = false,
    val isScanningImage: Boolean = false,
    val scannedEntries: List<ScannedLedgerEntry> = emptyList(),
    val scannedImagePreview: Bitmap? = null,
    val showAddTransactionSheet: Boolean = false,
    val addTransactionType: Int = 1, // 1 = Udhaar/Credit, 2 = Vasooli/Debit
    val targetCustomerId: Long? = null,
    val showNewCustomerDialog: Boolean = false,
    val showQrDialog: Boolean = false,
    val qrTargetCustomerId: Long? = null,
    val showStatementDialog: Boolean = false,
    val showPinDialog: Boolean = false,
    val isAppLocked: Boolean = false,
    val toastMessage: String? = null,
    val isAiThinking: Boolean = false
)

class HisaabViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = HisaabRepository(
        database.customerDao(),
        database.transactionDao(),
        database.shopProfileDao()
    )

    private val geminiService = GeminiService()
    val soundboxManager = SoundboxManager(application)

    val speechManager = SpeechToTextManager(
        context = application,
        onFinalResult = { recognizedText ->
            processVoiceInput(recognizedText)
        }
    )
    val speechState: StateFlow<SpeechRecognizerState> = speechManager.state

    private val _uiState = MutableStateFlow(HisaabUiState())
    val uiState: StateFlow<HisaabUiState> = _uiState.asStateFlow()

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shopProfile: StateFlow<ShopProfile> = repository.shopProfile
        .combine(MutableStateFlow(ShopProfile())) { profile, defaultProf ->
            profile ?: defaultProf
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopProfile())

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "नमस्ते मालिक! 🙏 मैं आपका AI मुनीम (Hisaab Ji) हूँ।\nआप मुझसे खातों का हिसाब पूछ सकते हैं, या सीधे बोलकर एंट्री दर्ज कर सकते हैं।",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // --- Tab & Navigation ---
    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab, selectedCustomerId = null)
    }

    fun openCustomerDetail(customerId: Long) {
        _uiState.value = _uiState.value.copy(selectedCustomerId = customerId)
    }

    fun closeCustomerDetail() {
        _uiState.value = _uiState.value.copy(selectedCustomerId = null)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    // --- Customer Operations ---
    fun addCustomer(name: String, hindiName: String = "", phone: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val cleanName = if (name.endsWith("Ji") || name.endsWith("जी")) name else "$name Ji"
            val customer = Customer(name = cleanName, hindiName = hindiName, phone = phone)
            val newId = repository.insertCustomer(customer)
            _uiState.value = _uiState.value.copy(showNewCustomerDialog = false)
            showToast("✅ ग्राहक $cleanName जुड़ गया")
            soundboxManager.speak("नया ग्राहक $cleanName जोड़ दिया गया है")
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            closeCustomerDetail()
            showToast("ग्राहक हटा दिया गया")
        }
    }

    // --- Transaction Operations ---
    fun openAddTransaction(customerId: Long, type: Int) {
        _uiState.value = _uiState.value.copy(
            showAddTransactionSheet = true,
            addTransactionType = type,
            targetCustomerId = customerId
        )
    }

    fun closeAddTransaction() {
        _uiState.value = _uiState.value.copy(
            showAddTransactionSheet = false,
            targetCustomerId = null
        )
    }

    fun recordTransaction(customerId: Long, amount: Double, type: Int, note: String, source: Int = 0) {
        if (amount <= 0) return
        viewModelScope.launch {
            val txn = Transaction(
                customerId = customerId,
                amount = amount,
                type = type,
                note = note,
                source = source
            )
            repository.insertTransaction(txn)
            closeAddTransaction()

            val customer = customers.value.find { it.id == customerId }
            val cName = customer?.name ?: "ग्राहक"
            val actionText = if (type == 1) "उधार जोड़ा" else "पैसे मिले"
            showToast("✅ ₹${amount.toInt()} $actionText ($cName)")

            if (shopProfile.value.soundboxEnabled) {
                soundboxManager.playSoundboxAlert(amount, cName, type == 1)
            }
        }
    }

    fun deleteTransaction(txn: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(txn)
            showToast("हिसाब हटा दिया गया")
        }
    }

    // --- Voice OS Engine ---
    fun openVoiceDialog() {
        _uiState.value = _uiState.value.copy(showVoiceDialog = true, voiceParsedAction = null)
    }

    fun closeVoiceDialog() {
        _uiState.value = _uiState.value.copy(showVoiceDialog = false, voiceParsedAction = null)
    }

    fun processVoiceInput(spokenText: String) {
        if (spokenText.isBlank()) return
        val parsed = VoiceParser.parse(spokenText)
        _uiState.value = _uiState.value.copy(voiceParsedAction = parsed)

        when (parsed.commandType) {
            VoiceCommandType.SCAN_PAPER -> {
                closeVoiceDialog()
                openScanner()
            }
            VoiceCommandType.SHOW_QR -> {
                closeVoiceDialog()
                val targetId = _uiState.value.selectedCustomerId ?: customers.value.firstOrNull()?.id
                if (targetId != null) openQrDialog(targetId)
            }
            VoiceCommandType.OPEN_CUSTOMER -> {
                val target = customers.value.find {
                    it.name.contains(parsed.customerName ?: "", ignoreCase = true) ||
                            it.hindiName.contains(parsed.customerName ?: "", ignoreCase = true)
                }
                if (target != null) {
                    closeVoiceDialog()
                    openCustomerDetail(target.id)
                }
            }
            VoiceCommandType.WHATSAPP_REMIND -> {
                closeVoiceDialog()
                // will trigger whatsapp for current customer
            }
            VoiceCommandType.NEW_CUSTOMER -> {
                if (!parsed.customerName.isNullOrBlank()) {
                    addCustomer(parsed.customerName, "", parsed.phoneNumber)
                    closeVoiceDialog()
                }
            }
            VoiceCommandType.TRANSACTION -> {
                // If hands-free mode is enabled for Usha ji & Kamal ji, auto-commit directly!
                if (speechManager.state.value.isHandsFreeEnabled) {
                    confirmVoiceTransaction(
                        parsed.customerName ?: "ग्राहक Ji",
                        parsed.amount,
                        parsed.transactionType,
                        parsed.note
                    )
                    showToast("🎙️ हैंड्स-फ्री: ${parsed.customerName ?: "ग्राहक"} का ₹${parsed.amount.toInt()} हिसाब दर्ज हुआ ✅")
                }
                // Otherwise keep dialog open showing parsed card for 1-tap confirmation
            }
            VoiceCommandType.UNKNOWN_OR_QUERY -> {
                // If it's a general question, route to AI Munim
                selectTab(AppTab.AI_MUNIM)
                closeVoiceDialog()
                sendAiMessage(spokenText)
            }
        }
    }

    fun confirmVoiceTransaction(name: String, amount: Double, type: Int, note: String) {
        viewModelScope.launch {
            var target = customers.value.find {
                it.name.contains(name, ignoreCase = true) ||
                        it.hindiName.contains(name, ignoreCase = true)
            }
            if (target == null) {
                val cleanName = if (name.endsWith("Ji") || name.endsWith("जी")) name else "$name Ji"
                val newId = repository.insertCustomer(Customer(name = cleanName))
                target = Customer(id = newId, name = cleanName)
            }
            recordTransaction(target.id, amount, type, note, source = 1)
            closeVoiceDialog()
        }
    }

    // --- Messy Paper Scanner (Gemini Vision) ---
    fun openCameraScreen() {
        _uiState.value = _uiState.value.copy(
            showCameraScreen = true,
            showScannerDialog = false
        )
    }

    fun closeCameraScreen() {
        _uiState.value = _uiState.value.copy(
            showCameraScreen = false
        )
    }

    fun onCameraPhotoCaptured(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            showCameraScreen = false,
            showScannerDialog = true
        )
        scanLedgerImage(bitmap)
    }

    fun openScanner() {
        _uiState.value = _uiState.value.copy(
            showScannerDialog = true,
            isScanningImage = false,
            scannedEntries = emptyList(),
            scannedImagePreview = null
        )
    }

    fun closeScanner() {
        _uiState.value = _uiState.value.copy(
            showScannerDialog = false,
            isScanningImage = false,
            scannedEntries = emptyList()
        )
    }

    fun scanLedgerImage(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            isScanningImage = true,
            scannedImagePreview = bitmap
        )
        viewModelScope.launch {
            val entries = geminiService.analyzeHandwrittenLedger(bitmap)
            _uiState.value = _uiState.value.copy(
                isScanningImage = false,
                scannedEntries = entries
            )
            soundboxManager.speak("कागज़ का हिसाब पढ़ लिया गया है। ${entries.size} एंट्रियां मिली हैं।")
        }
    }

    fun toggleScannedEntrySelection(index: Int) {
        val current = _uiState.value.scannedEntries.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(isSelected = !current[index].isSelected)
            _uiState.value = _uiState.value.copy(scannedEntries = current)
        }
    }

    fun saveScannedEntriesToLedger() {
        viewModelScope.launch {
            val selected = _uiState.value.scannedEntries.filter { it.isSelected }
            for (entry in selected) {
                var customer = customers.value.find {
                    it.name.contains(entry.customerName, ignoreCase = true) ||
                            it.hindiName.contains(entry.customerName, ignoreCase = true)
                }
                if (customer == null) {
                    val clean = if (entry.customerName.endsWith("Ji") || entry.customerName.endsWith("जी"))
                        entry.customerName else "${entry.customerName} Ji"
                    val newId = repository.insertCustomer(Customer(name = clean))
                    customer = Customer(id = newId, name = clean)
                }
                repository.insertTransaction(
                    Transaction(
                        customerId = customer.id,
                        amount = entry.amount,
                        type = entry.type,
                        note = entry.note,
                        source = 2
                    )
                )
            }
            closeScanner()
            showToast("✅ ${selected.size} एंट्रियां बही-खाते में सेव हो गईं")
            soundboxManager.speak("सभी स्कैन किए गए हिसाब खाते में जोड़ दिए गए हैं")
        }
    }

    // --- AI Munim Chat ---
    fun sendAiMessage(query: String) {
        if (query.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(text = query, isUser = true))
        _chatMessages.value = currentList
        _uiState.value = _uiState.value.copy(isAiThinking = true)

        viewModelScope.launch {
            val totalPending = calculateTotalPending()
            val totalCust = customers.value.size
            val shopContext = "Shop: ${shopProfile.value.shopName}, Owner: ${shopProfile.value.ownerName}, Total Market Due: ₹$totalPending across $totalCust customers."
            val answer = geminiService.askAiMunim(query, shopContext, currentList)

            val updated = _chatMessages.value.toMutableList()
            updated.add(ChatMessage(text = answer, isUser = false))
            _chatMessages.value = updated
            _uiState.value = _uiState.value.copy(isAiThinking = false)

            if (shopProfile.value.soundboxEnabled) {
                soundboxManager.speak(answer.take(120))
            }
        }
    }

    // --- Dialogs & UI Toggles ---
    fun openNewCustomerDialog() {
        _uiState.value = _uiState.value.copy(showNewCustomerDialog = true)
    }

    fun closeNewCustomerDialog() {
        _uiState.value = _uiState.value.copy(showNewCustomerDialog = false)
    }

    fun openQrDialog(customerId: Long) {
        _uiState.value = _uiState.value.copy(showQrDialog = true, qrTargetCustomerId = customerId)
    }

    fun closeQrDialog() {
        _uiState.value = _uiState.value.copy(showQrDialog = false, qrTargetCustomerId = null)
    }

    fun openStatementDialog() {
        _uiState.value = _uiState.value.copy(showStatementDialog = true)
    }

    fun closeStatementDialog() {
        _uiState.value = _uiState.value.copy(showStatementDialog = false)
    }

    // --- Security PIN ---
    fun checkPinProtection() {
        if (shopProfile.value.isPinEnabled && shopProfile.value.pin.isNotBlank()) {
            _uiState.value = _uiState.value.copy(isAppLocked = true)
        }
    }

    fun unlockAppWithPin(enteredPin: String): Boolean {
        if (enteredPin == shopProfile.value.pin) {
            _uiState.value = _uiState.value.copy(isAppLocked = false)
            showToast("स्वागत है!")
            return true
        } else {
            showToast("❌ गलत पिन!")
            return false
        }
    }

    fun updateSecurityPin(newPin: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = shopProfile.value.copy(pin = newPin, isPinEnabled = isEnabled)
            repository.updateShopProfile(updated)
            showToast(if (isEnabled) "🔒 सुरक्षा पिन सेट हो गया" else "पिन सुरक्षा बंद कर दी गई")
        }
    }

    fun updateShopSettings(shopName: String, ownerName: String, upiId: String, soundbox: Boolean, autoBackup: Boolean) {
        viewModelScope.launch {
            val updated = shopProfile.value.copy(
                shopName = shopName,
                ownerName = ownerName,
                upiId = upiId,
                soundboxEnabled = soundbox,
                autoBackupEnabled = autoBackup
            )
            repository.updateShopProfile(updated)
            showToast("✅ सेटिंग्स सुरक्षित हो गईं")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.clearAllData()
            showToast("🔄 डेमो डेटा रीसेट हो गया")
        }
    }

    // --- Helpers ---
    fun getCustomerBalance(customerId: Long): Double {
        return transactions.value
            .filter { it.customerId == customerId }
            .sumOf { if (it.type == 1) it.amount else -it.amount }
    }

    fun calculateTotalPending(): Double {
        return customers.value.sumOf { getCustomerBalance(it.id) }
    }

    fun calculateTodayCredit(): Double {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % 86400000L)
        return transactions.value
            .filter { it.type == 1 && it.timestamp >= startOfDay }
            .sumOf { it.amount }
    }

    fun calculateTodayDebit(): Double {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % 86400000L)
        return transactions.value
            .filter { it.type == 2 && it.timestamp >= startOfDay }
            .sumOf { it.amount }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        soundboxManager.shutdown()
    }
}
