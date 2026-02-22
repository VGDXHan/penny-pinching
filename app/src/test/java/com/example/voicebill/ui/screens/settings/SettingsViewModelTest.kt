package com.example.voicebill.ui.screens.settings

import com.example.voicebill.di.SecurePrefs
import com.example.voicebill.domain.usecase.ExportImportUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private val securePrefs: SecurePrefs = mockk(relaxed = true)
    private val exportImportUseCase: ExportImportUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { securePrefs.getApiKey() } returns null
        every { securePrefs.hasApiKey() } returns false
    }

    @Test
    fun saveApiKey_whenInvalid_shouldNotPersist() {
        val viewModel = SettingsViewModel(securePrefs, exportImportUseCase)
        viewModel.onApiKeyChanged("sk-abc来123")

        viewModel.saveApiKey()

        verify(exactly = 0) { securePrefs.saveApiKey(any()) }
        assertEquals("API Key 格式不正确：仅支持英文、数字和符号", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.hasApiKey)
    }

    @Test
    fun saveApiKey_whenValid_shouldPersistNormalizedKey() {
        val viewModel = SettingsViewModel(securePrefs, exportImportUseCase)
        viewModel.onApiKeyChanged("  Bearer sk-abc_123  ")

        viewModel.saveApiKey()

        verify(exactly = 1) { securePrefs.saveApiKey("sk-abc_123") }
        assertTrue(viewModel.uiState.value.hasApiKey)
        assertEquals("API Key 已保存", viewModel.uiState.value.message)
        assertEquals("sk-abc_123", viewModel.uiState.value.apiKey)
    }
}
