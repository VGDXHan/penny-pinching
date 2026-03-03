package com.example.voicebill.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountExpressionEvaluatorTest {

    private val evaluator = AmountExpressionEvaluator()

    @Test
    fun `evaluateToCents should support mixed operators`() {
        val result = evaluator.evaluateToCents("100*0.8+12.5-3")
        assertEquals(8950L, result)
    }

    @Test
    fun `evaluateToCents should support parentheses`() {
        val result = evaluator.evaluateToCents("(10+2.5)+0.5")
        assertEquals(1300L, result)
    }

    @Test
    fun `evaluateToCents should return null for invalid expression`() {
        val result = evaluator.evaluateToCents("100*")
        assertNull(result)
    }
}
