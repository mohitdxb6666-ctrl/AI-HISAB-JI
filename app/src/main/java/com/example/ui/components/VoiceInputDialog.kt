package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.ParsedVoiceAction
import com.example.ai.SpeechRecognizerState
import com.example.ai.VoiceCommandType
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SaffronGold

@Composable
fun VoiceInputDialog(
    speechState: SpeechRecognizerState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onToggleHandsFree: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onVoiceInputReceived: (String) -> Unit,
    parsedAction: ParsedVoiceAction?,
    onConfirmTransaction: (name: String, amount: Double, type: Int, note: String) -> Unit
) {
    var manualTypedText by remember { mutableStateOf("") }

    // Fallback Activity Intent Launcher in case device requires system speech modal
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onVoiceInputReceived(spoken)
            }
        }
    }

    // Auto-start listening with Android SpeechRecognizer when dialog opens
    LaunchedEffect(Unit) {
        if (!speechState.isListening) {
            onStartListening()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (speechState.isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (speechState.isListening) 600 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = {
            onStopListening()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF0F1524))
                .border(1.5.dp, Color(0xFF263353), RoundedCornerShape(30.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎙️ बोलकर हिसाब (Voice STT)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Android SpeechRecognizer द्वारा संचालित",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            onStopListening()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hands-Free Mode Banner (Special for Usha Ji & Kamal Ji)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (speechState.isHandsFreeEnabled)
                                Brush.horizontalGradient(listOf(Color(0xFF831843), Color(0xFFBE185D)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                        )
                        .border(
                            1.dp,
                            if (speechState.isHandsFreeEnabled) Color(0xFFF472B6) else Color(0xFF334155),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "👑 उषा व कमल हैंड्स-फ्री मोड",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (speechState.isHandsFreeEnabled)
                                    "लगातार सुन रहा है • बिना छुए ऑटो-सेव"
                                else
                                    "काउंटर पर रखकर बिना छुए हिसाब दर्ज करें",
                                color = if (speechState.isHandsFreeEnabled) Color(0xFFFCE7F3) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = speechState.isHandsFreeEnabled,
                            onCheckedChange = { enabled ->
                                onToggleHandsFree(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFDB2777)
                            ),
                            modifier = Modifier.testTag("hands_free_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Animated Sound Wave / Mic Button
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            if (speechState.isListening)
                                Brush.radialGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                            else
                                Brush.radialGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                        )
                        .clickable {
                            if (speechState.isListening) {
                                onStopListening()
                            } else {
                                onStartListening()
                            }
                        }
                        .testTag("voice_record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (speechState.isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-Time Audio Level Indicator Bars
                if (speechState.isListening) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(24.dp)
                    ) {
                        val level = speechState.soundLevel
                        repeat(9) { index ->
                            val heightRatio = ((level * (1f + (index % 3) * 0.4f)) * 2.2f).coerceIn(4f, 22f)
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(heightRatio.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (index % 2 == 0) SaffronGold else EmeraldGreen)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "माइक छूकर बोलना शुरू करें",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Live Partial or Spoken Text Feedback
                val displayText = when {
                    speechState.partialText.isNotBlank() -> speechState.partialText
                    speechState.spokenText.isNotBlank() -> speechState.spokenText
                    else -> speechState.statusMessage
                }

                Text(
                    text = displayText,
                    color = if (speechState.partialText.isNotBlank()) SaffronGold else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Parsed Transaction Card
                if (parsedAction != null && parsedAction.commandType == VoiceCommandType.TRANSACTION) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF192238))
                            .border(1.5.dp, SaffronGold, RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🤖 मुनीम जी ने समझा:",
                                color = SaffronGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${parsedAction.rawSpokenText}\"",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = parsedAction.customerName ?: "ग्राहक",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(" • ", color = Color.Gray)
                                Text(
                                    text = "₹${parsedAction.amount.toInt()}",
                                    color = if (parsedAction.transactionType == 1) Color(0xFFFF9800) else EmeraldGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(" • ", color = Color.Gray)
                                Text(
                                    text = if (parsedAction.transactionType == 1) "उधार दिया (+)" else "पैसे मिले (-)",
                                    color = if (parsedAction.transactionType == 1) Color(0xFFFF9800) else EmeraldGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onConfirmTransaction(
                                        parsedAction.customerName ?: "ग्राहक Ji",
                                        parsedAction.amount,
                                        parsedAction.transactionType,
                                        parsedAction.note
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("confirm_voice_transaction")
                            ) {
                                Text("खाते में दर्ज करें ✅", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sample Prompt Pills
                Text(
                    text = "💡 उदाहरण बोलें या छूकर टेस्ट करें:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                val samplePrompts = listOf(
                    "Ramesh ko 500 jod do",
                    "Suresh se 2000 mile",
                    "Amit ko 350 ka tel diya"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePrompts.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161F33))
                                .clickable { onVoiceInputReceived(prompt) }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prompt,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual type fallback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualTypedText,
                        onValueChange = { manualTypedText = it },
                        placeholder = { Text("या यहाँ बोल/लिख सकते हैं...", color = Color.Gray, fontSize = 12.5.sp) },
                        modifier = Modifier.weight(1f).testTag("manual_voice_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF161F33),
                            unfocusedContainerColor = Color(0xFF161F33)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (manualTypedText.isNotBlank()) {
                                onVoiceInputReceived(manualTypedText)
                                manualTypedText = ""
                            }
                        },
                        modifier = Modifier.background(SaffronGold, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                    }
                }
            }
        }
    }
}
