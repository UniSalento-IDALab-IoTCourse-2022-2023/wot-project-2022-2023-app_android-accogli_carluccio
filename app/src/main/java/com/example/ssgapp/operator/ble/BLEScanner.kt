package com.example.ssgapp.operator.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

//These fields are marked as API >= 31 in the Manifest class, so we can't use those without warning.
//So we create our own, which prevents over-suppression of the Linter
const val PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
const val PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"

enum class BluetoothScanningState {
    BluetoothScanningOn,
    BluetoothScanningOff,
    BluetoothScanningError,
}

class BLEScanner(context: Context) {

    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        ?: throw Exception("Bluetooth is not supported by this device")

    val bluetoothScanningState = MutableStateFlow(BluetoothScanningState.BluetoothScanningOff)

    val foundDevices = MutableStateFlow<List<BluetoothDeviceWithRssi>>(emptyList())

    private val scanner: BluetoothLeScanner
        get() = bluetooth.adapter.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return

            Log.d("BLE", result.toString()) // TODO: rimuovere questa riga - l'ho messa solo per capire se scansiona o meno in diversi stati dell'app


            val device = BluetoothDeviceWithRssi(result.device, result.rssi)

            if (!foundDevices.value.any { it.address == device.address }) { // Se il dispositivo scansionato non è presente in lista
                //if(device.address.equals("6D:7B:53:C3:2C:E8", ignoreCase = true)) {
                foundDevices.update { it + device } // Aggiungilo
                //}
            } else { // Se il dispositivo scansionato è gia presente in lista
                // Aggiorna il suo Rssi al valore piu recente
                foundDevices.update { list ->
                    list.map { if (it.address == device.address) it.copy(rssi = device.rssi) else it }
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            results ?: return

            for (result in results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            bluetoothScanningState.value = BluetoothScanningState.BluetoothScanningError
            // TODO: Se si verifica un errore, la scansione non viene messa in pausa!!
        }
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun startScanning() {
        scanner.startScan(scanCallback)
        bluetoothScanningState.value = BluetoothScanningState.BluetoothScanningOn
    }

    @RequiresPermission(PERMISSION_BLUETOOTH_SCAN)
    fun stopScanning() {
        scanner.stopScan(scanCallback)
        bluetoothScanningState.value = BluetoothScanningState.BluetoothScanningOff
    }


}

// WRAPPER CLASS PER IMPLEMENTARE RSSI

data class BluetoothDeviceWithRssi(
    private val device: BluetoothDevice,
    val rssi: Int
) {
    val address: String
        get() = device.address

    val name: String?
        @SuppressLint("MissingPermission")
        get() = device.name

    // Puoi aggiungere altre proprietà di BluetoothDevice che ti servono
    val bondState: Int
        @SuppressLint("MissingPermission")
        get() = device.bondState

    // funzione che restituisce l'oggetto BluetoothDevice originale, senza la proprietà rssi
    fun getDevice(): BluetoothDevice {
        return device
    }

    // Override toString(), equals() e hashCode() se necessario
    @SuppressLint("MissingPermission")
    override fun toString(): String {
        return "BluetoothDeviceWithRssi(name=${device.name}, address=${device.address}, rssi=$rssi)"
    }
}
