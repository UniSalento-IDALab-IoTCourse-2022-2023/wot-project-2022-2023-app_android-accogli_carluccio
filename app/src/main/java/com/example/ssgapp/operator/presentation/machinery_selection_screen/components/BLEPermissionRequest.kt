package com.example.ssgapp.operator.presentation.machinery_selection_screen.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ssgapp.R
import com.example.ssgapp.operator.presentation.machinery_selection_screen.ALL_BLE_PERMISSIONS
import com.example.ssgapp.ui.theme.SSGAppTheme


@Composable
fun BLEPermissionRequest(
    onBLEPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {

            Icon(
                painter = painterResource(id = R.drawable.bluetooth_24px),
                contentDescription = "Localized description",
                tint = Color.Gray,
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 44.dp)
            )


            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { granted ->
                if (granted.values.all { it }) {
                    // comunico che ho abilitato i permessi
                    onBLEPermissionGranted()
                }
            }

            Text(
                text = "To connect to the machinery, you need to grant BLE permissions."
            )

            Button(
                onClick = { launcher.launch(ALL_BLE_PERMISSIONS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }

        }

    }

}

@Preview(showBackground = true)
@Composable
fun BLEPermissionRequestPreview() {
    SSGAppTheme {
        BLEPermissionRequest(
            onBLEPermissionGranted = {}
        )

    }
}