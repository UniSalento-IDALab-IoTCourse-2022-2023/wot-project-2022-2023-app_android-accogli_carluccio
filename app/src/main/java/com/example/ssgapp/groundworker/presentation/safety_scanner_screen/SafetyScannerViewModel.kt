package com.example.ssgapp.groundworker.presentation.safety_scanner_screen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.ssgapp.MainActivity
import com.example.ssgapp.MyApplication
import com.example.ssgapp.R
import com.example.ssgapp.common.LocalStorage
import com.example.ssgapp.common.MQTTManager
import com.example.ssgapp.common.domain.model.GroupedBeacon
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.groundworker.domain.model.MessageAlert
import com.example.ssgapp.groundworker.domain.model.MessageAlertPriority
import com.example.ssgapp.groundworker.domain.model.MessageAlertType
import com.example.ssgapp.groundworker.domain.model.Technology
import com.example.ssgapp.groundworker.domain.repository.ActiveMachineryRepository
import com.example.ssgapp.operator.presentation.util.sendEvent
import com.example.ssgapp.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.service.ArmaRssiFilter
import org.altbeacon.beacon.service.RunningAverageRssiFilter
import javax.inject.Inject


enum class ScanningStatus(val color: Color) {
    SCANNING(Color(0xFFA1F2A0)),
    ALARM(Color(0xFFF2A0A0)),
    PAUSE(Color(0xFFE5E5E5)),

    FETCHING_MACHINERIES(Color(0xFFA9D7DD)),
    FETCHING_MACHINERIES_ERROR(Color(0xFFE5E5E5))
}
// Sviluppi futuri: se dopo tanto tempo ci sono sempre gli stessi beacons in allarme, attivare la rimozione automatica per evitare che
// l'allarme rimanga troppo tempo. Un tempo ragionevole potrebbe essere quello di impostare un timeout a 15 min.
data class SafetyScannerUiState(

    val scanningStatus: ScanningStatus = ScanningStatus.FETCHING_MACHINERIES,
    val scanButtonText: String = "Start scanning",
    val beaconCountText: String = "No beacons detected",
    val suggestionText: AnnotatedString = buildAnnotatedString {
        append("Keep the application open, or in the background.\n")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("Do not close the application while working!")
        }
    },

    val beacons: MutableState<List<Beacon>> = mutableStateOf(emptyList()),
    //val dangerousBeaconsMac: MutableState<List<String>> = mutableStateOf(listOf("D8:F1:CA:B2:7A:04")),
    val activeMachineryList: List<Machinery> = emptyList(), // lista con macchinari contenente l'id del macchinario anche per ciascun suo beacon
    val dangerousBeaconsInAlarm: List<com.example.ssgapp.common.domain.model.Beacon> = emptyList(),
    //val dangerousMachineriesInAlarm: List<Machinery> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SafetyScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activeMachineryRepository: ActiveMachineryRepository, //<- Interfaccia! e non implementazione!
    myApplication: MyApplication
): AndroidViewModel(myApplication) {
    private var mqttManager: MQTTManager

    var region = Region("acfd065e-c3c0-11e3-9bbe-1a514932ac01", null, null, null)


    private val _uiState = MutableStateFlow(SafetyScannerUiState())
    val uiState: StateFlow<SafetyScannerUiState> = _uiState.asStateFlow()

    //private val _myApplication = myApplication


    init {
        mqttManager = MQTTManager(context)

        viewModelScope.launch {
            while (true) {
                // Chiamare la tua funzione qui
                backgroundAlarmLauncher()
                // Sospendere l'esecuzione per 1 secondo
                delay(1000L)
            }
        }

        val beaconManager = BeaconManager.getInstanceForApplication(context)
        BeaconManager.setDebug(true)

        // Imposto l'averaging a 5 secondi (se non eseguo queste due linee, viene considerato il default, che è di 20s, troppo lento!)
        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter::class.java)
        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000L)
        //BeaconManager.setRssiFilterImplClass(ArmaRssiFilter::class.java) // ARMA Filter
        // https://altbeacon.github.io/android-beacon-library/distance_vs_time.html for reference


        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:0-1=4c00,i:2-24v,p:24-24"));


        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon like Eddystone or iBeacon, you must specify the byte layout
        // for that beacon's advertisement with a line like below.
        //
        // If you don't care about AltBeacon, you can clear it from the defaults:
        //beaconManager.getBeaconParsers().clear()

        // Uncomment if you want to block the library from updating its distance model database
        //BeaconManager.setDistanceModelUpdateUrl("")

        // The example shows how to find iBeacon.
        val parser = BeaconParser().
        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.getBeaconParsers().add(parser)

        // enabling debugging will send lots of verbose debug information from the library to Logcat
        // this is useful for troubleshooting problmes
        // BeaconManager.setDebug(true)


        // The BluetoothMedic code here, if included, will watch for problems with the bluetooth
        // stack and optionally:
        // - power cycle bluetooth to recover on bluetooth problems
        // - periodically do a proactive scan or transmission to verify the bluetooth stack is OK
        // BluetoothMedic.getInstance().legacyEnablePowerCycleOnFailures(this) // Android 4-12 only
        // BluetoothMedic.getInstance().enablePeriodicTests(this, BluetoothMedic.SCAN_TEST + BluetoothMedic.TRANSMIT_TEST)

//TODO: ho dovuto commentare questo altrimenti crashava all'avvio        setupBeaconScanning()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupBeaconScanning() {
        val beaconManager = BeaconManager.getInstanceForApplication(context)

        // By default, the library will scan in the background every 5 minutes on Android 4-7,
        // which will be limited to scan jobs scheduled every ~15 minutes on Android 8+
        // If you want more frequent scanning (requires a foreground service on Android 8+),
        // configure that here.
        // If you want to continuously range beacons in the background more often than every 15 mintues,
        // you can use the library's built-in foreground service to unlock this behavior on Android
        // 8+.   the method below shows how you set that up.
        try {
            setupForegroundService()
        }
        catch (e: SecurityException) {
            // On Android TIRAMUSU + this security exception will happen
            // if location permission has not been granted when we start
            // a foreground service.  In this case, wait to set this up
            // until after that permission is granted
            Log.d(TAG, "Not setting up foreground service scanning until location permission granted by user")
            return
        }
        //beaconManager.setEnableScheduledScanJobs(false);//-
        // Imposto ogni quanto tempo effettuare la scansione in background (altrimenti ci impiega tanto tra una scansione e l'altra)
        // ho impostato il minimo possibile (0) di pausa tra una scansione e l'altra per garantire una scansione rapida
        // (1s circa tra una scansione e l'altra). Se in futuro sarà necessario andare a modificare il periodo di scansione per tener
        // testa ai calcoli di distanza e comunicazioni, aumentare questo numero per evitare appunto colli di bottiglia.
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
        // Riguardo all'impostazione della scansione in foreground non c'è bisogno di impostare nulla perché è gia rapida di suo
        // e le massime prestazioni che si possono ottenere sono di 1s circa. Se si vuole modificare il timing, è sufficiente modificare
        // le successive due linee di codice dove si imposta rispettivamente il tempo che intercorre tra una scansione e l'altra
        // ed il tempo nel quale si effettua la scansione (entrambi in millisecondi).
        //beaconManager.foregroundBetweenScanPeriod = 2200
        //beaconManager.foregroundScanPeriod = 11000

        // Ranging callbacks will drop out if no beacons are detected
        // Monitoring callbacks will be delayed by up to 25 minutes on region exit
//        beaconManager.setIntentScanningStrategyEnabled(true)//-

        // Commentato perché non voglio che scansioni all'avvio! Inoltre non ho nemmeno bisogno del monitoring perché mi serve solo il ranging. The code below will start "monitoring" for beacons matching the region definition at the top of this file
        //beaconManager.startMonitoring(region) // commentato perche voglio che non parta la scansione appena l'app si avvia TODO che ho risolto spostando nell'oncreate quello che stava nell'onresume: quando questa riga non la commento e metto in background l'app mentre scansiona, e la riapro, allora non crasha. altrimenti crasha non so perche
        //beaconManager.startRangingBeacons(region)
        // These two lines set up a Live Data observer so this Activity can get beacon data from the Application class
        val regionViewModel = BeaconManager.getInstanceForApplication(context).getRegionViewModel(region)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observeForever( centralMonitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observeForever( centralRangingObserver)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupForegroundService() {
        val builder = Notification.Builder(context, "BeaconReferenceApp")
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
        builder.setContentTitle("Scanning for Beacons")
        // Commentato perche devo capire bene questa cosa, altrimenti quando tappo la notifica cerca di avviare una nuova attività e crasha
        //val intent = Intent(this, MainActivity::class.java)
        //val pendingIntent = PendingIntent.getActivity(
        //    this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        //)
        //builder.setContentIntent(pendingIntent);
        val channel =  NotificationChannel("beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setDescription("My Notification Channel Description")
//        val notificationManager =  getSystemService(
//            Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationManager =  context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channel.getId());
        Log.d(TAG, "Calling enableForegroundServiceScanning")
        BeaconManager.getInstanceForApplication(context).enableForegroundServiceScanning(builder.build(), 456);
        Log.d(TAG, "Back from  enableForegroundServiceScanning")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    val centralMonitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            Log.d(TAG, "outside beacon region: "+region)
        }
        else {
            Log.d(TAG, "inside beacon region: "+region)
            //sendNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        val rangeAgeMillis = System.currentTimeMillis() - (beacons.firstOrNull()?.lastCycleDetectionTimestamp ?: 0)
        if (rangeAgeMillis < 1000) {
            Log.d(MainActivity.TAG, "Ranged: ${beacons.count()} beacons")
            checkIncomingBeacons(beacons)
            Log.d("LLL", "${uiState.value.activeMachineryList.size}")
        }
        else {
            Log.d(MainActivity.TAG, "Ignoring stale ranged beacons from $rangeAgeMillis millis ago")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(machineriesNumber: Int) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "777"
        val channelName = "High priority notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("DISTANCE ALARM")
            .setContentText("You are close to ${if (machineriesNumber == 1) {"one machinery"} else {"${machineriesNumber} machineries"}}!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
        //.setSound(getCustomSoundUri())

        notificationManager.notify(1, notificationBuilder.build())



        /*
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "beacon-ref-notification-id"
        val channelName = "My Notification Name"
        val channelDescription = "My Notification Channel Description"
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Beacon nearby $distance")
            .setContentText("A beacon is nearby.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Priorità alta per la notifica urgente
            .build()

        notificationManager.notify(1, notification)*/
    }

    companion object {
        val TAG = "BeaconReference"
    }

    fun checkIncomingBeacons(beacons: Collection<Beacon>) {
        Log.d("LLL","*${uiState.value.scanningStatus}")

        val dangerousBeaconList = (uiState.value.activeMachineryList.flatMap{ it.beacons!! })

        // usata per mandare tramite MQTT la risoluzione dell'allarme
        var dangerousBeaconsWithAlarmFinished: List<com.example.ssgapp.common.domain.model.Beacon> = emptyList() // contiene tutti i beacons per i quali l'utente è distante per la prima volta dalla loro zona di pericolo
        var dangerousBeaconsWithAlarmStarted: List<com.example.ssgapp.common.domain.model.Beacon> = emptyList() // contiene tutti i beacons per i quali l'utente è per la prima volta dentro la loro zona di pericolo

        for (beacon: Beacon in beacons) {
            Log.d("CALLBACK-BEACON", "starting callback ${beacon}")

            Log.d(TAG, "$beacon about ${beacon.distance} meters away")

            // Check beacons di interesse
            val dangerousBeacon = dangerousBeaconList.find { it.macAddress.equals(beacon.bluetoothAddress, ignoreCase = true) }
            if (dangerousBeacon == null) continue



            /*
                Faccio che se si trova in allarme, quel beacon viene aggiunto in una lista (dangerousBeaconsInAlarm),
                e viene rimosso solo quando viene riscansionato, ma con una distanza maggiore
                della soglia + cuscinetto.
                Questa cosa la faccio perche alcune volte il telefono non vede per brevi momenti
                i beacons, e quindi se non implementassi un meccanismo del genere, finirei per
                fermare l'allarme non appena il telefono non vede quel certo beacon - cosa che
                non va bene perche in questo contesto voglio garantire il fatto che il beacon
                sia effettivamente lontano per fermare l'allarme!
            */
            if (uiState.value.dangerousBeaconsInAlarm.any{ it.macAddress.equals(dangerousBeacon.macAddress) }) { // Se questo beacon è in allarme
                // Allora ragiona mediante zona cuscinetto
                if (beacon.distance < dangerousBeacon.safetyDistance + 0.1 * dangerousBeacon.safetyDistance) {
                    // Continuo a lanciare l'allarme. Non rimuovo il beacon dalla lista
                } else {
                    // aggiungo in lista di aggiornamento per MQTT questo beacon
                    dangerousBeaconsWithAlarmFinished += dangerousBeacon

                }

            } else {
                // Altrimenti ragiona senza zona cuscinetto
                if (beacon.distance < dangerousBeacon.safetyDistance) {
//                    // Aggiungo il beacon in lista in modo da lanciare l'allarme
//                    val newDangerousBeaconsInAlarm = uiState.value.dangerousBeaconsInAlarm + dangerousBeacon
//                    _uiState.update { it.copy(
//                        dangerousBeaconsInAlarm = newDangerousBeaconsInAlarm
//                    ) }

                    // aggiungo in lista di aggiornamento per MQTT questo beacon
                    dangerousBeaconsWithAlarmStarted += dangerousBeacon


                    //Log.d("LLL", "Invio notifica ${beacon.distance} vs(<) ${dangerousBeacon.safetyDistance}")
                    //sendNotification(beacon.distance)

                } else {
                    Log.d("LLL", "NON Invio notifica")
                }
            }
        }

        // - Invio comunicazioni MQTT per segnalare l'avvenuta risoluzione degli allarmi

        // Mi genero un oggetto di tipo GroupedBeacon per lavorare meglio
        // Raggruppa gli oggetti Beacon per machineryId
        val groupedMap: Map<String, List<com.example.ssgapp.common.domain.model.Beacon>> = uiState.value.dangerousBeaconsInAlarm.groupBy { it.machineryId!! }
        // Trasforma la mappa in una lista di GroupedBeacon
        var groupedBeaconsList: List<GroupedBeacon> = groupedMap.map { (machineryId, beacon) ->
            GroupedBeacon(machineryId, beacon.toMutableList())
        }


        // Nota: nel caso in cui ci siano piu beacons in allarme, e questi siano appartenenti allo stesso macchinario, l'allarme viene mandato in maniera intelligente una sola volta.


        for (dangerousBeacon in dangerousBeaconsWithAlarmStarted) {

            //val beaconAlreadyInList = uiState.value.dangerousBeaconsInAlarm.any { it.macAddress == dangerousBeacon.macAddress }
            val machineryAlreadyInAlarm = groupedBeaconsList.any {
                it.machineryId == dangerousBeacon.machineryId && it.beacons.size > 0
            }

            // se il beacon è gia presente in lista, allora non fare niente e non lanciare allarme <- questo non si verifica mai in questo for, visto che dangerousBeaconsWithAlarmStarted riguarda solo quelli che sono entrati in allarme in questo istante
            // se il beacon non è presente in lista, ma il relativo macchinario era gia in allarme, allora aggiungilo in lista, ma non lanciare allarme
            // se il beacon non è presente in lista e il macchinario non è in allarme, allora aggiungilo in lista e lancia l'allarme
            //if (!beaconAlreadyInList) {

            // l'aggiunta in lista non la faccio, essendo che viene gia fatta nello step precedente dove controllo zona cuscinetto o no
            /*
            _uiState.update {
                it.copy(
                    dangerousBeaconsInAlarm = uiState.value.dangerousBeaconsInAlarm + dangerousBeacon
                )
            }*/

            if (!machineryAlreadyInAlarm) {
                // se il macchinario non era gia in allarme
                // Aggiungo il beacon in lista in modo da lanciare l'allarme
                val newDangerousBeaconsInAlarm = uiState.value.dangerousBeaconsInAlarm + dangerousBeacon
                _uiState.update { it.copy(
                        dangerousBeaconsInAlarm = newDangerousBeaconsInAlarm
                    )
                }
                // Mi genero un oggetto di tipo GroupedBeacon per lavorare meglio
                // Raggruppa gli oggetti Beacon per machineryId
                val groupedMap: Map<String, List<com.example.ssgapp.common.domain.model.Beacon>> = uiState.value.dangerousBeaconsInAlarm.groupBy { it.machineryId!! }
                // Trasforma la mappa in una lista di GroupedBeacon
                groupedBeaconsList = groupedMap.map { (machineryId, beacon) ->
                    GroupedBeacon(machineryId, beacon.toMutableList())
                }

                //groupedBeaconsList += GroupedBeacon(dangerousBeacon.machineryId!!, mutableListOf(dangerousBeacon))
                Log.d("MQTT-ISSUE", "Mando messaggio:\n${groupedBeaconsList}\n${dangerousBeaconsWithAlarmStarted}\n")
                // Comunico tramite MQTT l'allarme di ingresso nella zona di pericolo
                sendMQTTAlarm(dangerousBeacon.machineryId!!)
            } else {
                // se il macchinario era gia in allarme
                // Aggiungo il beacon in lista ma non lancio l'allarme
                val newDangerousBeaconsInAlarm = uiState.value.dangerousBeaconsInAlarm + dangerousBeacon
                _uiState.update { it.copy(
                    dangerousBeaconsInAlarm = newDangerousBeaconsInAlarm
                )
                }

                val groupedBeacons = groupedBeaconsList.find { it.machineryId == dangerousBeacon.machineryId }!! // son sicuro per mezzo della condizione machineryAlreadyInAlarm che è vera solo quando è presente almeno un elemento in questa find
                groupedBeacons.beacons += dangerousBeacon
            }
            //}
        }

        // Nota: nel caso in cui ci sia un beacon con allarme di risoluzione, e il relativo macchinario non è presente in lista di macchinari in allarme, la comunicazione intelligentemente non viene mandata dal momento che è gia stata inviata precedentemente
        for (dangerousBeacon in dangerousBeaconsWithAlarmFinished) {
            val beaconAlreadyInList = uiState.value.dangerousBeaconsInAlarm.any { it.macAddress == dangerousBeacon.macAddress }
            // con riferimento alla lista groupedBeaconsList o equivalentemente alla dangerousBeaconsInAlarm
            // se il beacon non è presente, allora ignora, ci saraà stato qualche errore strano
            // se il beacon è unico, rimuovilo dalla lista e invia allarme di risoluzione
            // se il beacon non è unico, allora rimuovilo dalla lista, ma non mandare allarme di risoluzione


            // check beacon non presente (se il beacon non è presente, allora ignora, ci saraà stato qualche errore strano)
            if (!beaconAlreadyInList) {
                continue
            }

            // rimuovo il beacon dalle liste di allarmi
            Log.d("MQTT-ISSUE", " - ${uiState.value.dangerousBeaconsInAlarm}")
            _uiState.update {
                it.copy(
                    dangerousBeaconsInAlarm = uiState.value.dangerousBeaconsInAlarm - dangerousBeacon
                )
            }
            Log.d("MQTT-ISSUE", " - ${uiState.value.dangerousBeaconsInAlarm}")
            val groupedBeacons = groupedBeaconsList.find { it.machineryId == dangerousBeacon.machineryId }!! // son sicuro per mezzo della condizione beaconAlreadyInList che è vera solo quando è presente almeno un elemento in questa find
            groupedBeacons.beacons.removeIf { it.macAddress == dangerousBeacon.macAddress }
            // nota che a questo punto, se quello era l'unico beacon in allarme, mi rimane in groupedBeacons un GroupedBeacon(macAddress, listavuota). Per essere pignoli si sarebbe dovuta rimuovere questo oggetto dalla lista, ma poiche non ha alcuna influenza sul codice di questo ciclo, e poiche successivamente viene poi reinizializzato, non da fastidio e quindi ho scelto di non fare nulla

            // check che quel beacon sia stato l'ultimo di quel macchinario in allarme
            if (groupedBeacons.beacons.size == 0 ) {
                // Comunico tramite MQTT l'avvenuta uscita dalla zona di pericolo
                sendMQTTAlarmResolution(dangerousBeacon.machineryId!!)
            }

        }

        Log.d("CALLBACK-BEACON", "ending callback beacon")

    }

    // Funzione che ogni secondo viene chiamata per lanciare l'allarme (solo notifica sullo smartphone) qualora sia necessario farlo.
    fun backgroundAlarmLauncher() {

        if (uiState.value.scanningStatus == ScanningStatus.SCANNING || uiState.value.scanningStatus == ScanningStatus.ALARM) {
            if (uiState.value.dangerousBeaconsInAlarm.isNotEmpty()) {
                Log.d("LLL", "Launching alarm. Beacons: ${uiState.value.dangerousBeaconsInAlarm}")
                launchAlarm()

                // Conto macchinari a seconda dei beacons (genero lista di macchinari che possiedono beacons)
                // Mi genero un oggetto di tipo GroupedBeacon per lavorare meglio
                // Raggruppa gli oggetti Beacon per machineryId
                val groupedMap: Map<String, List<com.example.ssgapp.common.domain.model.Beacon>> = uiState.value.dangerousBeaconsInAlarm.groupBy { it.machineryId!! }
                // Trasforma la mappa in una lista di GroupedBeacon
                var groupedBeaconsList: List<GroupedBeacon> = groupedMap.map { (machineryId, beacon) ->
                    GroupedBeacon(machineryId, beacon.toMutableList())
                }

                sendNotification(groupedBeaconsList.size)
            } else {
                Log.d("LLL", "End alarm")
                endAlarm()
            }
        }
    }

    private var rangingButtonTapped: (() -> Unit)? = null

    fun setRangingButtonTapped(rangingButtonTapped: () -> Unit) {
        this.rangingButtonTapped = rangingButtonTapped
    }

    fun performRangingButtonTapped() {
        Log.d("DEBUG", "attivo ranging1")
        rangingButtonTapped?.invoke()
        Log.d("DEBUG", "attivo ranging2")
    }

    //private var _beacons: List<Beacon> by mutableStateOf(emptyList())




    fun noBeaconDetected() {
        _uiState.update { currentState ->
            currentState.copy(beacons = mutableStateOf(emptyList()))
        }
    }


    fun startScanning() {
        _uiState.update { currentState ->
            currentState.copy(
                scanningStatus = ScanningStatus.SCANNING,
                scanButtonText = "Stop scanning"
            )
        }
    }

    fun pauseScanning() {
        if (uiState.value.scanningStatus == ScanningStatus.ALARM) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                scanningStatus = ScanningStatus.PAUSE,
                scanButtonText = "Start scanning"
            )
        }
    }

    fun launchAlarm() {
        _uiState.update { currentState ->
            currentState.copy(scanningStatus = ScanningStatus.ALARM)
        }
    }

    fun endAlarm() {
        _uiState.update { currentState ->
            currentState.copy(scanningStatus = ScanningStatus.SCANNING)
        }
    }

    fun newBeaconsDetected(beacons: List<Beacon>) {
        _uiState.update { currentState ->
            //Log.d("DEBUG", "nuovo beacon trovato")
            //Log.d("DEBUG", "beacon ${beacons.toString()}")
            currentState.copy(beacons = mutableStateOf(beacons))
        }
    }

    fun getScanningStatus(): ScanningStatus {
        return uiState.value.scanningStatus
    }



    fun getTodayActiveMachineries() {
        _uiState.update {
            it.copy(
                scanningStatus = ScanningStatus.FETCHING_MACHINERIES
            )
        }

        viewModelScope.launch {
            activeMachineryRepository.getTodayActiveMachineries()
                .onRight { activeMachineryListResponse ->
                    //Log.d("XXX", "Richiesta soddisfatta${activeMachineryListResponse.body().toString()}")

                    val activeMachineryList = activeMachineryListResponse.body()
                    if (activeMachineryList == null) {
                        _uiState.update {
                            it.copy(
                                scanningStatus = ScanningStatus.PAUSE
                                /*machineryScanningState = MachinerySelectionScanningState.AvailableMachineriesListEmpty,
                                machineryList = emptyList()*/
                            )
                        }

                    } else {
                        Log.d("LLL", "(1) - ${activeMachineryList.size}")

                        // Imposto la proprietà machineryId per ciascun beacon di ogni macchinario
                        val updatedActiveMachineryList = activeMachineryList.map { machinery ->
                            val updatedBeacons = machinery.beacons?.map { beacon ->
                                com.example.ssgapp.common.domain.model.Beacon(
                                    id = beacon.id,
                                    position = beacon.position,
                                    macAddress = beacon.macAddress,
                                    safetyDistance = beacon.safetyDistance,
                                    machineryId = machinery.id
                                )
                            }
                            Machinery(
                                id = machinery.id,
                                name = machinery.name,
                                type = machinery.type,
                                serialNumber = machinery.serialNumber,
                                macAddress = machinery.macAddress,
                                beacons = updatedBeacons,
                                isRemote = machinery.isRemote
                            )
                        }


                        _uiState.update {
                            it.copy(
                                scanningStatus = ScanningStatus.PAUSE,
                                activeMachineryList = updatedActiveMachineryList

                                /*machineryScanningState = MachinerySelectionScanningState.BluetoothScanningStarted,
                                machineryList = machineryList*/
                            )
                        }
                        Log.d("LLL", "${updatedActiveMachineryList}")

                        //delay(1000L)
                    }
                }
                .onLeft { error ->
                    _uiState.update {
                        it.copy(
                            scanningStatus = ScanningStatus.FETCHING_MACHINERIES_ERROR
                            /*machineryScanningState = MachinerySelectionScanningState.FetchingError,
                            error = error.error.message*/
                        )
                    }
                    // funzione di estensione implementata in ViewModelExt.kt
                    sendEvent(Event.Toast(error.error.message))
                    error.t?.message?.let { Log.d("Network", it) }
                }
        }

    }

    fun sendMQTTAlarm(machineryId: String) {

        val messageAlert = MessageAlert(
            type = MessageAlertType.DISTANCE,
            technology = Technology.BEACON,
            priority = MessageAlertPriority.WARNING,
            workerId = LocalStorage(context).getValue("WORKER_ID")!!,
            machineryId = machineryId,
            isAlarmStarted = true
        )

        Log.d("MQTT", "Sending MQTT messsage ${messageAlert}")

        mqttManager.sendMQTTAlarm(messageAlert)
    }

    fun sendMQTTAlarmResolution(machineryId: String) {

        val messageAlert = MessageAlert(
            type = MessageAlertType.DISTANCE,
            technology = Technology.BEACON,
            priority = MessageAlertPriority.COMMUNICATION,
            workerId = LocalStorage(context).getValue("WORKER_ID")!!,
            machineryId = machineryId,
            isAlarmStarted = false
        )

        Log.d("MQTT", "Sending MQTT messsage ${messageAlert}")

        mqttManager.sendMQTTAlarm(messageAlert)
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

}