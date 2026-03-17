package com.mocara.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Scanner State
 */
sealed class ScannerState {
    object Idle : ScannerState()
    object Scanning : ScannerState()
    data class Success(val drugId: String) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

/**
 * ScannerViewModel
 * Manages barcode/OCR scanning state and navigation
 */
class ScannerViewModel : ViewModel() {

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    // Drug ID mapping (mock - simulates barcode to drug lookup)
    private val barcodeMapping = mapOf(
        "123456789" to "ozempic",
        "987654321" to "insulin",
        "555555555" to "metformin",
        // Default mapping
        "default" to "ozempic"
    )

    /**
     * Start scanning
     */
    fun startScanning() {
        _scannerState.value = ScannerState.Scanning
    }

    /**
     * Process scanned barcode
     */
    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            try {
                // Look up drug from barcode
                val drugId = barcodeMapping[barcode] ?: barcodeMapping["default"]!!

                _scannerState.value = ScannerState.Success(drugId)
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Failed to process barcode: ${e.message}")
            }
        }
    }

    /**
     * Process OCR text result
     */
    fun onTextRecognized(text: String) {
        viewModelScope.launch {
            try {
                // Simple text matching for drug names
                val drugId = when {
                    text.contains("ozempic", ignoreCase = true) -> "ozempic"
                    text.contains("semaglutide", ignoreCase = true) -> "ozempic"
                    text.contains("insulin", ignoreCase = true) -> "insulin"
                    text.contains("metformin", ignoreCase = true) -> "metformin"
                    else -> "ozempic" // Default
                }

                _scannerState.value = ScannerState.Success(drugId)
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Failed to recognize text: ${e.message}")
            }
        }
    }

    /**
     * Manual drug entry (for testing)
     */
    fun selectDrugManually(drugId: String) {
        _scannerState.value = ScannerState.Success(drugId)
    }

    /**
     * Reset scanner state
     */
    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }

    /**
     * Handle scan error
     */
    fun onScanError(error: String) {
        _scannerState.value = ScannerState.Error(error)
    }
}