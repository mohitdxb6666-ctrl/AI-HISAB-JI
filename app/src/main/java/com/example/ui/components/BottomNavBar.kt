package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.SaffronGold

data class NavItem(
    val tab: AppTab,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun BottomNavBar(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(AppTab.HOME, "हिसाब", Icons.Default.Home, "nav_home"),
        NavItem(AppTab.CUSTOMERS, "ग्राहक", Icons.Default.People, "nav_customers"),
        NavItem(AppTab.AI_MUNIM, "मुनीम AI", Icons.Default.SmartToy, "nav_ai_munim"),
        NavItem(AppTab.ANALYTICS, "रिपोर्ट", Icons.Default.Analytics, "nav_analytics"),
        NavItem(AppTab.SETTINGS, "दुकान", Icons.Default.Settings, "nav_settings")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF090E1B))
            .border(1.dp, Color(0xFF1B243B))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedTab == item.tab
                val tint = if (isSelected) SaffronGold else Color(0xFF94A3B8)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectTab(item.tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag(item.tag)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = item.label,
                        color = tint,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
