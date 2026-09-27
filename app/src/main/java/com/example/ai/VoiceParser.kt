package com.example.ai

data class ParsedVoiceAction(
    val commandType: VoiceCommandType,
    val customerName: String? = null,
    val amount: Double = 0.0,
    val transactionType: Int = 1, // 1 = Credit (उधार), 2 = Debit (पैसे मिले)
    val note: String = "",
    val phoneNumber: String = "",
    val rawSpokenText: String = ""
)

enum class VoiceCommandType {
    TRANSACTION,      // Add credit or payment
    OPEN_CUSTOMER,    // Navigate to customer ledger
    NEW_CUSTOMER,     // Create new customer
    SHOW_QR,          // Open UPI QR code
    WHATSAPP_REMIND,  // Send WhatsApp reminder
    SCAN_PAPER,       // Start paper scanner
    UNKNOWN_OR_QUERY  // Ask AI chatbot
}

object VoiceParser {

    private val hindiNumberWords = mapOf(
        "एक" to 1.0, "दो" to 2.0, "तीन" to 3.0, "चार" to 4.0, "पांच" to 5.0, "पाँच" to 5.0,
        "छह" to 6.0, "सात" to 7.0, "आठ" to 8.0, "नौ" to 9.0, "दस" to 10.0,
        "बीस" to 20.0, "तीस" to 30.0, "चालीस" to 40.0, "पचास" to 50.0,
        "सौ" to 100.0, "सो" to 100.0, "हज़ार" to 1000.0, "हजार" to 1000.0, "लाख" to 100000.0,
        "डेढ़" to 1.5, "ढाई" to 2.5
    )

    fun parse(spokenText: String): ParsedVoiceAction {
        val text = spokenText.trim()
        val lower = text.lowercase()

        // 1. Check direct commands
        if (lower.contains("स्कैन") || lower.contains("scan") || lower.contains("कागज़") || lower.contains("कागज") || lower.contains("पर्ची")) {
            return ParsedVoiceAction(VoiceCommandType.SCAN_PAPER, rawSpokenText = text)
        }

        if (lower.contains("qr") || lower.contains("क्यूट") || lower.contains("क्यूआर") || lower.contains("कोड दिखाओ")) {
            return ParsedVoiceAction(VoiceCommandType.SHOW_QR, rawSpokenText = text)
        }

        if (lower.contains("व्हाट्सएप") || lower.contains("whatsapp") || lower.contains("तकादा") || lower.contains("याद दिला")) {
            val name = extractNameCandidate(text)
            return ParsedVoiceAction(VoiceCommandType.WHATSAPP_REMIND, customerName = name, rawSpokenText = text)
        }

        if (lower.contains("नया ग्राहक") || lower.contains("new customer") || lower.contains("नया खाता")) {
            val name = extractNameCandidate(text) ?: "नया ग्राहक"
            val phone = extractPhoneNumber(text)
            return ParsedVoiceAction(VoiceCommandType.NEW_CUSTOMER, customerName = name, phoneNumber = phone, rawSpokenText = text)
        }

        if (lower.contains("खाता खोलो") || lower.contains("open") || lower.contains("बही खाता") || lower.contains("लेजर")) {
            val name = extractNameCandidate(text)
            if (name != null) {
                return ParsedVoiceAction(VoiceCommandType.OPEN_CUSTOMER, customerName = name, rawSpokenText = text)
            }
        }

        // 2. Transaction extraction
        val amount = extractAmount(text)
        if (amount > 0) {
            val isDebit = lower.contains("मिले") || lower.contains("मिला") || lower.contains("पेमेंट") ||
                    lower.contains("payment") || lower.contains("वसूल") || lower.contains("जमा") ||
                    lower.contains("pay") || lower.contains("दिए") || lower.contains("दीये") || lower.contains("चुका")

            val transType = if (isDebit) 2 else 1
            val name = extractNameCandidate(text)
            val note = extractItemNote(text)

            return ParsedVoiceAction(
                commandType = VoiceCommandType.TRANSACTION,
                customerName = name,
                amount = amount,
                transactionType = transType,
                note = note,
                rawSpokenText = text
            )
        }

        return ParsedVoiceAction(
            commandType = VoiceCommandType.UNKNOWN_OR_QUERY,
            rawSpokenText = text
        )
    }

    private fun extractAmount(text: String): Double {
        // Convert Devanagari numerals
        val converted = text.map { ch ->
            when (ch) {
                '०' -> '0'; '१' -> '1'; '२' -> '2'; '३' -> '3'; '४' -> '4'
                '५' -> '5'; '६' -> '6'; '७' -> '7'; '८' -> '8'; '९' -> '9'
                else -> ch
            }
        }.joinToString("")

        // Match numeric digits
        val regex = Regex("""(?:₹|rs\.?|रुपये|रुपए)?\s*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE)
        val match = regex.find(converted)
        if (match != null) {
            return match.groupValues[1].toDoubleOrNull() ?: 0.0
        }

        // Check for words like "पाँच सौ"
        var total = 0.0
        val tokens = converted.split(Regex("""[\s,]+"""))
        for (token in tokens) {
            val value = hindiNumberWords[token]
            if (value != null) {
                if (value >= 100 && total > 0) {
                    total *= value
                } else {
                    total += value
                }
            }
        }
        return total
    }

    private fun extractNameCandidate(text: String): String? {
        val knownNames = listOf("Ramesh", "Suresh", "Amit", "Priya", "Mohan", "Vikram", "Rahul", "रमेश", "सुरेश", "अमित", "प्रिया", "मोहन", "विक्रम", "राहुल", "राजू", "कमल", "उषा")
        for (name in knownNames) {
            if (text.contains(name, ignoreCase = true)) {
                return if (name.endsWith("Ji") || name.endsWith("जी")) name else "$name Ji"
            }
        }

        // Pattern matching: e.g. "Ramesh ko", "अमित के"
        val regex = Regex("""([A-Za-z\u0900-\u097F]{2,15})(?:\s*(?:को|के|का|की|से|ji|जी|साहब|bhai|भाई))""")
        val match = regex.find(text)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
            if (!candidate.contains("खाता") && !candidate.contains("नया") && !candidate.contains("रुपया")) {
                return "$candidate Ji"
            }
        }
        return null
    }

    private fun extractItemNote(text: String): String {
        val items = listOf("तेल", "दूध", "घी", "चीनी", "आटा", "चावल", "दाल", "मसाला", "साबुन", "चाय", "किराना", "राशन")
        for (item in items) {
            if (text.contains(item)) return item
        }
        return "बोलकर जोड़ा गया"
    }

    private fun extractPhoneNumber(text: String): String {
        val digits = text.filter { it.isDigit() }
        return if (digits.length >= 10) digits.takeLast(10) else ""
    }
}
