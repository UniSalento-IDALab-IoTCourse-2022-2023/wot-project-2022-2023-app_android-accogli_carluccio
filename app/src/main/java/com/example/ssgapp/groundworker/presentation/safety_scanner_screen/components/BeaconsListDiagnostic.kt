package com.example.ssgapp.groundworker.presentation.safety_scanner_screen.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.SafetyScannerViewModel
import org.altbeacon.beacon.Beacon

@Composable
fun BeaconsListDiagnostic(
    beacons: List<Beacon>,
) {

    Column() {

        LazyColumn {
            items(beacons.size) { index ->

                Text(
                    text = "MAC: ${beacons[index].bluetoothAddress}\n" +
                            "id1: ${beacons[index].id1}\n" +
                            "id2: ${beacons[index].id2} " +
                            "id3:  rssi: ${beacons[index].rssi}\n" +
                            "est. distance: ${beacons[index].distance} m",
                    fontSize = 8.sp
                )

            }
        }
    }

}