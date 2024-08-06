package com.example.ssgapp.operator.presentation.machinery_dashboard_screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.ssgapp.common.presentation.util.components.SuggestionCard
import com.example.ssgapp.ui.theme.SSGAppTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ssgapp.common.LocalStorage
import com.example.ssgapp.operator.domain.model.CommunicationMessage
import com.example.ssgapp.operator.domain.model.CommunicationMessagePriority
import com.example.ssgapp.operator.domain.model.CommunicationMessageType
import com.example.ssgapp.operator.presentation.machinery_dashboard_screen.components.CommunicationCard
import com.example.ssgapp.operator.presentation.machinery_dashboard_screen.components.MachineryConnectionStatus

@SuppressLint("MissingPermission")
@Composable
fun MachineryDashboardScreen(
    machineryId: String,
    machineryMacAddress: String,
    isRemote: Boolean,
    machineryDashboardViewModel: MachineryDashboardViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by machineryDashboardViewModel.uiState.collectAsState()
    val secondUiState by machineryDashboardViewModel.secondUiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {// Viene lanciato solo una volta: quando il composable viene eseguito


        val workerId = LocalStorage(context = context).getValue("WORKER_ID")
        if (workerId == null) { // Se per qualche stranissimo motivo non riesce ad accedere al workerId
            LocalStorage(context).removeValue("WORKER_ID")
            LocalStorage(context).removeValue("JWT")
        } else {
            // Comunico al viewModel l'indirizzo MAC del macchinario
            // non credo serva piu perche direttamente avvaloro l'oggetto active device nel viewmodelmachineryDashboardViewModel.updateMacAddress(machineryMacAddress)

            // Effettuo connessione BLE con il macchinario (e ottengo i suoi servizi)
            machineryDashboardViewModel.setMachineryId(machineryId)
            machineryDashboardViewModel.setActiveDevice(machineryMacAddress)
            machineryDashboardViewModel.setWorkerId(workerId)
            machineryDashboardViewModel.setIsRemote(isRemote)
            machineryDashboardViewModel.connectActiveDevice()
        }

    }

    BackHandler {
        // Non fare nulla quando preme il tasto indietro di sistema
    }


    /*LazyColumn(Modifier.height(150.dp)) {
        items(discoveredCharacteristics.keys.sorted()) { serviceUuid ->
            Text(text = serviceUuid, fontWeight = FontWeight.Black)
            Column(modifier = Modifier.padding(start = 10.dp)) {
                discoveredCharacteristics[serviceUuid]?.forEach {
                    Text(it)
                }
            }
        }
    }*/

    Box {
        MachineryDashboardContent(
            uiState = uiState,
            secondUiState = secondUiState,
            meanRssiValue = machineryDashboardViewModel.meanRssiValue(),
            onDisconnect = {
                machineryDashboardViewModel.disconnectActiveDevice()
                navController.popBackStack()
            },
            readRaspCharacteristics = { machineryDashboardViewModel.readRaspCharacteristics() }
        )
/*
        Button(
            onClick = { machineryDashboardViewModel.receiveCommunicationMessage() },
            modifier = Modifier
                .alpha(0.5f)
        ) {
            Text("Receive Notification")
        }
        Text(machineryMacAddress)
        Text(machineryId)*/
    }
}


@Composable
fun MachineryDashboardContent(
    uiState: MachineryDashboardViewState,
    secondUiState: MachineryDashboardSecondViewState,
    meanRssiValue: Int,
    onDisconnect: () -> Unit,
    readRaspCharacteristics: () -> Unit,
) {
    //val items = remember { listOf("Message 1", "Message 2", "Message 3", "Card 4", "Card 3", "Card 4", "Card 3", "Card 4", "Card 3", "Card 4", "Card 3", "Card 4") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        /*Button(onClick = { readRaspCharacteristics() }) {
            
        }*/
       /* if(uiState.isDeviceConnected) {
            for (service in uiState.discoveredServices) {
                // Stampo lista servizi con le relative caratteristiche associate.
                // servizio === [array di caratteristiche]
                Log.d("GATT", "${service.key} === ${service.value}")
                Text(
                    "${service.key} === ${service.value}",
                    fontSize = 5.sp//12.sp
                )
            }
        }*/
        Text(
            text = "Dashboard",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        MachineryConnectionStatus(
            devicesConnectionStatus = uiState.devicesConnectionStatus,
            isRemote = uiState.isRemote,
            rssiValue = meanRssiValue,
            isRaspberry = uiState.activeDevice?.address == "E4:5F:01:5F:5B:3F",
            modifier = Modifier
                .fillMaxWidth()
        )


        SuggestionCard(
            uiState.suggestionText,
            modifier = Modifier.padding(vertical = 16.dp)
        )


        Text(
            text = "Recent communications",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (secondUiState.communicationMessages.count() == 0) {
            Text(
                text = "All clear",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
        ) {
            items(secondUiState.communicationMessages.reversed()) { message ->
                CommunicationCard(
                    CommunicationMessage(
                        title = message.title,
                        message = message.message,
                        time = message.time,
                        type = message.type,
                        priority = message.priority
                    ),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* Azione per l'icona */ }) {
                Icon(Icons.Default.Email, contentDescription = "Icona")
            }
            Button(
                onClick = { onDisconnect() },

                ) {
                Text("Disconnect")
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun MachineryDashboardScreenPreview() {
    val navController = rememberNavController()
    SSGAppTheme {
        MachineryDashboardContent(
            uiState = MachineryDashboardViewState(
                communicationMessages = listOf(
                    CommunicationMessage(
                        title = "Title",
                        message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
                        time = "12:00 PM",
                        type = CommunicationMessageType.DistanceAlarm,
                        priority = CommunicationMessagePriority.Warning
                    ),
                    CommunicationMessage(
                        title = "Title",
                        message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
                        time = "12:00 PM",
                        type = CommunicationMessageType.DistanceAlarm,
                        priority = CommunicationMessagePriority.Warning
                    )

                )
            ),
            secondUiState = MachineryDashboardSecondViewState(
                communicationMessages = listOf(
                    CommunicationMessage(
                        title = "Title",
                        message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
                        time = "12:00 PM",
                        type = CommunicationMessageType.DistanceAlarm,
                        priority = CommunicationMessagePriority.Warning
                    ),
                    CommunicationMessage(
                        title = "Title",
                        message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
                        time = "12:00 PM",
                        type = CommunicationMessageType.DistanceAlarm,
                        priority = CommunicationMessagePriority.Warning
                    )

                )
            ),
            meanRssiValue = 45,
            onDisconnect = {},
            readRaspCharacteristics = {}
        )
    }
}



