package com.example.ssgapp.common.presentation.util.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ssgapp.ui.theme.SSGAppTheme

@Composable
fun SuggestionCard(
    suggestionText: AnnotatedString,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE7E7E7),
        ),
        modifier = modifier
    ) {
        Text(
            text = suggestionText,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(10.dp)
        )

    }
}

@Preview
@Composable
fun SuggestionCardPreview() {
    SSGAppTheme {
        SuggestionCard(
            buildAnnotatedString {
                append("Keep the application opened, or in the background.\n")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Do not close the application while working!")
                }
            }
        )
    }
}