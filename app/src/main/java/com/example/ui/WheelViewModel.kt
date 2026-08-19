package com.example.ui

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WheelItem
import com.example.data.WheelPreferences
import com.example.data.WheelSlice
import com.example.util.SoundEffectManager
import com.example.util.WheelDistributor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class WheelUiState(
    val title: String = WheelPreferences.DEFAULT_TITLE,
    val messageTemplate: String = WheelPreferences.DEFAULT_MESSAGE_TEMPLATE,
    val centerLogoUri: String? = null,
    val items: List<WheelItem> = emptyList(),
    val slices: List<WheelSlice> = emptyList(),
    val rotationAngle: Float = 0f,
    val isSpinning: Boolean = false,
    val winningSlice: WheelSlice? = null,
    val showResultDialog: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val lastWinnerAnnouncement: String = ""
)

class WheelViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = WheelPreferences(application)
    private var soundManager: SoundEffectManager? = null

    private val _uiState = MutableStateFlow(WheelUiState())
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    private val rotationAnimatable = Animatable(0f)

    init {
        try {
            soundManager = SoundEffectManager(application)
        } catch (e: Throwable) {
            soundManager = null
        }
        loadSettings()
    }

    private fun loadSettings() {
        try {
            val title = prefs.getTitle()
            val template = prefs.getMessageTemplate()
            val logoUri = prefs.getCenterLogoUri()
            val items = prefs.getItems()
            val safeItems = if (items.isEmpty()) WheelPreferences.DEFAULT_ITEMS else items
            val slices = WheelDistributor.distributeSlices(safeItems)

            _uiState.update {
                it.copy(
                    title = title,
                    messageTemplate = template,
                    centerLogoUri = logoUri,
                    items = safeItems,
                    slices = slices
                )
            }
        } catch (e: Throwable) {
            val defaultItems = WheelPreferences.DEFAULT_ITEMS
            val defaultSlices = WheelDistributor.distributeSlices(defaultItems)
            _uiState.update {
                it.copy(
                    title = WheelPreferences.DEFAULT_TITLE,
                    messageTemplate = WheelPreferences.DEFAULT_MESSAGE_TEMPLATE,
                    items = defaultItems,
                    slices = defaultSlices
                )
            }
        }
    }

    fun startSpin() {
        val currentSlices = _uiState.value.slices
        if (currentSlices.isEmpty() || _uiState.value.isSpinning) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSpinning = true, showResultDialog = false) }

            val totalSlices = currentSlices.size
            val sliceAngle = 360f / totalSlices.coerceAtLeast(1)

            val fullSpins = Random.nextInt(6, 10) * 360f
            val targetSliceIndex = Random.nextInt(totalSlices.coerceAtLeast(1))
            val jitter = (Random.nextFloat() - 0.5f) * (sliceAngle * 0.7f)
            val targetSliceCenter = targetSliceIndex * sliceAngle + (sliceAngle / 2f) + jitter

            val currentRotation = _uiState.value.rotationAngle
            var targetAngle = currentRotation + fullSpins + (270f - targetSliceCenter - (currentRotation % 360f))
            while (targetAngle < currentRotation + (5 * 360f)) {
                targetAngle += 360f
            }

            var lastTickSliceIndex = -1

            try {
                rotationAnimatable.snapTo(currentRotation)
                rotationAnimatable.animateTo(
                    targetValue = targetAngle,
                    animationSpec = tween(
                        durationMillis = 4500,
                        easing = CubicBezierEasing(0.15f, 0.85f, 0.35f, 1.0f)
                    )
                ) {
                    val currentRot = this.value
                    _uiState.update { it.copy(rotationAngle = currentRot) }

                    // Sound tick on passing slice boundaries
                    val currentSlice = calculateWinningSlice(currentRot, currentSlices)
                    if (currentSlice != null && currentSlice.sliceIndex != lastTickSliceIndex) {
                        lastTickSliceIndex = currentSlice.sliceIndex
                        soundManager?.playTick()
                    }
                }
            } catch (e: Throwable) {
                // Fallback to targetAngle
                _uiState.update { it.copy(rotationAngle = targetAngle) }
            }

            val finalRotation = rotationAnimatable.value
            val winningSlice = calculateWinningSlice(finalRotation, currentSlices)

            val announcement = formatAnnouncement(
                _uiState.value.messageTemplate,
                winningSlice?.name ?: ""
            )

            soundManager?.playWin()

            _uiState.update {
                it.copy(
                    isSpinning = false,
                    winningSlice = winningSlice,
                    lastWinnerAnnouncement = announcement,
                    showResultDialog = true
                )
            }
        }
    }

    private fun calculateWinningSlice(rotation: Float, slices: List<WheelSlice>): WheelSlice? {
        if (slices.isEmpty()) return null
        val totalSlices = slices.size
        val sliceAngle = 360f / totalSlices
        var normalizedAngle = (270f - (rotation % 360f)) % 360f
        if (normalizedAngle < 0f) normalizedAngle += 360f

        val index = ((normalizedAngle / sliceAngle).toInt()) % totalSlices
        return slices.getOrNull(index)
    }

    private fun formatAnnouncement(template: String, itemName: String): String {
        return if (template.contains("{item}")) {
            template.replace("{item}", itemName)
        } else {
            "$template $itemName"
        }
    }

    fun dismissResult() {
        _uiState.update { it.copy(showResultDialog = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettingsSheet = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettingsSheet = false) }
    }

    fun updateTitle(newTitle: String) {
        prefs.saveTitle(newTitle)
        _uiState.update { it.copy(title = newTitle.ifBlank { WheelPreferences.DEFAULT_TITLE }) }
    }

    fun updateMessageTemplate(newTemplate: String) {
        prefs.saveMessageTemplate(newTemplate)
        _uiState.update { it.copy(messageTemplate = newTemplate.ifBlank { WheelPreferences.DEFAULT_MESSAGE_TEMPLATE }) }
    }

    fun setCenterLogo(uriString: String?) {
        prefs.saveCenterLogoUri(uriString)
        _uiState.update { it.copy(centerLogoUri = uriString) }
    }

    fun addItem(name: String, colorHex: String, quantity: Int) {
        if (name.isBlank() || quantity <= 0) return
        val current = _uiState.value.items.toMutableList()
        current.add(WheelItem(name = name.trim(), colorHex = colorHex, quantity = quantity.coerceIn(1, 100)))
        saveAndRecalculate(current)
    }

    fun updateItem(updatedItem: WheelItem) {
        val current = _uiState.value.items.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
        saveAndRecalculate(current)
    }

    fun removeItem(itemId: String) {
        var current = _uiState.value.items.filter { it.id != itemId }
        if (current.isEmpty()) {
            current = WheelPreferences.DEFAULT_ITEMS
        }
        saveAndRecalculate(current)
    }

    fun resetToDefaults() {
        prefs.resetToDefaults()
        loadSettings()
    }

    private fun saveAndRecalculate(newItems: List<WheelItem>) {
        val safeItems = if (newItems.isEmpty()) WheelPreferences.DEFAULT_ITEMS else newItems
        prefs.saveItems(safeItems)
        val slices = WheelDistributor.distributeSlices(safeItems)
        _uiState.update {
            it.copy(
                items = safeItems,
                slices = slices
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            soundManager?.release()
        } catch (e: Throwable) {
            // ignore
        } finally {
            soundManager = null
        }
    }
}
