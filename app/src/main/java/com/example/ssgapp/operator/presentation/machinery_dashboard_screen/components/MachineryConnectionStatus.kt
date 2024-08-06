package com.example.ssgapp.operator.presentation.machinery_dashboard_screen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssgapp.operator.domain.model.ConnectionStatus
import com.example.ssgapp.operator.domain.model.DevicesConnectionStatus
import com.example.ssgapp.ui.theme.SSGAppTheme
import kotlinx.coroutines.delay
import kotlin.math.pow


@Composable
fun MachineryConnectionStatus(
    devicesConnectionStatus: DevicesConnectionStatus,
    isRemote: Boolean,
    rssiValue: Int,
    isRaspberry: Boolean, // ho dovuto mettere questa variabile per scopo di test, essendo che tra raspberry e iphone, un certo valore di rssi indica distanze completamente differenti.
    modifier: Modifier
) {

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {

            // Left Component
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    //.background(Color.Red)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(devicesConnectionStatus.device1Status.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = devicesConnectionStatus.device1Name,
                    fontSize = 14.sp,
                    //modifier = Modifier.width(80.dp).weight(1f, fill = false)
                )
            }

            // Middle Component (Bidirectional Dashed Arrow)
            BidirectionalArrow(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp),
                color = Color.Gray,
                strokeWidth = 2.dp,
                arrowLength = 20f,
                dashLength = 10f,
                gapLength = 5f
            )

            // Right Component
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    //.background(Color.Red)
                //modifier = Modifier.width(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(devicesConnectionStatus.device2Status.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = devicesConnectionStatus.device2Name,
                    fontSize = 14.sp,
                    //modifier = Modifier.width(80.dp).weight(1f, fill = false)
                )
            }



        }
        if (!isRemote) {
            LinearDeterminateIndicator(toPercentage(rssiValue, isRaspberry))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MachineryConnectionStatusPreview() {
    SSGAppTheme {
        MachineryConnectionStatus(
            devicesConnectionStatus = DevicesConnectionStatus(
                device1Name = "Smartphone",
                device2Name = "Trattorino",
                device1Status = ConnectionStatus.Online,
                device2Status = ConnectionStatus.Offline
            ),
            isRemote = false,
            rssiValue = -30,
            isRaspberry = false,
            modifier = Modifier
        )
    }
}




@Composable
fun ProgressBar(progress: Int) {
    // Limita il progresso tra 0 e 100
    val clampedProgress = progress.coerceIn(0, 100)

    // Calcola la larghezza animata in base al progresso
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress / 100f,
        animationSpec = tween(durationMillis = 1000) // Usare la stessa durata
    )

    // Calcola il colore in base al progresso (da verde a blu a rosso)
    val animatedColor by animateColorAsState(
        targetValue = lerpColor(clampedProgress),
        animationSpec = tween(durationMillis = 1000) // Usare la stessa durata
    )

    // Rappresenta la barra
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = animatedProgress)
                .background(animatedColor)
        )
    }
}

@Composable
fun lerpColor(progress: Int): Color {
    return when {
        progress <= 50 -> {
            // Interpola da verde a blu
            val fraction = progress / 50f
            lerp(Color.Green, Color.Blue, fraction)
        }
        else -> {
            // Interpola da blu a rosso
            val fraction = (progress - 50) / 50f
            lerp(Color.Blue, Color.Red, fraction)
        }
    }
}

fun lerp(start: Color, end: Color, fraction: Float): Color {
    val r = (start.red + fraction * (end.red - start.red)).coerceIn(0f, 1f)
    val g = (start.green + fraction * (end.green - start.green)).coerceIn(0f, 1f)
    val b = (start.blue + fraction * (end.blue - start.blue)).coerceIn(0f, 1f)
    return Color(r, g, b)
}




@Preview(showBackground = true)
@Composable
fun RssiIndicatorPreview() {
    SSGAppTheme {

        var progress by remember { mutableStateOf(0f) }

        // Esempio di come aggiornare il progresso
        LaunchedEffect(Unit) {
            while (progress < 0.99f) {
                progress += 0.01f
                delay(50) // Simula un aggiornamento del progresso
            }
        }

        LinearDeterminateIndicator(progress)
    }
}


@Composable
fun LinearDeterminateIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        //verticalArrangement = Arrangement.spacedBy(12.dp),
        //horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        CustomLinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp) // Aumenta l'altezza della barra
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.weight(0.8f))
            Box(
                modifier = Modifier
                    .background(Color(0x33000000))
                    .size(width = 2.dp, height = 8.dp)
            )
            Spacer(modifier = Modifier.weight(0.2f))
        }

    }
}

@Composable
fun CustomLinearProgressIndicator(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
    )

    val color by animateColorAsState(
        targetValue = lerpColor(progress),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
    )

    Box(
        modifier = modifier
            .background(Color.LightGray, RoundedCornerShape(0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(color, RoundedCornerShape(0))
        )
    }
}

@Composable
fun lerpColor(progress: Float): Color {
    return Color(
        red = 1f - progress,
        green = 1f - progress,
        blue = 1f - progress
    )
}

@Preview(showBackground = true)
@Composable
fun LinearDeterminateIndicatorPreview() {
    var progress by remember { mutableStateOf(0.3f) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        LinearDeterminateIndicator(progress = progress)

        Button(onClick = {
            progress = if (progress < 1f) progress + 0.1f else 0f
        }) {
            Text("Increment Progress")
        }
    }
}

/** Iterate the progress value */
suspend fun loadProgress(updateProgress: (Float) -> Unit) {
    for (i in 1..100) {
        updateProgress(i.toFloat() / 100)
        delay(100)
    }
}

fun toPercentage(rssiValue: Int, isRaspberry: Boolean): Float {

    if (isRaspberry) {
        // Limita il valore RSSI al range tipico
        val limitedRssi = rssiValue.coerceIn(-30, 0)

        val ciao = -limitedRssi - 0f
        return ciao / 30f // 100 - 0


        //return (limitedRssi + 100f) / 50f
    }

    // Limita il valore RSSI al range tipico
    val limitedRssi = rssiValue.coerceIn(-100, 0)

    val ciao = -limitedRssi - 0f
    return ciao / 100f // 100 - 0


    //return (limitedRssi + 100f) / 50f

}

