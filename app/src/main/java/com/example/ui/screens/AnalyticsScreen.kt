package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.data.models.Transaction
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SaffronGold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    customers: List<Customer>,
    transactions: List<Transaction>,
    customerBalances: (Long) -> Double,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    val totalCredit = transactions.filter { it.type == 1 }.sumOf { it.amount }
    val totalDebit = transactions.filter { it.type == 2 }.sumOf { it.amount }
    val totalPending = totalCredit - totalDebit
    val recoveryRate = if (totalCredit > 0) ((totalDebit / totalCredit) * 100).toInt().coerceIn(0, 100) else 0

    val now = System.currentTimeMillis()
    val startOfDay = now - (now % 86400000L)
    val todayTxnCount = transactions.count { it.timestamp >= startOfDay }

    val topDebtors = customers.map { it to customerBalances(it.id) }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "📊 उत्पादकता व खाता रिपोर्ट",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "दुकान की रिकवरी और दैनिक उत्पादकता",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Productivity Score Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF131A2D))
                    .border(1.dp, Color(0xFF22304F), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ आज की उत्पादकता",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$todayTxnCount लेनदेन दर्ज",
                            color = SaffronGold,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "वसूली दर (Recovery Rate): $recoveryRate%",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (recoveryRate / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = EmeraldGreen,
                        trackColor = Color(0xFF1E283F),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "💡 सुझाव: 70% से अधिक रिकवरी दर से दुकान का कैश फ्लो मजबूत रहता है।",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // Financial Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1E293B))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("कुल उधारी दी गई", color = Color(0xFFFFA000), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatter.format(totalCredit), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1E293B))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("कुल वसूली आई", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatter.format(totalDebit), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Top Debtors
        item {
            Text(
                text = "⚠️ शीर्ष बकायेदार (Top Debtors)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(topDebtors.size) { index ->
            val (cust, bal) = topDebtors[index]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111827))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cust.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(if (cust.phone.isNotBlank()) cust.phone else "नियमित ग्राहक", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Text(
                        formatter.format(bal),
                        color = Color(0xFFFF7A7A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
