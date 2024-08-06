package com.example.ssgapp.operator.presentation.machinery_dashboard_screen

import android.bluetooth.BluetoothDevice
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.ssgapp.operator.domain.model.CommunicationMessage
import com.example.ssgapp.operator.domain.model.ConnectionStatus
import com.example.ssgapp.operator.domain.model.DevicesConnectionStatus

data class MachineryDashboardViewState(

    val workerId: String = "",
    val machineryId: String = "",
    val isRemote: Boolean = false,

    val devicesConnectionStatus: DevicesConnectionStatus = DevicesConnectionStatus(
        device1Name = "Smartphone",
        device2Name = "Machinery",
        device1Status = ConnectionStatus.Online,
        device2Status = ConnectionStatus.Offline
    ),
    val suggestionText: AnnotatedString = buildAnnotatedString {
        append("Ensure Bluetooth is On\n")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Keep Bluetooth enabled to receive important notifications!")
        }
    },
    /*
    val suggestionText: AnnotatedString = buildAnnotatedString {
        append("Smartphone offline.\n")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Connect to the local network to receive important communications!")
        }
    },
    */
    val communicationMessages: List<CommunicationMessage> = emptyList(), // L'ho dovuta implementare nel secondo viewstate per il limite delle variabili che posso unire nel combine nel viewmodel

    // BLE
    val activeDevice: BluetoothDevice? = null,
    val isDeviceConnected: Boolean = false,
    val discoveredServices: Map<String, List<String>> = emptyMap(),
    val rssiValue: Int = 0,
    val userWantsToKeepConnectionWithMachine: Boolean = true

    /*
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
*/
)

data class MachineryDashboardSecondViewState(

    val communicationMessages: List<CommunicationMessage> = emptyList(),

)