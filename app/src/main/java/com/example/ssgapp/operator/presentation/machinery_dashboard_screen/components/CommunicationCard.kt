package com.example.ssgapp.operator.presentation.machinery_dashboard_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ssgapp.operator.domain.model.CommunicationMessage
import com.example.ssgapp.operator.domain.model.CommunicationMessagePriority
import com.example.ssgapp.operator.domain.model.CommunicationMessageType

@Composable
fun CommunicationCard(
    communicationMessage: CommunicationMessage,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        //shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        //elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = communicationMessage.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = communicationMessage.priority.color, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = communicationMessage.priority.text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    text = communicationMessage.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = communicationMessage.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunicationCard_Alarm_Preview() {
    CommunicationCard(
        communicationMessage = CommunicationMessage(
            title = "Notification Title",
            message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
            time = "12:00 PM",
            type = CommunicationMessageType.DistanceAlarm,
            priority = CommunicationMessagePriority.Warning
        ),
        modifier = Modifier.padding(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CommunicationCard_General_Preview() {
    CommunicationCard(
        communicationMessage = CommunicationMessage(
            title = "Notification Title",
            message = "This is the message of the notification. It can be a bit longer to demonstrate the ellipsis overflow in case of longer texts.",
            time = "12:00 PM",
            type = CommunicationMessageType.DistanceAlarm,
            priority = CommunicationMessagePriority.Warning
        ),
        modifier = Modifier.padding(8.dp)
    )
}
