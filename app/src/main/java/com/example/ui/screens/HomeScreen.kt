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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.ui.HisaabUiState
import com.example.ui.components.ActionButtonsGrid
import com.example.ui.components.CustomerItemCard
import com.example.ui.components.HeaderBanner
import com.example.ui.components.MasterBalanceCard
import com.example.ui.theme.CardSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WhatsAppGreen

@Composable
fun HomeScreen(
    uiState: HisaabUiState,
    customers: List<Customer>,
    customerBalances: (Long) -> Double,
    totalPending: Double,
    todayCredit: Double,
    todayDebit: Double,
    onCustomerClick: (Long) -> Unit,
    onVoiceClick: () -> Unit,
    onScanClick: () -> Unit,
    onNewCustomerClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onViewAllCustomers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filteredCustomers = customers.filter {
        it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                it.hindiName.contains(uiState.searchQuery, ignoreCase = true)
    }.sortedByDescending { customerBalances(it.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderBanner()
        }

        item {
            MasterBalanceCard(
                totalPending = totalPending,
                customerCount = customers.size,
                todayCredit = todayCredit,
                todayDebit = todayDebit
            )
        }

        item {
            ActionButtonsGrid(
                onVoiceClick = onVoiceClick,
                onScanClick = onScanClick,
                onNewCustomerClick = onNewCustomerClick
            )
        }

        // Search Box
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("🔍 ग्राहक खोजें (नाम या फ़ोन)...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth().testTag("home_search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF131A2D),
                    unfocusedContainerColor = Color(0xFF131A2D),
                    focusedIndicatorColor = SaffronGold,
                    unfocusedIndicatorColor = Color(0xFF22304F)
                )
            )
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👥 ग्राहक",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2C2417), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FREE Unlimited",
                            color = SaffronGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "सभी देखें →",
                    color = SaffronGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewAllCustomers() }
                )
            }
        }

        // Top 5 Customers List
        items(filteredCustomers.take(5)) { customer ->
            val bal = customerBalances(customer.id)
            CustomerItemCard(
                customer = customer,
                balance = bal,
                onClick = { onCustomerClick(customer.id) }
            )
        }

        // Promo Referral Banner as seen in reference screenshot
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFE58700), Color(0xFFFFA000))
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎁", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "दोस्त को जोड़ो, ₹500 पाओ",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "दूसरे दुकानदार भाई को शेयर करो, वो 10 ग्राहक जोड़े तो ₹500",
                        color = Color(0xFF3E2000),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "भाई मैंने अपनी दुकान के लिए 'AI Hisaab Ji - आपका डिजिटल मुनीम' ऐप शुरू किया है। इसमें बोलकर हिसाब हो जाता है और पर्ची का फोटो लेते ही सारा खाता दर्ज हो जाता है! आप भी इस्तेमाल करें।"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share with shopkeeper friends"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = WhatsAppGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔗 WhatsApp पर भेजें", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // How to use 2-minute tutorial card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF131A2D))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "📖 कैसे use करें? 2 मिनट",
                        color = SaffronGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).background(Color(0xFFFFA000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Scan", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("कागज़ की फोटो लो", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("पूरी कॉपी एक बार में digital", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).background(Color(0xFFFFA000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("बोल के डालो", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("जैसे: 'Ramesh ko 500 jod do'", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).background(Color(0xFFFFA000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📲", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("WhatsApp reminder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("एक tap में याद दिलाओ", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // Developer Dedication Footer Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0C1322))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SaffronGold, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("UK", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Developed by", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("Usha Kamal (UK Fintech)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Dedicated to my parents Usha & Kamal 🙏",
                        color = Color(0xFFFFB74D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🇮🇳 Bharat Ka Pehla Voice Controlled Khata App",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "सरल • तेज़ • भरोसेमंद • स्थानीय भाषा सपोर्ट • 100% सुरक्षित",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
