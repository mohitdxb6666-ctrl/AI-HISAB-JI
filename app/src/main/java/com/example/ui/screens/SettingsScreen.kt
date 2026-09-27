package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ShopProfile
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SaffronGold

@Composable
fun SettingsScreen(
    profile: ShopProfile,
    onSaveProfile: (shopName: String, ownerName: String, upiId: String, soundbox: Boolean, autoBackup: Boolean) -> Unit,
    onOpenSetPinDialog: () -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var shopName by remember(profile) { mutableStateOf(profile.shopName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var upiId by remember(profile) { mutableStateOf(profile.upiId) }
    var soundboxEnabled by remember(profile) { mutableStateOf(profile.soundboxEnabled) }
    var autoBackup by remember(profile) { mutableStateOf(profile.autoBackupEnabled) }

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
                    text = "⚙️ दुकान व ऐप सेटिंग्स",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "दुकान प्रोफाइल, बैकअप, पासवर्ड और वॉइस बॉक्स",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Shop Profile Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF131A2D))
                    .border(1.dp, Color(0xFF22304F), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = "Shop", tint = SaffronGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("दुकान का विवरण", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("दुकान का नाम", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1B243B),
                            unfocusedContainerColor = Color(0xFF1B243B)
                        )
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("दुकानदार का नाम", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1B243B),
                            unfocusedContainerColor = Color(0xFF1B243B)
                        )
                    )

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI आईडी (QR कोड के लिए)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1B243B),
                            unfocusedContainerColor = Color(0xFF1B243B)
                        )
                    )

                    Button(
                        onClick = {
                            onSaveProfile(shopName, ownerName, upiId, soundboxEnabled, autoBackup)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
                    ) {
                        Text("विवरण सुरक्षित करें ✅", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Voice Soundbox Toggle
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF131A2D))
                    .border(1.dp, Color(0xFF22304F), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Soundbox", tint = SaffronGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("साउंडबॉक्स वॉइस अलर्ट", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("हिसाब जुड़ते ही बोलकर बताएगा", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        }
                    }

                    Switch(
                        checked = soundboxEnabled,
                        onCheckedChange = {
                            soundboxEnabled = it
                            onSaveProfile(shopName, ownerName, upiId, soundboxEnabled, autoBackup)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SaffronGold)
                    )
                }
            }
        }

        // Security PIN / Password Protection
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF131A2D))
                    .border(1.dp, Color(0xFF22304F), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color(0xFFF87171))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("पासवर्ड / 4-डिजिट पिन लॉक", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(if (profile.isPinEnabled) "पिन सक्रिय है" else "पिन बंद है", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                            }
                        }

                        Switch(
                            checked = profile.isPinEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && profile.pin.isBlank()) {
                                    onOpenSetPinDialog()
                                } else {
                                    onTogglePin(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF87171))
                        )
                    }

                    if (profile.isPinEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🔄 पिन बदलें",
                            color = SaffronGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOpenSetPinDialog() }
                        )
                    }
                }
            }
        }

        // Google Drive Backup Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF131A2D))
                    .border(1.dp, Color(0xFF22304F), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Drive backup", tint = Color(0xFF60A5FA))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Google Drive बैकअप व सुरक्षित सेव", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("फोन खोने पर भी खाता हमेशा सुरक्षित", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val backupText = "AI Hisaab Ji Backup Data\nShop: $shopName\nDate: ${System.currentTimeMillis()}"
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "AI_Hisaab_Ji_Backup.json")
                                putExtra(Intent.EXTRA_TEXT, backupText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Save to Google Drive"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("backup_drive_button")
                    ) {
                        Text("☁️ Google Drive में सेव करें", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Reset demo data
        item {
            Button(
                onClick = onResetData,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.width(6.dp))
                Text("डेटा रीसेट करें (डेमो लोड करें)", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
