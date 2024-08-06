package com.example.ssgapp.operator.presentation.machinery_dashboard_screen

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssgapp.R
import com.example.ssgapp.common.MQTTManager
import com.example.ssgapp.groundworker.domain.model.MessageAlert
import com.example.ssgapp.groundworker.domain.model.MessageAlertPriority
import com.example.ssgapp.groundworker.domain.model.MessageAlertType
import com.example.ssgapp.groundworker.domain.model.Technology
import com.example.ssgapp.operator.ble.BLEDeviceConnection
import com.example.ssgapp.operator.ble.BLEScanner
import com.example.ssgapp.operator.ble.PERMISSION_BLUETOOTH_CONNECT
import com.example.ssgapp.operator.ble.PERMISSION_BLUETOOTH_SCAN
import com.example.ssgapp.operator.domain.model.CommunicationMessage
import com.example.ssgapp.operator.domain.model.CommunicationMessagePriority
import com.example.ssgapp.operator.domain.model.CommunicationMessageType
import com.example.ssgapp.operator.domain.model.ConnectionStatus
import com.example.ssgapp.operator.domain.model.DevicesConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.pow



@HiltViewModel
class MachineryDashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    //private val productsRepository: ProductsRepository //<- Interfaccia! e non implementazione!
    application: Application
): AndroidViewModel(application) {
    private val _application = application // Tappabuchi. Non so per quale motivo, ma non mi vede 'application' quando vado a usarla dentro una funzione

    // MQTT
    private var mqttManager: MQTTManager

    // Viene avvalorata dal composable una volta che viene istanziato, nel LaunchedEffect(Unit)
    /*private var _macAddress = ""
    var macAddress: String
        get() = _macAddress
        private set(value) {
            _macAddress = value
        }*/

    // BLE CONNECT VARIABLES
    private val bleScanner = BLEScanner(application)

    private var activeConnection = MutableStateFlow<BLEDeviceConnection?>(null)

    private val _isDeviceConnected = activeConnection.flatMapLatest { it?.isConnected ?: flowOf(false) }
    private val _activeDeviceServices = activeConnection.flatMapLatest {
        it?.services ?: flowOf(emptyList())
    }

    // STATO CONNESSIONE - lo devo definire in questo modo perche la classe BLEDeviceConnection è quella che mi dice se sono connessi
    private val _devicesConnectionStatus = activeConnection.flatMapLatest {
        it?.devicesConnectionStatus ?: flowOf(DevicesConnectionStatus(
            device1Name = "Smartphone",
            device2Name = "Machinery",
            device1Status = ConnectionStatus.Online,
            device2Status = ConnectionStatus.Offline
        ))

    }
    private val _rssiValue = activeConnection.flatMapLatest {
        it?.rssiValue ?: flowOf(0)
    }

    // STATE VIEW
    private val _uiState = MutableStateFlow(MachineryDashboardViewState())
    private val _secondUiState = MutableStateFlow(MachineryDashboardSecondViewState())
    //val uiState = _uiState.asStateFlow()

    val uiState = combine(
        _uiState,
        _isDeviceConnected,
        _activeDeviceServices,
        _devicesConnectionStatus,
        _rssiValue,
    ) { _state, _isDeviceConnected, _services, _devicesConnectionStatus, _rssiValue ->
        _state.copy(
            isDeviceConnected = _isDeviceConnected,
            discoveredServices = _services.associate { service -> Pair(service.uuid.toString(), service.characteristics.map { it.uuid.toString() }) },
            devicesConnectionStatus = _devicesConnectionStatus,
            rssiValue = _rssiValue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MachineryDashboardViewState())


    private val _communicationMessages = activeConnection.flatMapLatest {
        it?.communicationMessages ?: flowOf(emptyList())
    }
    val secondUiState = combine(
        _secondUiState,
        _communicationMessages
    ) { _state, _communicationMessages ->
        _state.copy(
            communicationMessages = _communicationMessages
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MachineryDashboardSecondViewState())

    private val _rssiValues = MutableStateFlow<List<Int>>(emptyList())
    val rssiValues = _rssiValues.asStateFlow()

    init {
        mqttManager = MQTTManager(context)

        viewModelScope.launch { observeRssiValue() }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun receiveCommunicationMessage() {
        val incomingMessage = CommunicationMessage(
            title = "Notification Title",
            message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
            time = getCurrentTimeString(),
            type = CommunicationMessageType.DistanceAlarm,
            priority = CommunicationMessagePriority.Warning
        )

        _uiState.update {
            it.copy(communicationMessages = listOf(incomingMessage) + it.communicationMessages)
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentTimeString(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH.mm.ss")
        return currentTime.format(formatter)
    }
/*
    fun updateMacAddress(macAddress: String) {
        this.macAddress = macAddress
    }
*/




    // BLE

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN])
    fun setActiveDevice(deviceMacAddress: String) {
        val bluetoothManager = _application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val device = bluetoothManager.adapter.getRemoteDevice(deviceMacAddress)

        activeConnection.value = device?.run { BLEDeviceConnection(_application, device) }
        _uiState.update { it.copy(activeDevice = device) }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connectActiveDevice() {
        activeConnection.value?.connect()
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun disconnectActiveDevice() {
        activeConnection.value?.disconnect()
    }


    // Per callback per ottenere RSSI da classe BLEDeviceConnection
    private fun observeRssiValue() {
        if (true) { // TODO: questo codice non va eseguito se si guidano macchinari da remoto!
            viewModelScope.launch {


                _rssiValue.collect {
                    // Se sono connesso al macchinario, leggo periodicamente il suo RSSI
                    while (true) {
                        if (rssiValues.value.size != 10) { // per calcolare Media dei 10 valori ottenuti negli ultimi 5 secondi
                            _rssiValues.update {
                                it + uiState.value.rssiValue
                            }
                        } else {
                            _rssiValues.update {
                                it.subList(1,4) + uiState.value.rssiValue
                            }
                        }


                        if (uiState.value.devicesConnectionStatus.device2Status == ConnectionStatus.Online) {
                            if (uiState.value.activeDevice?.address == "E4:5F:01:5F:5B:3F" && meanRssiValue() < -24) {
                                // Lancio allarme Worker Away
                                Log.d("GATT", "Worker away!!!")
                                sendMQTTAlarm()
                                sendNotification()
                            } else if (meanRssiValue() < -80) { // -80 per l'iphone, -24 per il raspberry tramite la formula
                                // Lancio allarme Worker Away
                                Log.d("GATT", "Worker away!!!")
                                sendMQTTAlarm()
                                sendNotification()
                            }
                            activeConnection.value?.readRssiValue()
                        }
                        delay(500)
                    }
                }
                activeConnection.value?.readRssiValue()

            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification() {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "777"
        val channelName = "High priority notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("DISTANCE ALARM")
            .setContentText("You are away from the machinery!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
        //.setSound(getCustomSoundUri())

        notificationManager.notify(1, notificationBuilder.build())

    }

    // Ipotizzando che a distanza di 1 metro l'rssi sia = -65
    fun calculateDistance(rssi: Int, txPowerAtOneMeter: Int = -65, pathLossExponent: Double = 2.0): Double {
        val distance = 10.toDouble().pow((txPowerAtOneMeter - rssi) / (-10 * pathLossExponent))
        return distance
    }





    fun setIsRemote(isRemote: Boolean) {
        _uiState.update {
            it.copy(isRemote = isRemote)
        }
    }

    fun setMachineryId(machineryId: String) {
        _uiState.update {
            it.copy(machineryId = machineryId)
        }
    }

    fun setWorkerId(workerId: String) {
        _uiState.update {
            it.copy(workerId = workerId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMQTTAlarm() {

        val messageAlert = MessageAlert(
            type = MessageAlertType.DRIVER_AWAY,
            technology = Technology.RSSI,
            priority = MessageAlertPriority.WARNING,
            workerId = uiState.value.workerId,
            machineryId = uiState.value.machineryId,
            isAlarmStarted = true
        )

        mqttManager.sendMQTTAlarm(messageAlert)
    }

    /* TODO: non serve perche ho implementato la ricerca delle caratteristiche direttamente nella connect
        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        fun discoverActiveDeviceServices() {
            activeConnection.value?.discoverServices()
        }*/

    fun meanRssiValue(): Int {
        var sum = 0
        for (rssi in rssiValues.value) {
            sum += rssi
        }
        return sum / rssiValues.value.size
    }

    fun readRaspCharacteristics() {
        Log.d("GATT-READ", "Leggo caratteristica")
        activeConnection.value?.readRaspCharacteristics()
    }

}