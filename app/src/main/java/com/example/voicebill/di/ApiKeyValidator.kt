package com.example.voicebill.di

data class ApiKeyValidationResult(
    val normalizedKey: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)

object ApiKeyValidator {

    fun normalize(raw: String): String {
        val trimmed = raw.trim()
        val withoutBearer = if (trimmed.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            trimmed.substring(BEARER_PREFIX.length)
        } else {
            trimmed
        }
        return withoutBearer.trim()
    }

    fun validate(raw: String): ApiKeyValidationResult {
        val normalized = normalize(raw)
        if (normalized.isBlank()) {
            return ApiKeyValidationResult(
                normalizedKey = "",
                isValid = false,
                errorMessage = "API Key 不能为空"
            )
        }

        // Header 值只能使用可见 ASCII，避免 Authorization 头非法字符异常。
        val hasInvalidChar = normalized.any { it.code !in VISIBLE_ASCII_RANGE }
        if (hasInvalidChar) {
            return ApiKeyValidationResult(
                normalizedKey = normalized,
                isValid = false,
                errorMessage = "API Key 格式不正确：仅支持英文、数字和符号"
            )
        }

        return ApiKeyValidationResult(
            normalizedKey = normalized,
            isValid = true
        )
    }

    private const val BEARER_PREFIX = "Bearer "
    private val VISIBLE_ASCII_RANGE = 0x21..0x7E
}
