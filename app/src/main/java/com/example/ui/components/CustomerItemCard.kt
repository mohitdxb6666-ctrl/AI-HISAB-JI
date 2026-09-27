package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CustomerItemCard(
    customer: Customer,
    balance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }
    val formattedBalance = formatter.format(balance)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF131A2D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("customer_card_${customer.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF1E2C4C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Customer icon",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name and Status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (balance > 0) "बाकी" else "खाता चुकता",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Balance & WhatsApp reminder button
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedBalance,
                    color = if (balance > 0) Color.White else Color(0xFF10B981),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // WhatsApp reminder button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2417))
                        .clickable {
                            val msg = "नमस्ते ${customer.name} जी 🙏\nआपके खाते में $formattedBalance बाकी हैं।\nकृपया सुविधानुसार भुगतान कर दें।\nधन्यवाद।\n- AI Hisaab Ji"
                            val cleanPhone = customer.phone.filter { it.isDigit() }
                            val uri = if (cleanPhone.length >= 10) {
                                Uri.parse("https://wa.me/91$cleanPhone?text=${Uri.encode(msg)}")
                            } else {
                                Uri.parse("https://wa.me/?text=${Uri.encode(msg)}")
                            }
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("wa_remind_${customer.id}")
                ) {
                    Text(
                        text = "📲 याद दिलाएं",
                        color = Color(0xFFFFB74D),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
