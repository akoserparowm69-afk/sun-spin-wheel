package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.WheelItem

private val PRESET_COLORS = listOf(
    "#D0BCFF", // M3 Lavender
    "#EADDFF", // M3 Light Purple
    "#F9DEDC", // M3 Soft Rose
    "#C4EED0", // Soft Mint
    "#C2E7FF", // Soft Sky
    "#FFD8E4", // Soft Peach
    "#E8DEF8", // M3 Purple Gray
    "#FFDBCF", // Soft Coral
    "#F59E0B", // Amber
    "#EC4899", // Pink
    "#10B981", // Emerald
    "#3B82F6"  // Royal Blue
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    title: String,
    messageTemplate: String,
    centerLogoUri: String?,
    items: List<WheelItem>,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onMessageTemplateChange: (String) -> Unit,
    onCenterLogoChange: (String?) -> Unit,
    onAddItem: (String, String, Int) -> Unit,
    onDeleteItem: (String) -> Unit,
    onResetDefaults: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    // New item draft inputs
    var newItemName by remember { mutableStateOf("") }
    var newItemColor by remember { mutableStateOf(PRESET_COLORS[0]) }
    var newItemQuantity by remember { mutableIntStateOf(1) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onCenterLogoChange(uri.toString())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                color = Color(0xFFCAC4D0),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(modifier = Modifier.size(width = 44.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6750A4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✦", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_settings_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF49454F)
                    )
                }
            }

            // Tabs: 0: Items & Slices, 1: Titles & Logo
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF3EDF7),
                contentColor = Color(0xFF6750A4),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF6750A4)
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "ပစ္စည်းများ (Items: ${items.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) Color(0xFF6750A4) else Color(0xFF49454F)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "ခေါင်းစဉ်နှင့် Logo",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) Color(0xFF6750A4) else Color(0xFF49454F)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Items Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section: Add New Item Card
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "+ ပစ္စည်းအသစ်ထည့်ရန် (ADD NEW ITEM)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6750A4),
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Item Name Input
                                    OutlinedTextField(
                                        value = newItemName,
                                        onValueChange = { newItemName = it },
                                        label = { Text("ပစ္စည်းအမည် (Item Name)") },
                                        placeholder = { Text("ဥပမာ - ရွှေဆွဲကြိုး") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("new_item_name_input"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF6750A4),
                                            unfocusedBorderColor = Color(0xFFCAC4D0),
                                            focusedTextColor = Color(0xFF1D1B20),
                                            unfocusedTextColor = Color(0xFF1D1B20),
                                            focusedLabelColor = Color(0xFF6750A4),
                                            unfocusedLabelColor = Color(0xFF49454F),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Quantity Stepper
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "အရေအတွက် / Slices (Quantity):",
                                            fontSize = 13.sp,
                                            color = Color(0xFF49454F),
                                            fontWeight = FontWeight.Medium
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { if (newItemQuantity > 1) newItemQuantity-- },
                                                modifier = Modifier.size(36.dp),
                                                shape = CircleShape,
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                            ) {
                                                Text("-", fontSize = 18.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                                            }

                                            Text(
                                                text = "$newItemQuantity",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D1B20)
                                            )

                                            OutlinedButton(
                                                onClick = { if (newItemQuantity < 50) newItemQuantity++ },
                                                modifier = Modifier.size(36.dp),
                                                shape = CircleShape,
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                            ) {
                                                Text("+", fontSize = 18.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Color Picker Palette
                                    Text(
                                        text = "အရောင် ရွေးချယ်ရန် (COLOR):",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6750A4),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        PRESET_COLORS.forEach { colorHex ->
                                            val color = Color(android.graphics.Color.parseColor(colorHex))
                                            val isSelected = newItemColor.equals(colorHex, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) Color(0xFF6750A4) else Color(0x33000000),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { newItemColor = colorHex },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color(0xFF1D1B20),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            if (newItemName.isNotBlank()) {
                                                onAddItem(newItemName, newItemColor, newItemQuantity)
                                                newItemName = ""
                                                newItemQuantity = 1
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("add_item_button"),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6750A4),
                                            contentColor = Color.White
                                        ),
                                        enabled = newItemName.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ထည့်သွင်းမည် (Add Item)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Section: Slice Count Info Banner
                        item {
                            val totalSlices = items.sumOf { it.quantity }
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color(0xFF21005D),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "စုစုပေါင်း Slices: $totalSlices ခု (အချိုးညီ အညီအမျှ ခွဲဝေထားသည်)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF21005D)
                                    )
                                }
                            }
                        }

                        // Section: Items List
                        item {
                            Text(
                                text = "ACTIVE ITEMS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(items, key = { it.id }) { item ->
                            val itemColor = try {
                                Color(android.graphics.Color.parseColor(item.colorHex))
                            } catch (e: Exception) {
                                Color(0xFFD0BCFF)
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(itemColor)
                                                .border(1.dp, Color(0x33000000), CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = item.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D1B20)
                                            )
                                            Text(
                                                text = "${item.quantity} slices (${(item.quantity * 360f / items.sumOf { it.quantity }.coerceAtLeast(1)).toInt()}°)",
                                                fontSize = 12.sp,
                                                color = Color(0xFF49454F)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteItem(item.id) },
                                        modifier = Modifier.testTag("delete_item_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFB3261E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Title, Message Template & Center Logo Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title Setting
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "HEADER TITLE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6750A4),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = onTitleChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF6750A4),
                                            unfocusedBorderColor = Color(0xFFCAC4D0),
                                            focusedTextColor = Color(0xFF1D1B20),
                                            unfocusedTextColor = Color(0xFF1D1B20),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Message Template Setting
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "WINNER ANNOUNCEMENT TEMPLATE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6750A4),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "အသုံးပြုရန် - {item} နေရာတွင် ပစ္စည်းအမည် အလိုအလျောက် ပေါ်ပါမည်",
                                        fontSize = 11.sp,
                                        color = Color(0xFF49454F)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = messageTemplate,
                                        onValueChange = onMessageTemplateChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF6750A4),
                                            unfocusedBorderColor = Color(0xFFCAC4D0),
                                            focusedTextColor = Color(0xFF1D1B20),
                                            unfocusedTextColor = Color(0xFF1D1B20),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // Center Logo Setting
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "CENTER LOGO / ICON",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6750A4),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Preview
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                                .border(2.dp, Color(0xFF6750A4), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (centerLogoUri != null) {
                                                AsyncImage(
                                                    model = centerLogoUri,
                                                    contentDescription = "Center Logo",
                                                    modifier = Modifier.size(60.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "Default Pin",
                                                    tint = Color(0xFF6750A4)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    photoPickerLauncher.launch("image/*")
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF6750A4)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("ပုံရွေးရန် (Upload Photo)", fontSize = 13.sp)
                                            }

                                            if (centerLogoUri != null) {
                                                OutlinedButton(
                                                    onClick = { onCenterLogoChange(null) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = Color(0xFFB3261E)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("ပုံဖျက်ပြီး မူလအတိုင်းထားမည်", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Reset to Defaults
                        item {
                            OutlinedButton(
                                onClick = onResetDefaults,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFB3261E)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("မူလအခြေအနေသို့ ပြန်ထားမည် (Reset All Defaults)")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
