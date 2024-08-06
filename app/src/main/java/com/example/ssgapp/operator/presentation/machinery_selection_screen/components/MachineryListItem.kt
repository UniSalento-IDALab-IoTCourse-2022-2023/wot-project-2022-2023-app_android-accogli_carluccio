package com.example.ssgapp.operator.presentation.machinery_selection_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.ui.theme.SSGAppTheme

@Composable
fun MachineryListItem(
    machinery: Machinery,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.LightGray else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(machinery.name!!)
            Text(
                machinery.macAddress!!,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (machinery.isRemote != null && machinery.isRemote == true){ "Remote" } else { "Not Remote" },
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MachineryListItem_Unselected_Preview() {
    SSGAppTheme {
        MachineryListItem(
            machinery = Machinery(
                id = "A",
                name = "Carro Plaza 2T",
                type = "Camion Trasportatore",
                serialNumber = "AA77AA",
                macAddress = "AAAA:AAAA:AAAA:AAAA",
                isRemote = false
            ),
            isSelected = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MachineryListItem_Selected_Preview() {
    SSGAppTheme {
        MachineryListItem(
            machinery = Machinery(
                id = "A",
                name = "Carro Plaza 2T",
                type = "Camion Trasportatore",
                serialNumber = "AA77AA",
                macAddress = "AAAA:AAAA:AAAA:AAAA",
                isRemote = true
            ),
            isSelected = true,
            onClick = {}
        )
    }
}