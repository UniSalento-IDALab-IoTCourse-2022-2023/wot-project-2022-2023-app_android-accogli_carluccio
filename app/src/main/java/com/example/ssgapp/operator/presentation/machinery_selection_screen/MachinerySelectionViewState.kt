package com.example.ssgapp.operator.presentation.machinery_selection_screen

import com.example.ssgapp.operator.ble.BluetoothDeviceWithRssi
import com.example.ssgapp.common.domain.model.Machinery

data class MachinerySelectionViewState(
    // Bluetooth info
    val machineryScanningState: MachinerySelectionScanningState = MachinerySelectionScanningState.RequestingBLEPermission,
    val foundDevices: List<BluetoothDeviceWithRssi> = emptyList(),

    // Machinery info (API)
    val machineryList: List<Machinery> = emptyList(),
    val selectedMachinery: Machinery? = null,

    val hasAllPermissions: Boolean = false,


    val error: String? = null
)

enum class MachinerySelectionScanningState {
    RequestingBLEPermission,

    FetchingAllowedMachineries,
    AvailableMachineriesListEmpty,
    FetchingError,

    BluetoothScanningStarted,
    BluetoothScanningPaused,
    BluetoothScanningError,


}