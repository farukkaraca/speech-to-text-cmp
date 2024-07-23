package components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LanguageSelectionDialog(
    supportedLanguages: List<String>,
    onLanguageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier
                .padding(
                    vertical = 50.dp,
                    horizontal = 10.dp
                ),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .padding(
                            bottom = 8.dp
                        )
                )

                if (supportedLanguages.isNotEmpty()) {
                    LazyColumn {
                        items(supportedLanguages) { language ->
                            Text(
                                text = language,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(8.dp)
                                    .clickable {
                                        onLanguageSelected(language)
                                        onDismissRequest()
                                    }
                            )
                        }
                    }
                } else {
                    Text(
                        "No supported languages"
                    )
                }
            }
        }
    }
}
