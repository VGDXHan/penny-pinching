package com.example.voicebill.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.voicebill.domain.model.CategorySummary
import com.example.voicebill.ui.theme.PieChartColors
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun PieChartCard(
    title: String,
    data: List<CategorySummary>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 左侧：饼图
                    PieChartCanvas(
                        data = data,
                        selectedCategoryId = selectedCategoryId,
                        onCategoryClick = onCategoryClick,
                        modifier = Modifier.size(150.dp)
                    )

                    // 右侧：图例
                    PieChartLegend(
                        data = data,
                        selectedCategoryId = selectedCategoryId,
                        onCategoryClick = onCategoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PieChartCanvas(
    data: List<CategorySummary>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(data, selectedCategoryId) {
                detectTapGestures { offset ->
                    val clickedCategoryId = calculateClickedSector(
                        offset = offset,
                        size = size,
                        data = data
                    )
                    if (clickedCategoryId == selectedCategoryId) {
                        onCategoryClick(null)
                    } else {
                        onCategoryClick(clickedCategoryId)
                    }
                }
            }
    ) {
        val radius = size.minDimension / 2f * 0.9f
        val center = Offset(size.width / 2f, size.height / 2f)
        var startAngle = -90f

        data.forEachIndexed { index, category ->
            val sweepAngle = category.percentage * 360f
            val color = PieChartColors[index % PieChartColors.size]
            val isSelected = category.categoryId == selectedCategoryId

            val displayColor = if (isSelected) {
                color.copy(alpha = 1f)
            } else {
                color.copy(alpha = 0.85f)
            }

            drawArc(
                color = displayColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = center - Offset(radius, radius),
                size = Size(radius * 2, radius * 2)
            )

            if (isSelected) {
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = center - Offset(radius, radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            startAngle += sweepAngle
        }
    }
}

private fun calculateClickedSector(
    offset: Offset,
    size: IntSize,
    data: List<CategorySummary>
): Long? {
    val center = Offset(size.width / 2f, size.height / 2f)
    val minDimension = minOf(size.width, size.height).toFloat()
    val radius = minDimension / 2f * 0.9f

    val distance = sqrt((offset.x - center.x).pow(2) + (offset.y - center.y).pow(2))
    if (distance > radius) return null

    val angleRad = atan2(offset.y - center.y, offset.x - center.x)
    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
    val angle = ((angleDeg + 90 + 360) % 360)

    var startAngle = 0f
    data.forEachIndexed { index, category ->
        val sweepAngle = category.percentage * 360f
        val endAngle = startAngle + sweepAngle
        val isLastSector = index == data.size - 1
        val actualEndAngle = if (isLastSector) 360f else endAngle

        if (angle >= startAngle && angle < actualEndAngle) {
            return category.categoryId
        }
        startAngle = endAngle
    }
    return null
}

@Composable
private fun PieChartLegend(
    data: List<CategorySummary>,
    selectedCategoryId: Long?,
    onCategoryClick: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        data.forEachIndexed { index, category ->
            val isSelected = category.categoryId == selectedCategoryId
            val color = PieChartColors[index % PieChartColors.size]

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSelected) {
                            onCategoryClick(null)
                        } else {
                            onCategoryClick(category.categoryId)
                        }
                    },
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.Transparent,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = color,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${(category.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "¥${String.format("%.2f", category.amountCents / 100.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
