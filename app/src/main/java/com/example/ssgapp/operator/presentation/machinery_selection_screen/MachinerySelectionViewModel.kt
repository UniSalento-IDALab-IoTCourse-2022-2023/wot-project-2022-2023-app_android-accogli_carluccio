package com.example.ssgapp.operator.presentation.machinery_selection_screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssgapp.common.LocalStorage
import com.example.ssgapp.operator.ble.BLEScanner
import com.example.ssgapp.operator.ble.PERMISSION_BLUETOOTH_SCAN
import com.example.ssgapp.operator.ble.BluetoothScanningState
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.operator.domain.repository.MachineryRepository
import com.example.ssgapp.operator.presentation.util.sendEvent
import com.example.ssgapp.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
}
else {
    arrayOf(
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}
fun haveAllPermissions(context: Context) =
    ALL_BLE_PERMISSIONS
        .all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }


/*
@HiltViewModel
class MachinerySelectionViewModel @Inject constructor(
    private val machineryRepository: MachineryRepository //<- Interfaccia! e non implementazione!
): ViewModel() {
Senza BLE avrei scritto così
 */
@HiltViewModel
class MachinerySelectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val machineryRepository: MachineryRepository, //<- Interfaccia! e non implementazione!
    application: Application
): AndroidViewModel(application) {
    private val bleScanner = BLEScanner(application)

    private val _uiState = MutableStateFlow(MachinerySelectionViewState())
    val uiState = _uiState.asStateFlow() // Incapsulation Principle

    private var _isGettingAllowedMachineriesAndScan = false // TODO: è un tappabuchi perche voglio che quando torna alla schermata della scansione, la scansione venga fatta in automatico. ma siccome non c'e un modo per re-inizializzare da capo il viewmodel, uso questa soluzione fatta a mano. Può generare problemi perche non gestisce la concorrenza in maniera inrtelligente (uso un semplice bool)

    init {
        // Se i permessi sono stati concessi, avvia scansione, altrimenti resta nella schermata di richiesta permessi
        if(haveAllPermissions(application.applicationContext)){
            _uiState.update {
                it.copy(
                    hasAllPermissions = true,
                    machineryScanningState = MachinerySelectionScanningState.FetchingAllowedMachineries
                )
            }
            getAllowedMachineriesAndScan()
        }

    }
    /*
        fun getProducts(){
            viewModelScope.launch {
                _state.update {
                    it.copy(isLoading = true)
                }
                /*productsRepository.getProducts()
                    .onRight { products ->
                        _state.update {
                            it.copy(products = products)
                        }
                    }.onLeft {
                        _state.update {
                            it.copy(error = error.error.message)
                        }
                        // funzione di estensione implementata in ViewModelExt.kt
                        sendEvent(Event.Toast(error.error.message))
                    }*/
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }*/

    @SuppressLint("MissingPermission")
    fun getAllowedMachineriesAndScan() {
        _isGettingAllowedMachineriesAndScan = true
        viewModelScope.launch {

            val workerId = LocalStorage(context).getValue("WORKER_ID")!!
            machineryRepository.getAvailableMachineriesFor(workerId)
                .onRight { machineryListResponse ->
                    val machineryList = machineryListResponse.body()
                    if (machineryList == null) {
                        _uiState.update {
                            it.copy(
                                machineryScanningState = MachinerySelectionScanningState.AvailableMachineriesListEmpty,
                                machineryList = emptyList()
                            )
                        }

                    } else {
                        _uiState.update {
                            it.copy(
                                machineryScanningState = MachinerySelectionScanningState.BluetoothScanningStarted,
                                machineryList = machineryList
                            )
                        }
                        //delay(1000L)
                        startScanning()
                    }

                }.onLeft { error ->
                    _uiState.update {
                        it.copy(
                            machineryScanningState = MachinerySelectionScanningState.FetchingError,
                            error = error.error.message
                        )
                    }
                    // funzione di estensione implementata in ViewModelExt.kt
                    sendEvent(Event.Toast(error.error.message))
                    error.t?.message?.let { Log.d("Network", it) }
                }

            /*
            _uiState.update {
                it.copy(machineryList = listOf(
                    Machinery(name = "Carro Plaza", macAddress = "A"),
                    Machinery(name = "Trattore Lamborghini", macAddress = "B"),
                    Machinery(name = "Trattorino Ludopatico", macAddress = "C"),
                    Machinery(name = "Bulldozer Sovietico", macAddress = "D"),
                ))
            }*/
            _isGettingAllowedMachineriesAndScan = false
        }



    }

    fun selectMachinery(selectedMachinery: Machinery) {
        _uiState.update {
            it.copy(selectedMachinery = selectedMachinery)
        }

    }

    fun connectToMachinery(navigateToDashboard: (Machinery) -> Unit) {
        val machinery = uiState.value.selectedMachinery
        _uiState.update {
            it.copy(selectedMachinery = null)
        }
        if(machinery == null) return // per evitare che se per piu volte di fila premo connect, mi crei tante dashboards nel navigation stack


        // Interrompo scansione BLE -> non necessario perche viene gestito da onBackground()
        //stopScanning()


        // Navigo nella Dashboard passandogli il MAC del macchinario con cui voglio fare la connect
        navigateToDashboard(machinery)

    }

    fun onBLEPermissionGranted() {
        _uiState.update {
            it.copy(machineryScanningState = MachinerySelectionScanningState.FetchingAllowedMachineries)
        }

        getAllowedMachineriesAndScan()
    }





    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun startScanning() {

        viewModelScope.launch {
            bleScanner.foundDevices.collect { devices ->
                _uiState.update { it.copy(foundDevices = devices) }
            }
        }
        viewModelScope.launch {

            bleScanner.bluetoothScanningState.collect { scanningState ->
                _uiState.update {
                    it.copy(machineryScanningState = when(scanningState) {
                        BluetoothScanningState.BluetoothScanningOn -> MachinerySelectionScanningState.BluetoothScanningStarted
                        BluetoothScanningState.BluetoothScanningOff -> MachinerySelectionScanningState.BluetoothScanningPaused
                        BluetoothScanningState.BluetoothScanningError -> MachinerySelectionScanningState.BluetoothScanningError
                    }
                ) }
            }

        }

        bleScanner.startScanning() // faccio partire scansione solo dopo aver impostato correttamente tutto

    }
    fun waitForTrue(boolean: MutableStateFlow<Boolean>): Flow<Unit> {
        return flow {
            boolean.collect { value ->
                if (value) {
                    emit(Unit)
                }
            }
        }
    }


    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun stopScanning() {
        bleScanner.stopScanning()
    }


    override fun onCleared() {
        super.onCleared()

        //when the ViewModel dies, shut down the BLE client with it
        if (bleScanner.bluetoothScanningState.value == BluetoothScanningState.BluetoothScanningOn) {
            if (ActivityCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bleScanner.stopScanning()
            }
        }
    }

    fun haveAllPermissions(): Boolean {
        return _uiState.value.hasAllPermissions
    }

    fun onResume() {
        // Se i permessi sono stati concessi, avvia scansione, altrimenti resta nella schermata di richiesta permessi
        if(haveAllPermissions() && !_isGettingAllowedMachineriesAndScan){
            _uiState.update {
                it.copy(machineryScanningState = MachinerySelectionScanningState.FetchingAllowedMachineries)
            }
            Log.d("NAVBACK", "Eseguo scansione da onResume")
            getAllowedMachineriesAndScan()
        }
    }

    fun onBackground() {
        Log.d("Lifecycle", "onBackground()")

        stopScanning()
        _uiState.update {
            it.copy(
                machineryScanningState = MachinerySelectionScanningState.BluetoothScanningPaused
            )
        }
    }

    fun onForeground() {
        Log.d("Lifecycle", "onForeground()")
        if(haveAllPermissions() && !_isGettingAllowedMachineriesAndScan) {
            _uiState.update {
                it.copy(machineryScanningState = MachinerySelectionScanningState.FetchingAllowedMachineries)
            }
            Log.d("NAVBACK", "Eseguo scansione da onResume")
            getAllowedMachineriesAndScan()
        }
    }

    fun logout(
        onLogoutButtonPressed: () -> Unit
    ) {
        // Rimuovo oggetti dallo storage locale
        LocalStorage(context = context).removeValue("JWT")
        LocalStorage(context = context).removeValue("WORKER_ID")

        // Vado nella schermata di login
        onLogoutButtonPressed()
    }

    fun readRaspCharacteristics() {

    }


}