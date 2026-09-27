package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.ScannedLedgerEntry
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SaffronGold

@Composable
fun PaperScanDialog(
    onDismiss: () -> Unit,
    isScanning: Boolean,
    scannedEntries: List<ScannedLedgerEntry>,
    scannedImagePreview: Bitmap?,
    onImageSelected: (Bitmap) -> Unit,
    onOpenLiveCamera: () -> Unit = {},
    onToggleEntry: (Int) -> Unit,
    onConfirmSave: () -> Unit
) {
    val context = LocalContext.current

    // Camera Capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onImageSelected(bitmap)
        }
    }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onImageSelected(bitmap)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF0E1424))
                .border(1.dp, Color(0xFF243354), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📸 कागज़ खाता स्कैन (AI OCR)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "हाथ की लिखावट, आधा हिंदी आधा English",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera & Gallery action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenLiveCamera,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("take_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("लाइव कैमरा खीचें", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("pick_gallery_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("गैलरी से चुनें", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Image Preview if captured
                if (scannedImagePreview != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap = scannedImagePreview.asImageBitmap(),
                            contentDescription = "Scanned receipt preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Processing Indicator
                if (isScanning) {
                    Column(
                        modifier = Modifier.padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = SaffronGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Gemini AI लिखावट को डिजिटल खाते में बदल रहा है...",
                            color = Color(0xFFFBBF24),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (scannedEntries.isNotEmpty()) {
                    // Display Extracted Entries
                    Text(
                        text = "जाँचें और सेव करें (${scannedEntries.count { it.isSelected }} चुनी गईं):",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(scannedEntries) { index, entry ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF172033))
                                    .clickable { onToggleEntry(index) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = entry.isSelected,
                                        onCheckedChange = { onToggleEntry(index) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = EmeraldGreen,
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.customerName,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${entry.note} • ${if (entry.type == 1) "उधार" else "भुगतान"}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "₹${entry.amount.toInt()}",
                                        color = if (entry.type == 1) Color(0xFFFFB74D) else EmeraldGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onConfirmSave,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("save_scanned_entries")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("✅ सब सेव करें (बही-खाते में जोड़ें)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Empty scan hint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF131A2B))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ऊपर दिए गए 'फोटो खीचें' बटन को दबाकर पुराने पर्चे या कॉपी की फोटो लें।\nGemini AI खराब और कटी-फटी लिखावट को भी पहचानकर नाम और हिसाब निकाल लेता है।",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
