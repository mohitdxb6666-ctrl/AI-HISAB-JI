package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SaffronGold

@Composable
fun NewCustomerDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, hindiName: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hindiName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF0F1626))
                .border(1.dp, Color(0xFF263352), RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "➕ नया ग्राहक खाता जोड़ें",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "खातेदार का नाम और फ़ोन नंबर दर्ज करें",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ग्राहक का नाम (जैसे Ramesh Ji)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("new_customer_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF172033),
                        unfocusedContainerColor = Color(0xFF172033)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hindiName,
                    onValueChange = { hindiName = it },
                    label = { Text("हिंदी नाम (वैकल्पिक: जैसे रमेश)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().testTag("new_customer_hindi_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF172033),
                        unfocusedContainerColor = Color(0xFF172033)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) phone = it },
                    label = { Text("मोबाइल नंबर (WhatsApp के लिए)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("new_customer_phone_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF172033),
                        unfocusedContainerColor = Color(0xFF172033)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243048)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("रद्द", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name, hindiName, phone)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.4f).testTag("save_new_customer_button")
                    ) {
                        Text("खाता बनाएं ✅", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
