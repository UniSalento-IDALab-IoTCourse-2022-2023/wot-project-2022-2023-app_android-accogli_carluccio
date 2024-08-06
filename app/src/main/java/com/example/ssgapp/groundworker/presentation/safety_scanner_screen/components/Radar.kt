package com.example.ssgapp.groundworker.presentation.safety_scanner_screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.ScanningStatus

@Composable
fun Radar(
    status: ScanningStatus,
    modifier: Modifier = Modifier
) {
    val transition1 = rememberInfiniteTransition()
    val transition2 = rememberInfiniteTransition()
    val transition3 = rememberInfiniteTransition()

    val size1 by transition1.animateFloat( // Cerchio esterno
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val size1_2 by transition1.animateFloat( // Cerchio esterno
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val size2 by transition2.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val size2_2 by transition2.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val size3 by transition3.animateFloat(
        initialValue = 1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )


    Canvas(modifier = modifier.fillMaxWidth()) {
        val size = Size(this.size.width, this.size.height)
        val center = Offset(size.width / 2, size.height / 2)
        val radius1 = size.width / 2.5
        val radius1_2 = size.width / 2.9
        val radius2 = size.width / 2.85
        val radius2_2 = size.width / 3.3
        val radius3 = size.width / 3.6

        //Cerchio esterno
        if(status == ScanningStatus.SCANNING || status == ScanningStatus.ALARM) {
            drawCircle(
                color = Color(
                    status.color.red,
                    status.color.green,
                    status.color.blue,
                    alpha = 0.15f
                ), center = center, radius = radius1.toFloat() * size1
            )
            drawCircle(color = Color.White, center = center, radius = radius2.toFloat() * size2)
            drawCircle(
                color = Color(
                    status.color.red,
                    status.color.green,
                    status.color.blue,
                    alpha = 0.45f
                ), center = center, radius = radius1_2.toFloat() * size1_2
            )
            //drawCircle(color = Color.White, center = center, radius = radius2_2.toFloat()*size2_2)
        }
        drawCircle(color = Color(status.color.red, status.color.green, status.color.blue, alpha = 1f), center = center, radius = radius3.toFloat()*size3) // Cerchio interno
    }
}
