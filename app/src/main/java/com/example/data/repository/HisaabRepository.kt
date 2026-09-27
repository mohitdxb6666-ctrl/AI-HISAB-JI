package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.ShopProfileDao
import com.example.data.dao.TransactionDao
import com.example.data.models.Customer
import com.example.data.models.ShopProfile
import com.example.data.models.Transaction
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class HisaabRepository(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao,
    private val shopProfileDao: ShopProfileDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val shopProfile: Flow<ShopProfile?> = shopProfileDao.getProfile()

    fun getTransactionsForCustomer(customerId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsForCustomer(customerId)

    fun getCustomerById(id: Long): Flow<Customer?> =
        customerDao.getCustomerById(id)

    suspend fun findCustomerByName(name: String): Customer? =
        customerDao.findCustomerByName(name)

    suspend fun insertCustomer(customer: Customer): Long =
        customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) =
        customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) {
        transactionDao.deleteTransactionsForCustomer(customer.id)
        customerDao.deleteCustomer(customer)
    }

    suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.deleteTransaction(transaction)

    suspend fun updateShopProfile(profile: ShopProfile) =
        shopProfileDao.insertOrUpdate(profile)

    suspend fun seedInitialDataIfNeeded() {
        val profile = shopProfileDao.getProfileDirect()
        if (profile == null) {
            shopProfileDao.insertOrUpdate(ShopProfile())
        }

        val existing = customerDao.findCustomerByName("Ramesh Ji")
        if (existing == null) {
            val rameshId = customerDao.insertCustomer(
                Customer(name = "Ramesh Ji", hindiName = "रमेश", phone = "9876543210")
            )
            val sureshId = customerDao.insertCustomer(
                Customer(name = "Suresh Ji", hindiName = "सुरेश", phone = "9876500000")
            )
            val amitId = customerDao.insertCustomer(
                Customer(name = "Amit Ji", hindiName = "अमित", phone = "9812345678")
            )

            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // Ramesh: ₹5000 total balance (₹4000 + ₹2000 - ₹1000 = ₹5000)
            transactionDao.insertTransaction(
                Transaction(customerId = rameshId, amount = 4000.0, type = 1, note = "राशन सामान", timestamp = now - 10 * oneDay)
            )
            transactionDao.insertTransaction(
                Transaction(customerId = rameshId, amount = 2000.0, type = 1, note = "घी व तेल", timestamp = now - 3 * oneDay)
            )
            transactionDao.insertTransaction(
                Transaction(customerId = rameshId, amount = 1000.0, type = 2, note = "नकद मिले", timestamp = now - 1 * oneDay)
            )

            // Suresh: ₹2300 total balance (₹4300 - ₹2000 = ₹2300)
            transactionDao.insertTransaction(
                Transaction(customerId = sureshId, amount = 4300.0, type = 1, note = "थोक सामान", timestamp = now - 7 * oneDay)
            )
            transactionDao.insertTransaction(
                Transaction(customerId = sureshId, amount = 2000.0, type = 2, note = "UPI से मिले", timestamp = now - 1 * oneDay)
            )

            // Amit: ₹850 total balance (₹1650 - ₹800 = ₹850)
            transactionDao.insertTransaction(
                Transaction(customerId = amitId, amount = 1650.0, type = 1, note = "किराना सामान", timestamp = now - 14 * oneDay)
            )
            transactionDao.insertTransaction(
                Transaction(customerId = amitId, amount = 800.0, type = 2, note = "पेमेंट मिला", timestamp = now - 2 * oneDay)
            )
        }
    }

    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("appName", "AI Hisaab Ji")
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())

        val profile = shopProfileDao.getProfileDirect()
        if (profile != null) {
            val profJson = JSONObject()
            profJson.put("shopName", profile.shopName)
            profJson.put("ownerName", profile.ownerName)
            profJson.put("phone", profile.phone)
            profJson.put("upiId", profile.upiId)
            profJson.put("isPinEnabled", profile.isPinEnabled)
            root.put("profile", profJson)
        }

        // We can safely read all customers directly from DAO if needed
        // or through lists
        return root.toString(2)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        customerDao.deleteAllCustomers()
        seedInitialDataIfNeeded()
    }
}
