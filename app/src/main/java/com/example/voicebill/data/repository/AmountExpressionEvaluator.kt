package com.example.voicebill.data.repository

import java.math.BigDecimal
import java.math.RoundingMode

class AmountExpressionEvaluator {

    fun evaluateToCents(expression: String): Long? {
        return runCatching {
            val valueInYuan = Parser(expression).parse()
            if (valueInYuan <= BigDecimal.ZERO) {
                return null
            }
            valueInYuan
                .multiply(HUNDRED)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull()
    }

    private class Parser(private val input: String) {
        private var index = 0

        fun parse(): BigDecimal {
            val value = parseExpression()
            skipWhitespace()
            if (index != input.length) {
                throw IllegalArgumentException("unexpected token")
            }
            return value
        }

        private fun parseExpression(): BigDecimal {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                value = when {
                    match('+') -> value + parseTerm()
                    match('-') -> value - parseTerm()
                    else -> return value
                }
            }
        }

        private fun parseTerm(): BigDecimal {
            var value = parseFactor()
            while (true) {
                skipWhitespace()
                value = when {
                    match('*') -> value.multiply(parseFactor())
                    match('/') -> {
                        val divisor = parseFactor()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw IllegalArgumentException("division by zero")
                        }
                        value.divide(divisor, DIVISION_SCALE, RoundingMode.HALF_UP)
                    }
                    else -> return value
                }
            }
        }

        private fun parseFactor(): BigDecimal {
            skipWhitespace()
            return when {
                match('+') -> parseFactor()
                match('-') -> parseFactor().negate()
                match('(') -> {
                    val value = parseExpression()
                    skipWhitespace()
                    if (!match(')')) {
                        throw IllegalArgumentException("missing closing parenthesis")
                    }
                    value
                }
                else -> parseNumber()
            }
        }

        private fun parseNumber(): BigDecimal {
            skipWhitespace()
            val start = index
            var hasDigit = false

            while (index < input.length && input[index].isDigit()) {
                index++
                hasDigit = true
            }
            if (index < input.length && input[index] == '.') {
                index++
                while (index < input.length && input[index].isDigit()) {
                    index++
                    hasDigit = true
                }
            }

            if (!hasDigit) {
                throw IllegalArgumentException("number expected")
            }
            return input.substring(start, index).toBigDecimal()
        }

        private fun skipWhitespace() {
            while (index < input.length && input[index].isWhitespace()) {
                index++
            }
        }

        private fun match(ch: Char): Boolean {
            if (index < input.length && input[index] == ch) {
                index++
                return true
            }
            return false
        }
    }

    private companion object {
        private val HUNDRED = BigDecimal("100")
        private const val DIVISION_SCALE = 10
    }
}
