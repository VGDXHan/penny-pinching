package com.example.voicebill.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicebill.di.ApiKeyValidator
import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.usecase.DataOperationResult
import com.example.voicebill.domain.usecase.ExportImportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val message: String? = null,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val exportImportUseCase: ExportImportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        _uiState.value = _uiState.value.copy(
            apiKey = securePrefs.getApiKey() ?: "",
            hasApiKey = securePrefs.hasApiKey()
        )
    }

    fun onApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
    }

    fun saveApiKey() {
        val validation = ApiKeyValidator.validate(_uiState.value.apiKey)
        if (!validation.isValid) {
            _uiState.value = _uiState.value.copy(
                message = validation.errorMessage
            )
            return
        }

        securePrefs.saveApiKey(validation.normalizedKey)
        _uiState.value = _uiState.value.copy(
            apiKey = validation.normalizedKey,
            hasApiKey = true,
            message = "API Key 已保存"
        )
    }

    fun clearApiKey() {
        securePrefs.clearApiKey()
        _uiState.value = _uiState.value.copy(
            apiKey = "",
            hasApiKey = false,
            message = "API Key 已清除"
        )
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            when (val result = exportImportUseCase.exportData(uri)) {
                is DataOperationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        message = result.message
                    )
                }
                is DataOperationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        message = result.error
                    )
                }
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            when (val result = exportImportUseCase.importData(uri)) {
                is DataOperationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        message = result.message
                    )
                }
                is DataOperationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        message = result.error
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
