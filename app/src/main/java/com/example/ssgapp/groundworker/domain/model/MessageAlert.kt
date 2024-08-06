package com.example.ssgapp.groundworker.domain.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime


data class MessageAlert(
    @SerializedName("timestamp") val timestamp: String = LocalDateTime.now().toString(),
    @SerializedName("type") val type: MessageAlertType,
    @SerializedName("technologyID") val technology: Technology, // TODO: da toglier ID finale nel prossimo jar che arriva
    @SerializedName("priority") val priority: MessageAlertPriority,
    @SerializedName("workerID") val workerId: String,
    @SerializedName("machineryID") val machineryId: String,
    @SerializedName("isEntryAlarm") val isAlarmStarted: Boolean
)

enum class MessageAlertType (val string: String) {
    DRIVER_AWAY("DRIVER_AWAY"),
    GENERAL("GENERAL"),
    DISTANCE("DISTANCE") // non mi piace questo nome, avrei preferito qualcosa come "CLOSE_TO_MACHINE"
}
enum class MessageAlertPriority (val string: String) {
    WARNING("WARNING"),
    DANGER("DANGER"),
    COMMUNICATION("COMMUNICATION")
}
enum class Technology (val string: String) {
    BEACON("BLE - Beacon"),
    RSSI("BLE - RSSI")
}