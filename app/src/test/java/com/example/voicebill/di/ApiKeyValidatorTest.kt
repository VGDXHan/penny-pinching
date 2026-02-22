package com.example.voicebill.di

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyValidatorTest {

    @Test
    fun validate_whenBlank_shouldReturnInvalid() {
        val result = ApiKeyValidator.validate("   ")

        assertFalse(result.isValid)
        assertEquals("API Key 不能为空", result.errorMessage)
    }

    @Test
    fun validate_whenContainsChinese_shouldReturnInvalid() {
        val result = ApiKeyValidator.validate("sk-abc来123")

        assertFalse(result.isValid)
        assertEquals("API Key 格式不正确：仅支持英文、数字和符号", result.errorMessage)
    }

    @Test
    fun validate_whenPrefixedWithBearer_shouldNormalizeAndPass() {
        val result = ApiKeyValidator.validate("Bearer sk-abc_123")

        assertTrue(result.isValid)
        assertEquals("sk-abc_123", result.normalizedKey)
    }

    @Test
    fun validate_whenVisibleAsciiOnly_shouldPass() {
        val result = ApiKeyValidator.validate("sk-abc_123-XYZ")

        assertTrue(result.isValid)
        assertEquals("sk-abc_123-XYZ", result.normalizedKey)
    }
}
