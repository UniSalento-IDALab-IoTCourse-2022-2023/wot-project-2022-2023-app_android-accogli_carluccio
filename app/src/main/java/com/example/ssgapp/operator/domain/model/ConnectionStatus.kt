package com.example.ssgapp.operator.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ssgapp.ui.theme.Green
import com.example.ssgapp.ui.theme.Red

data class DevicesConnectionStatus (
    val device1Name: String,
    val device2Name: String,
    val device1Status: ConnectionStatus,
    val device2Status: ConnectionStatus
)
enum class ConnectionStatus(val text: String, val color: Color) {
    Online("Online", Green),
    Offline("Offline", Red),
}