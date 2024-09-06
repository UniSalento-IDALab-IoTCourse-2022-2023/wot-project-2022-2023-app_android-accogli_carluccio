package com.example.ssgapp.operator.ble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings.Global
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.ssgapp.R
import com.example.ssgapp.operator.domain.model.CommunicationMessage
import com.example.ssgapp.operator.domain.model.CommunicationMessagePriority
import com.example.ssgapp.operator.domain.model.CommunicationMessageType
import com.example.ssgapp.operator.domain.model.ConnectionStatus
import com.example.ssgapp.operator.domain.model.DevicesConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.BitSet
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit




val MANUFACTURER_NAME_SERVICE_UUID: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")//Device Information//UUID.fromString("12341000-1234-1234-1234-123456789abc")
val MANUFACTURER_NAME_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")//manufacturer name string//UUID.fromString("12341001-1234-1234-1234-123456789abc")
val BASE_ALARM_SERVICE_UUID: UUID = UUID.fromString("12341000-1234-1234-1234-123456789abc")
val BASE_ALARM_CHARACTERISTIC_UUID: UUID = UUID.fromString("12341001-1234-1234-1234-123456789abc")
val BASE_ALARM_TEXT_CHARACTERISTIC_UUID: UUID = UUID.fromString("12341002-1234-1234-1234-123456789abc")



@Suppress("DEPRECATION")
class BLEDeviceConnection @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    private val context: Context,
    private val bluetoothDevice: BluetoothDevice
) {
    val isConnected = MutableStateFlow(false)
    val successfulNameWrites = MutableStateFlow(0)
    val services = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    val devicesConnectionStatus = MutableStateFlow(DevicesConnectionStatus(
        device1Name = "Smartphone",
        device2Name = "Machinery",
        device1Status = ConnectionStatus.Online,
        device2Status = ConnectionStatus.Offline
    ))
    val rssiValue = MutableStateFlow<Int>(0)
    val userWantsToKeepConnectionWithMachine = MutableStateFlow(true) // Per fare in modo che se la connessione droppa improvvisamente, viene ristabilita la connessione

    val communicationMessages = MutableStateFlow<List<CommunicationMessage>>(emptyList())
    val tmpGeneralCommunicationMessage: MutableStateFlow<CommunicationMessage?> = MutableStateFlow(null) // usata giusto per tenere salvato da qualche parte la comunicazione generale prima di leggerne il messaggio. si sarebbe potuta fare in modi migliori, ma questo è il piu rapido

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(communicationMessage: CommunicationMessage) {

        val timeToRepeat = when (communicationMessage.priority) {
            CommunicationMessagePriority.Communication -> 1 // manda solo una notifica
            CommunicationMessagePriority.Warning -> 3 // manda 3 notifiche (una ogni secondo)
            CommunicationMessagePriority.Danger -> 8 // manda 8 notifiche (una ogni secondo)
        }

        GlobalScope.launch (Dispatchers.Main) {
            for (i in 1..timeToRepeat) {
                delay(1000)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "777"
                val channelName = "High priority notifications"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(channel)

                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(communicationMessage.title)
                    .setContentText(communicationMessage.message)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                //.setSound(getCustomSoundUri())

                notificationManager.notify(1, notificationBuilder.build())
            }
        }

    }

    private val callback = object: BluetoothGattCallback() {
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            val value = characteristic.value
            val hexValue = value?.let { bytesToHex(it) } ?: ""

            Log.d("GATT-READ", "Leggo caratteristica (notify)")
            Log.d("GATT-READ", "characteristic:${characteristic}, value:${hexValue}")


            if (hexValue.startsWith("00")) { // distance alarm
                communicationMessages.value = communicationMessages.value + CommunicationMessage.fromBLE(hexValue)
                sendNotification(communicationMessage = CommunicationMessage.fromBLE(hexValue))
            } else {
                // general alarm. devo leggere il testo del messaggio dall'apposita caratteristica BASE_ALARM_TEXT_CHARACTERISTIC_UUID
                tmpGeneralCommunicationMessage.value = CommunicationMessage.fromBLE(hexValue)
                readAlertMessage()
            }


        }
        private fun bytesToHex(bytes: ByteArray): String {
            val hexArray = "0123456789ABCDEF".toCharArray()
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.d("GATT-READ", "Leggo caratteristica (notify)")
            Log.d("GATT-READ", "characteristic:${characteristic}, value:${value}")
        }

        @Deprecated("Deprecated in Java")
        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)

            Log.d("GATT-READ", "descriptor read: ${descriptor.uuid}, " +
                    "${descriptor.characteristic.uuid}, $status, ${descriptor.value}")
            Log.d(
                "GATT-READ", "descriptor read: ${descriptor.uuid}, " +
                        "${descriptor.characteristic.uuid}, $status, ${descriptor.value}"
            )


        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)

            Log.d("GATT-READ", "descriptor read: ${descriptor.uuid}, " +
                    "${descriptor.characteristic.uuid}, $status, ${value}")
        }


        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d("GATT-READ","descriptor write: ${descriptor.uuid}, ${descriptor.characteristic.uuid}, $status")
        }




        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                rssiValue.value = rssi
                Log.d("RSSI", String.format("BluetoothGatt ReadRssi[%d]", rssi))
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val connected = newState == BluetoothGatt.STATE_CONNECTED
            if (connected) {
                //read the list of services
                discoverServices() // ottengo lista servizi
            }
            isConnected.value = connected
            devicesConnectionStatus.value = DevicesConnectionStatus(
                device1Name = devicesConnectionStatus.value.device1Name,
                device2Name = devicesConnectionStatus.value.device2Name,
                device1Status = devicesConnectionStatus.value.device1Status,
                device2Status = if (connected) ConnectionStatus.Online else ConnectionStatus.Offline
            )
            if (!connected && userWantsToKeepConnectionWithMachine.value) { // Se la connessione droppa improvvisamente, ristabilisco la connessione
                Log.d("GATT", "Attempting reconnection")
                connect()
            }

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            services.value = gatt.services

            // stampo i servizi disponibili
            for(service in services.value) {
                //Log.d("GATT", service.uuid.toString())

            }
            subscribeToBaseAlarmCharacteristics(listOf(BASE_ALARM_CHARACTERISTIC_UUID))



            //readRaspCharacteristics() //<- Lettura caratteristica
            // subscribeToBaseAlarmCharacteristics(listOf(BASE_ALARM_CHARACTERISTIC_UUID))
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            //value: ByteArray,
            status: Int
        ) {
            val value = characteristic.value
            val hexValue = value?.let { bytesToHex(it) } ?: ""

            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value
                if (value != null) {
                    // Gestisci il valore
                    //Log.d("GATT-READ", "Value: ${value.joinToString(",")}")
                    Log.d("GATT-READ", "Value: ${hexValue}")

                    val message = decodeHexToUtf8(hexValue)
                    Log.d("GATT-READ", "${message}") // 6F6C6C6548 -> hello   6F616963 -> ciao


                    // invio notifica
                    val communicationMessage = CommunicationMessage(
                        title = tmpGeneralCommunicationMessage.value!!.title,
                        message = message,
                        time = tmpGeneralCommunicationMessage.value!!.time,
                        type = tmpGeneralCommunicationMessage.value!!.type,
                        priority = tmpGeneralCommunicationMessage.value!!.priority
                    )
                    tmpGeneralCommunicationMessage.value = null // resetto visto che non serve piu
                    communicationMessages.value = communicationMessages.value + communicationMessage
                    sendNotification(communicationMessage = communicationMessage)

                } else {
                    Log.e("GATT-READ", "Characteristic value is null")
                }
            } else {
                Log.e("GATT-READ", "Failed to read characteristic, status: $status")
            }






//            Log.d("GATT-READ","Letta caratteristica")
//            Log.d("GATT-READ","Letta caratteristica ${status} ${characteristic.value}")
            super.onCharacteristicRead(gatt, characteristic, status)
            //if (characteristic.uuid == BASE_ALARM_CHARACTERISTIC_UUID) {
                //passwordRead.value = String(characteristic.value)
            //}
            //Log.d("GATT-READ", ">>>>>>> ${characteristic.uuid}=${String(characteristic.value)}")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
//            if (characteristic.uuid == NAME_CHARACTERISTIC_UUID) {
//                successfulNameWrites.update { it + 1 }
//            }
        }
    }

    private var gatt: BluetoothGatt? = null

    fun readRssiValue() {
        if (gatt?.readRemoteRssi() ?: false) {
            Log.d("RSSI", "Rssi reading")
        } else {
            Log.d("RSSI", "Cannot read rssi")
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun disconnect() {
        userWantsToKeepConnectionWithMachine.value = false
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun connect() {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun discoverServices() {
        gatt?.discoverServices()
    }


    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun subscribeToBaseAlarmCharacteristics(characteristicsUUID: List<UUID>) {
        val service = gatt?.getService(BASE_ALARM_SERVICE_UUID)
        for (characteristicUUID in characteristicsUUID) {/*
            val characteristic = service?.getCharacteristic(characteristicUUID)
            if (characteristic != null) {
                Log.d("GATT-READ", "${setCharacteristicNotification(gatt, characteristic, true)}")
*/
                //val success = gatt?.setCharacteristicNotification(characteristic, true) // enable notification
                //Log.d("GATT-READ", "Read status: $success")
                //Log.d("GATT-READ", "notification status = ${success}")
/*
                val uuid: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                val descriptor: BluetoothGattDescriptor = characteristic.getDescriptor(uuid)
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                gatt?.writeDescriptor(descriptor)*/



            GlobalScope.launch (Dispatchers.Main) {
                delay(1000)

                var characteristic: BluetoothGattCharacteristic? = null

                while (characteristic == null) {
                    characteristic = service?.getCharacteristic(characteristicUUID)
                    delay(1000)

                }

                val notifyRegistered = setCharacteristicNotification(gatt, characteristic!!, true)
                /*
                val characteristic = service?.getCharacteristic(characteristicUUID)

                val notifyRegistered = setCharacteristicNotification(gatt, characteristic!!, true)*/
                Log.d("GATT-READ", "$notifyRegistered")

                delay(1000) // attendo approssimativamente 1s perché è necessario attendere che la write di setCharacteristicNotification() sia stata fatta prima di fare una altra write. per gestire meglio la cosa sarebbe stato opportuno usare meccanismi di sincronizzazione, cioe far partire il prossimo blocco di codice solo una volta che viene chiamata la callback del precedente blocco di codice


                characteristic!!.descriptors.find { desc ->
                    desc.uuid.toString() == CCCD.uuid
                }?.also { cccd ->
                    if (characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        gatt?.writeDescriptor(cccd)
                    }

                    /*if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                        cccd.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
                        gatt?.writeDescriptor(cccd)
                    }*/

                }
            }

                // give gatt a little breathing room for writes
               // delay(300L)






            //}
        }


    }
    fun setCharacteristicNotification(
        bluetoothGatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean {
        Log.d("GATT-READ", "setCharacteristicNotification")
        bluetoothGatt?.setCharacteristicNotification(characteristic, enable)
        val descriptor =
            characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        descriptor.setValue(
            if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else byteArrayOf(
                0x00,
                0x00
            )
        )
        return bluetoothGatt?.writeDescriptor(descriptor) ?: false //descriptor write operation successfully started?
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readManufacturerName() {
        val service = gatt?.getService(MANUFACTURER_NAME_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(MANUFACTURER_NAME_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("GATT", "Read status: $success")
        }
    }
    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    public fun readRaspCharacteristics() {
        val service = gatt?.getService(BASE_ALARM_SERVICE_UUID)
        if (service == null) {
            Log.e("GATT-READ", "Service not found: $BASE_ALARM_SERVICE_UUID")
            return
        }

        val characteristic = service.getCharacteristic(BASE_ALARM_CHARACTERISTIC_UUID)
        if (characteristic == null) {
            Log.e("GATT-READ", "Characteristic not found: $BASE_ALARM_CHARACTERISTIC_UUID")
            return
        }

        val success = gatt?.readCharacteristic(characteristic) == true
        Log.d("GATT-READ", "Read status: $success")
        if (!success) {
            Log.e("GATT-READ", "Failed to initiate characteristic read")
        }
    }
    @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
    fun readAlertMessage() {
        val service = gatt?.getService(BASE_ALARM_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BASE_ALARM_TEXT_CHARACTERISTIC_UUID)
        if (characteristic != null) {
            val success = gatt?.readCharacteristic(characteristic)
            Log.v("GATT", "Read status: $success")
        }
    }

}



abstract class ParsableUuid(val uuid: String) {

    abstract fun commands(param: Any? = null): Array<String>
    abstract fun getReadStringFromBytes(byteArray: ByteArray): String

}


object CCCD : ParsableUuid("00002902$UUID_DEFAULT".lowercase()) {

    override fun commands(param: Any?): Array<String> {
        return if (param == BleProperties.PROPERTY_NOTIFY)
            arrayOf(
                "Enable Notifications: ${BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE.toHex()}",
                "Disable Notifications: ${BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.toHex()}"
            ) else arrayOf(
            "Enable Indications: ${BluetoothGattDescriptor.ENABLE_INDICATION_VALUE.toHex()}",
            "Disable Indications: ${BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE.toHex()}"
        )
    }

    override fun getReadStringFromBytes(byteArray: ByteArray): String {
        return if (byteArray.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ||
            byteArray.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
        )
            "Enabled."
        else
            "Disabled."
    }
}
const val UUID_DEFAULT = "-0000-1000-8000-00805F9B34FB"

enum class BleProperties(val value: Int) {
    PROPERTY_BROADCAST(1),
    PROPERTY_EXTENDED_PROPS(128),
    PROPERTY_INDICATE(32),
    PROPERTY_NOTIFY(16),
    PROPERTY_READ(2),
    PROPERTY_SIGNED_WRITE(64),
    PROPERTY_WRITE(8),
    PROPERTY_WRITE_NO_RESPONSE(4);

    companion object {

        fun getAllProperties(bleValue: Int): List<BleProperties> {
            var propertyList = mutableListOf<BleProperties>()

            values().forEach {
                if (bleValue and it.value > 0)
                    propertyList.add(it)
            }
            return propertyList

        }
    }
}




/**
 * Use “%02x” to convert the given byte to its corresponding hex value.
 * Moreover, it pads the hex value with a leading zero if necessary.
 */
fun ByteArray.toHex(): String =
    "0x" + joinToString(separator = "") { eachByte -> "%02X".format(eachByte).uppercase() }

fun ByteArray.print(): String =
    joinToString(separator = ",") { eachByte -> eachByte.toInt().toString() }

fun Int.toHex(): String =
    "0x%04X".format(this)

fun Int.toHex2(): String =
    "%02X".format(this)

fun ByteArray.bitsToHex(): String {
    val bitSet = BitSet.valueOf(this)
    Log.d("GATT-READ", bitSet.toString())
    Log.d("GATT-READ", bitSet[bitSet.size()].toString())
    return bitSet.toLongArray().toHex()
}

fun ByteArray.bits(): String {
    val bitSet = BitSet.valueOf(this)
    val sb = StringBuilder()
    for (i in 0..15) {
        val curBit = if (i <= bitSet.size())
            bitSet.get(i)
        else
            false

        sb.append(if (curBit) '1' else '0')
    }
    return sb.reverse().toString()
}

fun ByteArray.toBinaryString(): String {
    return this.joinToString(" ") {
        //Integer.toBinaryString(it.toInt())
        toBinary(it.toInt(), 8)
    }
}

private fun toBinary(num: Int, length: Int): String {
    var num = num
    val sb = StringBuilder()
    for (i in 0 until length) {
        sb.append(if (num and 1 == 1) '1' else '0')
        num = num shr 1
    }
    return sb.reverse().toString()
}

fun LongArray.toHex(): String =
    joinToString(separator = "") { words -> "0x%04X".format(words.toShort()) }

fun Byte.toHex(): String = "%02X".format(this)

fun String.decodeHex(): ByteArray {
    with (this.substringAfter("0x")) {
        require(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
        //.toString(Charsets.ISO_8859_1)
    }
}

fun ByteArray.decodeSkipUnreadable(): String {
    val badChars = '\uFFFD'

    this.forEach {
        Log.d("GATT-READ", this.indexOf(it).toString() + ": " + it.toInt().toString())
    }

    val newString = this.decodeToString().filter {
        it != badChars
    }

    /*.filter {
    it.code > 0
}*/

    return newString
}


// "^([A-F0-9]{4}|[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12})$"
// (4 or 8 in beginning) yyyyxxxx-0000-1000-8000-00805f9b34fb
// only replace first four 0's if padded.
fun UUID.toGss() =
    this.toString()
        .replaceFirst(Regex("^0+(?!$)"), "")
        .replace("-0000-1000-8000-00805f9b34fb", "")
        .uppercase()

fun Long.toMillis() =
    System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(
        SystemClock.elapsedRealtimeNanos() - this, TimeUnit.NANOSECONDS
    )

fun Long.toDate() =
    SimpleDateFormat("MM/dd/yy h:mm:ss ", Locale.US).format(Date(this))



fun decodeHexToUtf8(hexString: String): String {
    // Converte la stringa esadecimale in un array di byte
    val byteArray = ByteArray(hexString.length / 2) { index ->
        hexString.substring(index * 2, index * 2 + 2).toInt(16).toByte()
    }

    // Inverte l'ordine dei byte per little endian
    byteArray.reverse()

    // Decodifica l'array di byte UTF-8 in una stringa
    return byteArray.toString(Charsets.UTF_8).reversed()
}

