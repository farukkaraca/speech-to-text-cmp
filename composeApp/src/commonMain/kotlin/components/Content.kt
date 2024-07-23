package components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Mic
import androidx.compose.material.icons.twotone.MicOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.TranscriptState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun Content(
    isListening: Boolean = true,
    transcriptState: TranscriptState,
    supportedLanguages: List<String>,
    onLanguageSelected: (String) -> Unit,
    onClickMic: () -> Unit = {},
) {

    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember {
        mutableStateOf(
            transcriptState.selectedLanguage
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                vertical = 10.dp,
                horizontal = 60.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
            text = if (isListening) "Listening... \nTap the icon to stop listening." else "Tap the icon to start listening.",
            style = TextStyle(
                fontSize = 14.sp,
                letterSpacing = TextUnit(
                    1f,
                    TextUnitType.Sp
                )
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        IconButton(
            onClick = {
                onClickMic()
            }) {
            Icon(
                modifier = Modifier
                    .scale(if (isListening) scale else 1f)
                    .size(100.dp),
                imageVector = if (isListening) Icons.TwoTone.Mic else Icons.TwoTone.MicOff,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        val (result, resultTextColor) = when {
            transcriptState.error.isError -> {
                "ERROR: ${transcriptState.error.message}" to MaterialTheme.colors.error
            }

            transcriptState.transcript != null -> {
                transcriptState.transcript to MaterialTheme.colors.onBackground
            }

            else -> "" to MaterialTheme.colors.onBackground
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = result,
            style = TextStyle(
                fontSize = 14.sp,
                letterSpacing = TextUnit(1.5f, TextUnitType.Sp),
                textAlign = TextAlign.Justify,
                color = resultTextColor
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Black
            ),
            onClick = { showLanguageDialog = true }) {
            Text(
                style = TextStyle(
                    color = Color.White
                ),
                text = selectedLanguage
            )
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                supportedLanguages = supportedLanguages,
                onLanguageSelected = {
                    onLanguageSelected(it)
                    selectedLanguage = it
                },
                onDismissRequest = {
                    showLanguageDialog = false
                }
            )
        }
    }
}
