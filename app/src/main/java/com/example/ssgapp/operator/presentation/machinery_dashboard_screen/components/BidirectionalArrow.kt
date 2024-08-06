package com.example.ssgapp.operator.presentation.machinery_dashboard_screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BidirectionalArrow(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    strokeWidth: Dp = 2.dp,
    arrowLength: Float = 10f,
    dashLength: Float = 10f,
    gapLength: Float = 5f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val path = Path().apply {
            moveTo(arrowLength/2, canvasHeight / 2)
            lineTo(canvasWidth-arrowLength/2, canvasHeight / 2)
        }

        val paint = Paint().apply {
            this.color = color
            this.style = PaintingStyle.Stroke
            this.strokeWidth = strokeWidth.toPx()
            this.pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
            )
        )

        // Draw arrows at both ends
        drawArrowHead(Offset(0f, canvasHeight / 2), arrowLength, color, isStart = false)
        drawArrowHead(Offset(canvasWidth, canvasHeight / 2), arrowLength, color, isStart = true)
    }
}

fun DrawScope.drawArrowHead(
    position: Offset,
    length: Float,
    color: Color,
    isStart: Boolean = true
) {
    val angle = if (isStart) 180f else 0f
    val path = Path().apply {
        moveTo(position.x, position.y)
        lineTo(
            (position.x + length * cos((angle - 30).toRadians())).toFloat(),
            (position.y + length * sin((angle - 30).toRadians())).toFloat()
        )
        lineTo(
            (position.x + length * cos((angle + 30).toRadians())).toFloat(),
            (position.y + length * sin((angle + 30).toRadians())).toFloat()
        )
        close()
    }
    drawPath(
        path = path,
        color = color
    )
}

fun Float.toRadians() = this * PI / 180f

@Preview(showBackground = true)
@Composable
fun PreviewBidirectionalArrow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        BidirectionalArrow(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            arrowLength = 15f,
            dashLength = 10f,
            gapLength = 5f
        )
    }
}
