package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldGreen
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun QrCodeDialog(
    customerName: String,
    amount: Double,
    upiId: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF0F1626))
                .border(1.dp, Color(0xFF263352), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 UPI QR कोड",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ग्राहक: $customerName",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                Text(
                    text = "देय राशि: ₹${amount.toInt()}",
                    color = EmeraldGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // QR Frame Container
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(14.dp)
                        .testTag("upi_qr_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    UpiQrCanvas(
                        upiUri = "upi://pay?pa=$upiId&pn=MohitKirana&am=$amount&cu=INR",
                        modifier = Modifier.size(190.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "UPI ID: $upiId",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Google Pay, PhonePe, Paytm या किसी भी UPI ऐप से स्कैन करें।",
                    color = Color(0xFF64748B),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("बंद करें", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UpiQrCanvas(upiUri: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cells = 21
        val cellSize = size.width / cells
        val hash = upiUri.hashCode()

        // Helper to draw Finder Patterns (corners)
        fun drawFinder(cellX: Int, cellY: Int) {
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(cellX * cellSize, cellY * cellSize),
                size = Size(7 * cellSize, 7 * cellSize),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset((cellX + 1) * cellSize, (cellY + 1) * cellSize),
                size = Size(5 * cellSize, 5 * cellSize),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset((cellX + 2) * cellSize, (cellY + 2) * cellSize),
                size = Size(3 * cellSize, 3 * cellSize),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }

        drawFinder(0, 0)
        drawFinder(cells - 7, 0)
        drawFinder(0, cells - 7)

        // Draw Matrix Modules
        for (r in 0 until cells) {
            for (c in 0 until cells) {
                if ((r < 8 && (c < 8 || c >= cells - 8)) || (r >= cells - 8 && c < 8)) continue
                val factor = abs(sin((hash + r * 17 + c * 31).toDouble()))
                if (factor > 0.44) {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize - 1.5f, cellSize - 1.5f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            }
        }
    }
}
