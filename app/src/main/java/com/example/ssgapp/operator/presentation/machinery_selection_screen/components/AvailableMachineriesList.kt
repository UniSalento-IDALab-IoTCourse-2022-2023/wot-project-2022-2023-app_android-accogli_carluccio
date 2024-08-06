package com.example.ssgapp.operator.presentation.machinery_selection_screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssgapp.common.presentation.util.components.LoadingIndicator
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.operator.presentation.machinery_selection_screen.MachinerySelectionScanningState
import com.example.ssgapp.operator.presentation.machinery_selection_screen.MachinerySelectionViewState
import com.example.ssgapp.ui.theme.SSGAppTheme

@Composable
fun AvailableMachineriesList(
    uiState: MachinerySelectionViewState,
    onMachinerySelection: (Machinery) -> Unit,
    onConnectButtonPressed: () -> Unit
) {
    Column {
        if(uiState.machineryList.size == 0) {
            LoadingIndicator(
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = "Scanning allowed machineries",
                fontSize = 18.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.fillMaxHeight())
        }
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Togliere il commento per vedere i dispositivi bluetooth vicini
            /*items(uiState.foundDevices) { device ->
                DeviceItem(
                    deviceName = device.name,
                    deviceAddress = device.address,
                    deviceRssi = device.rssi,
                    selectDevice = { /*selectDevice(device)*/ }
                )
            }*/

            items(uiState.machineryList) { machinery ->
                MachineryListItem(
                    machinery = machinery,
                    isSelected = machinery.macAddress == uiState.selectedMachinery?.macAddress,
                    onClick = { onMachinerySelection(machinery) }
                )
            }
        }

        Button(
            enabled = uiState.selectedMachinery != null,
            onClick = {
                onConnectButtonPressed()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        ) {
            Text("Connect")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AvailableMachineriesListPreview() {
    SSGAppTheme {
        AvailableMachineriesList(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA"),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB"),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC"),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD"),
                ),
                machineryScanningState = MachinerySelectionScanningState.RequestingBLEPermission
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {}
        )
    }
}
