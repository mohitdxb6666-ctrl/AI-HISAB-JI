package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun HeaderBanner(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Dedicated Parental Dedication Banner (As requested by user!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF7A00), Color(0xFFFF9500), Color(0xFFFF5500))
                    )
                )
                .padding(vertical = 7.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "✨ मेरे माता-पिता उषा जी व कमल जी को समर्पित 🙏",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Dedicated to my parents Usha & Kamal • MADE IN INDIA 🇮🇳",
                    color = Color(0xFFFFF1D6),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Big Prominent Branding Header
        Text(
            text = "AI HISAAB JI -",
            color = Color(0xFFFBBF24),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Text(
            text = "आपका डिजिटल मुनीम",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Golden Pill Badge
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SaffronGold, SaffronDark)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "BHARAT KA PEHLA VOICE CONTROLLED KHATA APP",
                color = Color.Black,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "बस लिखो या बोलो, हिसाब हो जाएगा।",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
