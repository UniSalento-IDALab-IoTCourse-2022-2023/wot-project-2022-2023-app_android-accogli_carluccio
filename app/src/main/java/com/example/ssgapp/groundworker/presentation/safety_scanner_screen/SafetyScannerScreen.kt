package com.example.ssgapp.groundworker.presentation.safety_scanner_screen


import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.example.ssgapp.R
import com.example.ssgapp.common.domain.model.GroupedBeacon
import com.example.ssgapp.common.presentation.navigation.NavigationItem
import com.example.ssgapp.common.presentation.util.components.SuggestionCard
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.components.BeaconsListDiagnostic
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.components.Radar
import com.example.ssgapp.ui.theme.SSGAppTheme
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SafetyScannerScreen(
    navController: NavController,
    safetyScannerViewModel: SafetyScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    /*BackHandler {
        // Non fare nulla quando preme il tasto indietro di sistema
    }*/
    BackHandler {
        // Ottieni l'attività corrente e spostala in background
        (context as? Activity)?.moveTaskToBack(true)
    }


    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("MainActivity", "Ranged: ${beacons.count()} beacons")
        if (BeaconManager.getInstanceForApplication(context).rangedRegions.size > 0) {
//            safetyScannerViewModel.setBeaconCountText("Ranging enabled: ${beacons.count()} beacon(s) detected")
            val sortedBeacons = beacons.sortedBy { it.distance }
            safetyScannerViewModel.newBeaconsDetected(sortedBeacons)

            /*beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
                beacons
                    .sortedBy { it.distance }
                    .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())*/
        }
    }

    fun rangingButtonTapped() {
        val beaconManager = BeaconManager.getInstanceForApplication(context)
        if (beaconManager.rangedRegions.size == 0) {
            beaconManager.startRangingBeacons(safetyScannerViewModel.region)
        }
        else {
            beaconManager.stopRangingBeacons(safetyScannerViewModel.region)
            safetyScannerViewModel.noBeaconDetected() // beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
    }
    fun startRangingBeacons() {
        val beaconManager = BeaconManager.getInstanceForApplication(context)

        beaconManager.startRangingBeacons(safetyScannerViewModel.region)

    }
    fun stopRangingBeacons() {
        val beaconManager = BeaconManager.getInstanceForApplication(context)

        beaconManager.stopRangingBeacons(safetyScannerViewModel.region)
        safetyScannerViewModel.noBeaconDetected() // beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))

    }

    /*
        fun monitoringButtonTapped() {
            var dialogTitle = ""
            var dialogMessage = ""
            val beaconManager = BeaconManager.getInstanceForApplication(this)
            if (beaconManager.monitoredRegions.size == 0) {
                beaconManager.startMonitoring(beaconReferenceApplication.region)
                dialogTitle = "Beacon monitoring started."
                dialogMessage = "You will see a dialog if a beacon is detected, and another if beacons then stop being detected."
                appViewModel.setMonitoringButtonText("Stop Monitoring")

            }
            else {
                beaconManager.stopMonitoring(beaconReferenceApplication.region)
                dialogTitle = "Beacon monitoring stopped."
                dialogMessage = "You will no longer see dialogs when beacons start/stop being detected."
                appViewModel.setMonitoringButtonText("Start Monitoring")
            }
            val builder =
                AlertDialog.Builder(this)
            builder.setTitle(dialogTitle)
            builder.setMessage(dialogMessage)
            builder.setPositiveButton(android.R.string.ok, null)
            alertDialog?.dismiss()
            alertDialog = builder.create()
            alertDialog?.show()

        }
    */



    val uiState by safetyScannerViewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {// Viene lanciato solo una volta: quando il composable viene eseguito
// Creo canale di notifica
        val channel = NotificationChannel(
            "777",//CHANNEL_ID,
            "High priority notifications",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


        // You MUST make sure the following dynamic permissions are granted by the user to detect beacons
        //
        //    Manifest.permission.BLUETOOTH_SCAN
        //    Manifest.permission.BLUETOOTH_CONNECT
        //    Manifest.permission.ACCESS_FINE_LOCATION
        //    Manifest.permission.ACCESS_BACKGROUND_LOCATION // only needed to detect in background
        //
        // The code needed to get these permissions has become increasingly complex, so it is in
        // its own file so as not to clutter this file focussed on how to use the library.

//        if (!BeaconScanPermissionsActivity.allPermissionsGranted(context,
//                true)) {
//            val intent = Intent(this, BeaconScanPermissionsActivity::class.java)
//            intent.putExtra("backgroundAccessRequested", true)
//            startActivity(intent) // TODO: questo crasha, per cui i permessi momentaneamente li imposto dalle impostazioni dell'app direttamente sul telefono
//        }
//        else {
            // All permissions are granted now.  In the case where we are configured
            // to use a foreground service, we will not have been able to start scanning until
            // after permissions are graned.  So we will do so here.
            if (BeaconManager.getInstanceForApplication(context).monitoredRegions.size == 0) {
                Log.d("LLL", "Tutto ok")
                safetyScannerViewModel.setupBeaconScanning()
            }
//        }



        // Set up a Live Data observer for beacon data
        val regionViewModel = BeaconManager.getInstanceForApplication(context).getRegionViewModel(safetyScannerViewModel.region)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        //regionViewModel.regionState.observe(this, monitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observe(lifecycleOwner, rangingObserver)
        /*rangingButton = findViewById<Button>(R.id.rangingButton)
        monitoringButton = findViewById<Button>(R.id.monitoringButton)
        beaconListView = findViewById<ListView>(R.id.beaconList)
        beaconCountTextView = findViewById<TextView>(R.id.beaconCount)
*/

        // passo la funzione che fa uso del context nel ViewModel
        //rangingButtonTapped()
        //safetyScannerViewModel.setRangingButtonTapped({ rangingButtonTapped() })



        safetyScannerViewModel.noBeaconDetected() //beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))


        safetyScannerViewModel.getTodayActiveMachineries()
    }

    BeaconsListDiagnostic(uiState.beacons.value)

    SafetyScannerContent(
        uiState = uiState,
        onLogOutButtonPressed = {
            safetyScannerViewModel.logout { navController.navigate(NavigationItem.Login.route) { popUpTo(NavigationItem.SafetyScannerScreen.route) { inclusive = true } } }
            //navController.navigateUp()
        },
        onRangingButtonTapped = { rangingButtonTapped() },
        startScanning = { safetyScannerViewModel.startScanning() },
        pauseScanning = { safetyScannerViewModel.pauseScanning() },
        getTodayActiveMachineries = { safetyScannerViewModel.getTodayActiveMachineries() }
    )

}


@Composable
fun SafetyScannerContent(
    uiState: SafetyScannerUiState,
    onLogOutButtonPressed: () -> Unit,
    onRangingButtonTapped: () -> Unit,
    startScanning: () -> Unit,
    pauseScanning: () -> Unit,
    getTodayActiveMachineries: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            Spacer(Modifier.width(48.dp))

            Text(
                text = "Safety scanner",
                style = MaterialTheme.typography.titleLarge,
                //fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )

            IconButton(
                onClick = {
                    // TODO 2: leggere il TODO 1 authViewModel.logout()
                    //stopRangingBeacons()
                    //safetyScannerViewModel.pauseScanning()
                    onLogOutButtonPressed()
                },
                enabled = uiState.scanningStatus == ScanningStatus.PAUSE,
                modifier = Modifier
                    .size(48.dp)
            ) {
                val icon: ImageVector = ImageVector.vectorResource(id = R.drawable.logout)
                Icon(
                    imageVector = icon,
                    contentDescription = "Logout",
                    tint = Color(0xFFEE6666),
                    modifier = Modifier
                        .size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Radar(
            uiState.scanningStatus,
            modifier = Modifier
                .height(240.dp)
        )

        Text(
            text = when (uiState.scanningStatus) {
                ScanningStatus.PAUSE -> "Scanning paused"
                ScanningStatus.SCANNING -> "Scanning nearby machineries..."
                ScanningStatus.ALARM -> {
                    // Raggruppa gli oggetti Beacon per machineryId
                    val groupedMap: Map<String, List<com.example.ssgapp.common.domain.model.Beacon>> =
                        uiState.dangerousBeaconsInAlarm.groupBy { it.machineryId!! }
                    // Trasforma la mappa in una lista di GroupedBeacon
                    var groupedBeaconsList: List<GroupedBeacon> =
                        groupedMap.map { (machineryId, beacon) ->
                            GroupedBeacon(machineryId, beacon.toMutableList())
                        }
                    "You are close ${if (groupedBeaconsList.size == 1) {"one machinery"} else {"${groupedBeaconsList.size} machineries"}}!"
                }
                ScanningStatus.FETCHING_MACHINERIES -> "Updating active machineries"
                ScanningStatus.FETCHING_MACHINERIES_ERROR -> "Error fetching active machineries"
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(top = 16.dp)
        )

        if(uiState.scanningStatus == ScanningStatus.ALARM) {
            Text(
                text = "Move away to stop the alarm.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0x55000000),
                modifier = Modifier.height(32.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }


        SuggestionCard(
            suggestionText = uiState.suggestionText,
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 32.dp)
        )

        Button(
            enabled = uiState.scanningStatus != ScanningStatus.FETCHING_MACHINERIES,
            onClick = {
                if (uiState.scanningStatus == ScanningStatus.PAUSE) {
                    onRangingButtonTapped()
                    startScanning()
                } else if (uiState.scanningStatus == ScanningStatus.SCANNING || uiState.scanningStatus == ScanningStatus.ALARM) {
                    onRangingButtonTapped()
                    pauseScanning()
                } else if (uiState.scanningStatus == ScanningStatus.FETCHING_MACHINERIES_ERROR) {
                    getTodayActiveMachineries()
                }
            },
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp), // Imposta il bordo arrotondato con un raggio di 8dp
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF7F7F7), // Imposta il colore di sfondo del pulsante
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color(0XFF000000))

        ) {
            Text(
                text = if (uiState.scanningStatus == ScanningStatus.PAUSE) {
                    "Start scanning"
                } else {
                    if (uiState.scanningStatus == ScanningStatus.FETCHING_MACHINERIES_ERROR) {
                        "Update"
                    } else {
                        if (uiState.scanningStatus == ScanningStatus.FETCHING_MACHINERIES) {
                            "Start scanning"
                        } else {
                            "Stop scanning"
                        }
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }
    }
}


@Preview
@Composable
fun SafetyScannerScreen_FetchingMachineries_Preview() {
    SSGAppTheme {
        SafetyScannerContent(
            uiState = SafetyScannerUiState(
                scanningStatus = ScanningStatus.FETCHING_MACHINERIES
            ),
            onLogOutButtonPressed = {},
            onRangingButtonTapped = {},
            startScanning = {},
            pauseScanning = {},
            getTodayActiveMachineries = {}
        )

    }
}

@Preview
@Composable
fun SafetyScannerScreen_FetchingMachineriesError_Preview() {
    SSGAppTheme {
        SafetyScannerContent(
            uiState = SafetyScannerUiState(
                scanningStatus = ScanningStatus.FETCHING_MACHINERIES_ERROR
            ),
            onLogOutButtonPressed = {},
            onRangingButtonTapped = {},
            startScanning = {},
            pauseScanning = {},
            getTodayActiveMachineries = {}
        )

    }
}

@Preview
@Composable
fun SafetyScannerScreen_Pause_Preview() {
    SSGAppTheme {
        SafetyScannerContent(
            uiState = SafetyScannerUiState(
                scanningStatus = ScanningStatus.PAUSE
            ),
            onLogOutButtonPressed = {},
            onRangingButtonTapped = {},
            startScanning = {},
            pauseScanning = {},
            getTodayActiveMachineries = {}
        )

    }
}

@Preview
@Composable
fun SafetyScannerScreen_Scanning_Preview() {
    SSGAppTheme {
        SafetyScannerContent(
            uiState = SafetyScannerUiState(
                scanningStatus = ScanningStatus.SCANNING
            ),
            onLogOutButtonPressed = {},
            onRangingButtonTapped = {},
            startScanning = {},
            pauseScanning = {},
            getTodayActiveMachineries = {}
        )

    }
}

@Preview
@Composable
fun SafetyScannerScreen_Alarm_Preview() {
    SSGAppTheme {
        SafetyScannerContent(
            uiState = SafetyScannerUiState(
                scanningStatus = ScanningStatus.ALARM
            ),
            onLogOutButtonPressed = {},
            onRangingButtonTapped = {},
            startScanning = {},
            pauseScanning = {},
            getTodayActiveMachineries = {}
        )

    }
}