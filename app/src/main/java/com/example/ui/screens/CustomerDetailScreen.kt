package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.data.models.Transaction
import com.example.ui.theme.CardSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.WhatsAppGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerDetailScreen(
    customer: Customer,
    balance: Double,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onAddCredit: () -> Unit,
    onAddDebit: () -> Unit,
    onShowQr: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onDeleteCustomer: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val formattedBalance = formatter.format(balance)
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale("hi", "IN"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (customer.phone.isNotBlank()) "📞 ${customer.phone}" else "खाता संख्या #${customer.id}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { onDeleteCustomer(customer) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete customer",
                    tint = Color(0xFFEF4444)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Balance Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF131A2E))
                        .border(1.dp, Color(0xFF243254), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (balance > 0) "कुल बाकी (बाज़ार में अटका)" else "खाता चुकता है",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formattedBalance,
                            color = if (balance > 0) Color(0xFFFFB74D) else EmeraldGreen,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // WhatsApp Reminder
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1A3326))
                                    .clickable {
                                        val msg = "नमस्ते ${customer.name} जी 🙏\nआपकी दुकान का कुल बकाया $formattedBalance है। कृपया यथाशीघ्र भुगतान करें।\nधन्यवाद।"
                                        val cleanPhone = customer.phone.filter { it.isDigit() }
                                        val uri = if (cleanPhone.length >= 10) {
                                            Uri.parse("https://wa.me/91$cleanPhone?text=${Uri.encode(msg)}")
                                        } else {
                                            Uri.parse("https://wa.me/?text=${Uri.encode(msg)}")
                                        }
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📲 WhatsApp तकादा",
                                    color = WhatsAppGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // QR Code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF2E2416))
                                    .clickable { onShowQr() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCode, contentDescription = "QR", tint = SaffronGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "QR कोड दिखाएं",
                                        color = SaffronGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ledger History Title
            item {
                Text(
                    text = "📜 बही-खाता इतिहास (${transactions.size} लेनदेन)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Transactions list
            items(transactions) { txn ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111728))
                        .border(1.dp, Color(0xFF1E2840), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Type Indicator Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (txn.type == 1) Color(0xFF3B1E1E) else Color(0xFF173826),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (txn.type == 1) "⬆️" else "⬇️",
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (txn.note.isNotBlank()) txn.note else if (txn.type == 1) "उधार माल" else "नकद प्राप्त",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${dateFormat.format(Date(txn.timestamp))} • ${if (txn.source == 1) "🎙️ बोलकर" else if (txn.source == 2) "📸 स्कैन" else "हाथ से"}",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (txn.type == 1) "+" else "-"} ₹${txn.amount.toInt()}",
                                color = if (txn.type == 1) Color(0xFFFF7A7A) else EmeraldGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )

                            IconButton(
                                onClick = { onDeleteTransaction(txn) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B101D))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddCredit,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronGold),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_add_credit")
            ) {
                Text("➕ उधार दिया", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAddDebit,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_add_debit")
            ) {
                Text("💰 पैसे मिले", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
