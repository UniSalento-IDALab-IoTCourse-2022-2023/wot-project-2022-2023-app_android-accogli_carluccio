package com.example.ssgapp.operator.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ssgapp.ui.theme.Red
import java.time.LocalTime

class CommunicationMessage(
    val title: String,
    val message: String,
    val time: String,
    val type: CommunicationMessageType,
    val priority: CommunicationMessagePriority
) {
    companion object {
        fun fromBLE(bytes: String): CommunicationMessage {




            var type = CommunicationMessageType.General // inizia con 01
            var title = "General"
            var message = ""
            var priority = CommunicationMessagePriority.Communication
            if (bytes.endsWith("01")) {
                priority = CommunicationMessagePriority.Warning
            } else if (bytes.endsWith("02")) {
                priority = CommunicationMessagePriority.Danger
            }


            if (bytes.startsWith("00")) {
                type = CommunicationMessageType.DistanceAlarm // inizia con 00
                title = "Distance"

                if (priority == CommunicationMessagePriority.Communication) {
                    message = "Distance alarm cleared"
                } else {
                    message = "Close to pedestrian!"
                }
            } else {
                message = "todo" // in questo caso devo ancora leggermi la caratteristica BLE
            }

            val currentTime = LocalTime.now()
            val hours = String.format("%02d", currentTime.hour)
            val minutes = String.format("%02d", currentTime.minute)
            val seconds = String.format("%02d", currentTime.second)

            return CommunicationMessage(
                title = title,
                message = message,
                time = "$hours:$minutes",
                type = type,
                priority = priority
            )
        }
    }
}

enum class CommunicationMessageType(val text: String, val color: Color) {
    General("General", Color.DarkGray),
    DistanceAlarm("Distance Alarm", Color(0xFFF57A7A)),
}

enum class CommunicationMessagePriority(val text: String, val color: Color) {
    Communication("Communication", Color.DarkGray),
    Warning("Warning", Color(0xFFE7CE83)),
    Danger("Danger", Color(0xFFF57A7A)),
}
