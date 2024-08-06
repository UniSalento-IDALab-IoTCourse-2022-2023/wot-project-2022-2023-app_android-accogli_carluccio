package com.example.ssgapp.operator.presentation.machinery_selection_screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ssgapp.R
import com.example.ssgapp.common.presentation.navigation.NavigationItem
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.ScanningStatus
import com.example.ssgapp.operator.presentation.machinery_selection_screen.components.AvailableMachineriesList
import com.example.ssgapp.operator.presentation.machinery_selection_screen.components.BLEPermissionRequest
import com.example.ssgapp.operator.presentation.machinery_selection_screen.components.FetchingAllowedMachineriesLoader
import com.example.ssgapp.ui.theme.SSGAppTheme

class AppLifecycleObserver(
    private val onPause: () -> Unit,
    private val onResume: () -> Unit
) : DefaultLifecycleObserver {

    override fun onPause(owner: LifecycleOwner) {
        onPause()
    }

    override fun onResume(owner: LifecycleOwner) {
        onResume()
    }
}

/*
* TODO: Descrivere come ho gestito gli stati, quando va in background, in foreground, e quando ritorna dalla dashboard cosa accade - meccanismo tappabuchi per la sincronizzazione
*
* */
@Composable
internal fun MachinerySelectionScreen(
    machinerySelectionViewModel: MachinerySelectionViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by machinerySelectionViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {// Viene lanciato solo una volta: quando il composable viene eseguito

    }

    BackHandler {
        // Ottieni l'attività corrente e spostala in background
        (context as? Activity)?.moveTaskToBack(true)
    }

    val navigateToDashboard = { navController.navigate("${NavigationItem.MachineryDashboardScreen.route}/${uiState.selectedMachinery?.id}/${uiState.selectedMachinery?.macAddress}/${(uiState.selectedMachinery?.isRemote ?: "false")}")}//NavigationItem.MachineryDashboardScreen.route)}

    // QUANDO L'UTENTE RITORNA NELLA SCHERMATA DI SCANSIONE DALLA DASHBOARD
    // Monitor the current back stack entry
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Use LaunchedEffect to trigger an action when this composable is resumed
    LaunchedEffect(navBackStackEntry) {
        // Check if this composable is the topmost item on the back stack
        if (navBackStackEntry?.destination?.route == NavigationItem.MachinerySelectionScreen.route) {
            Log.d("NAVBACK", "Callback chiamata")
            // MachinerySelectionScreen is now in foreground
            machinerySelectionViewModel.onResume()
        }
    }

    // QUANDO L'UTENTE METTE L'APP IN BACKGROUND - interrompo scansione bluetooth
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    // Define the lifecycle observer
    val lifecycleObserver = remember {
        AppLifecycleObserver(
            onPause = {
                // Handle the app going to the background
                machinerySelectionViewModel.onBackground()
            },
            onResume = {
                // Handle the app coming to the foreground
                machinerySelectionViewModel.onForeground()
            }
        )
    }

    // Register the lifecycle observer
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }


    MachinerySelectionContent(
        uiState = uiState,
        onMachinerySelection = { machinerySelectionViewModel.selectMachinery(it) },
        onConnectButtonPressed = {
            machinerySelectionViewModel.connectToMachinery({ navigateToDashboard() })
        },
        onBLEPermissionGranted = { machinerySelectionViewModel.onBLEPermissionGranted() },
        onLogOutButtonPressed = {
            machinerySelectionViewModel.logout(
                onLogoutButtonPressed = { navController.navigate(NavigationItem.Login.route) { popUpTo(NavigationItem.MachinerySelectionScreen.route) { inclusive = true }  }}
            )
        }
    )

}

@Composable
fun MachinerySelectionContent(
    uiState: MachinerySelectionViewState,
    onMachinerySelection: (Machinery) -> Unit,
    onConnectButtonPressed: () -> Unit,
    onBLEPermissionGranted: () -> Unit,
    onLogOutButtonPressed: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            //Spacer(Modifier.width(48.dp))

            Text(
                text = "Select Machinery",
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


        when (uiState.machineryScanningState) {
            MachinerySelectionScanningState.FetchingAllowedMachineries ->
                FetchingAllowedMachineriesLoader()
            MachinerySelectionScanningState.BluetoothScanningStarted ->
                AvailableMachineriesList(
                    uiState = uiState,
                    onMachinerySelection = onMachinerySelection,
                    onConnectButtonPressed = onConnectButtonPressed
                )
            MachinerySelectionScanningState.FetchingError ->
                Text("Fetching error")
            MachinerySelectionScanningState.BluetoothScanningPaused ->
                Text("Scanning Paused")
            MachinerySelectionScanningState.BluetoothScanningError ->
                Text("Scanning Failed")
            MachinerySelectionScanningState.RequestingBLEPermission ->
                BLEPermissionRequest(onBLEPermissionGranted = { onBLEPermissionGranted() })
            MachinerySelectionScanningState.AvailableMachineriesListEmpty ->
                Text("No machinery available for you 🫤")
        }

    }

}



// PREVIEWS

@Preview(showBackground = true)
@Composable
fun MachinerySelection_RequestingBLEPermission_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.RequestingBLEPermission
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachinerySelection_FetchingAllowedMachineries_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.FetchingAllowedMachineries
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachinerySelection_AvailableMachineriesListEmpty_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.AvailableMachineriesListEmpty
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachinerySelection_FetchingError_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.FetchingError
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachinerySelection_BluetoothScanningStarted_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.BluetoothScanningStarted
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachinerySelection_BluetoothScanningPaused_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.BluetoothScanningPaused
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachinerySelection_BluetoothScanningError_Preview() {
    SSGAppTheme {
        MachinerySelectionContent(
            uiState = MachinerySelectionViewState(
                machineryList = listOf(
                    Machinery(
                        id = "A",
                        name = "Carro Plaza 2T",
                        type = "Camion Trasportatore",
                        serialNumber = "AA77AA",
                        macAddress = "AAAA:AAAA:AAAA:AAAA",
                        isRemote = true
                    ),
                    Machinery(
                        id = "B",
                        name = "Trattore Lamborghini",
                        type = "Trattore",
                        serialNumber = "BB77BB",
                        macAddress = "BBBB:BBBB:BBBB:BBBB",
                        isRemote = false
                    ),
                    Machinery(
                        id = "C",
                        name = "Trattorino Ludopatico",
                        type = "Trattore",
                        serialNumber = "CC77CC",
                        macAddress = "CCCC:CCCC:CCCC:CCCC",
                        isRemote = false
                    ),
                    Machinery(
                        id = "D",
                        name = "Bulldozer Sovietico 5T",
                        type = "Bulldozer",
                        serialNumber = "AA77AA",
                        macAddress = "DDDD:DDDD:DDDD:DDDD",
                        isRemote = false
                    ),
                ),
                machineryScanningState = MachinerySelectionScanningState.BluetoothScanningError
            ),
            onMachinerySelection = {},
            onConnectButtonPressed = {},
            onBLEPermissionGranted = {},
            onLogOutButtonPressed = {}
        )
    }
}
