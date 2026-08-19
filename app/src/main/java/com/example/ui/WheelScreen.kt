package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ResultDialog
import com.example.ui.components.SettingsSheet
import com.example.ui.components.SpinWheelCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen(
    viewModel: WheelViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!state.isSpinning) 1.025f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Color(0xFFFEF7FF),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        // Geometric Balance Sparkle Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6750A4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Lucky Spin",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color(0xFF1D1B20),
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    // Geometric Balance Settings Icon Button (Lavender Circle)
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEADDFF))
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF21005D),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEF7FF)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFEF7FF)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header / Subtitle Text Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "READY TO TEST YOUR LUCK?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20),
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Total ${state.slices.size} Slices • Interleaved Distribution",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6750A4)
                    )
                }

                // Geometric Balance Wheel Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SpinWheelCanvas(
                        slices = state.slices,
                        rotationAngle = state.rotationAngle,
                        isSpinning = state.isSpinning,
                        centerLogoUri = state.centerLogoUri,
                        modifier = Modifier.fillMaxWidth(0.96f)
                    )
                }

                // Bottom Action: Geometric Balance Pill Button (Rounded 28dp, #6750A4)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.startSpin() },
                        enabled = !state.isSpinning && state.slices.isNotEmpty(),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .scale(if (!state.isSpinning) pulseScale else 1f)
                            .shadow(
                                elevation = if (!state.isSpinning) 10.dp else 2.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = Color(0x336750A4)
                            )
                            .testTag("start_spin_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            disabledContainerColor = Color(0xFFCAC4D0),
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF49454F)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "✦",
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (state.isSpinning) "လှည့်နေသည်... (Spinning)" else "ကံစမ်းမယ် (Start SPIN)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Geometric Balance Navigation Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6750A4))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "WHEEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Result Popup Modal
    if (state.showResultDialog) {
        ResultDialog(
            winningSlice = state.winningSlice,
            announcementText = state.lastWinnerAnnouncement,
            onDismiss = { viewModel.dismissResult() },
            onSpinAgain = {
                viewModel.dismissResult()
                viewModel.startSpin()
            }
        )
    }

    // Settings Modal Bottom Sheet
    if (state.showSettingsSheet) {
        SettingsSheet(
            title = state.title,
            messageTemplate = state.messageTemplate,
            centerLogoUri = state.centerLogoUri,
            items = state.items,
            onDismiss = { viewModel.closeSettings() },
            onTitleChange = { viewModel.updateTitle(it) },
            onMessageTemplateChange = { viewModel.updateMessageTemplate(it) },
            onCenterLogoChange = { viewModel.setCenterLogo(it) },
            onAddItem = { name, colorHex, quantity ->
                viewModel.addItem(name, colorHex, quantity)
            },
            onDeleteItem = { itemId ->
                viewModel.removeItem(itemId)
            },
            onResetDefaults = {
                viewModel.resetToDefaults()
            }
        )
    }
}

