package com.example.ssgapp.operator.presentation.machinery_selection_screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssgapp.common.presentation.util.components.LoadingIndicator
import com.example.ssgapp.ui.theme.SSGAppTheme

@Composable
fun FetchingAllowedMachineriesLoader() {
    Column {
        LoadingIndicator(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(vertical = 16.dp)
        )

        Text(
            text = "Fetching allowed machineries",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.fillMaxHeight())
    }
}

@Preview(showBackground = true)
@Composable
fun FetchingAllowedMachineriesLoaderPreview() {
    SSGAppTheme {
        FetchingAllowedMachineriesLoader(

        )
    }
}