package presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.ListeningStatus
import data.TranscriptState

@Composable
fun Content(
    paddingValues: PaddingValues,
    transcriptState: TranscriptState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                color = MaterialTheme.colorScheme.background
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val (result, resultTextColor) = when {
            transcriptState.error.isError -> {
                "ERROR: ${transcriptState.error.message}" to MaterialTheme.colorScheme.error
            }

            transcriptState.transcript != null -> {
                transcriptState.transcript to MaterialTheme.colorScheme.onBackground
            }

            else -> "" to MaterialTheme.colorScheme.onBackground
        }

        val scrollState = rememberScrollState()

        LaunchedEffect(result) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(
                    vertical = 5.dp,
                    horizontal = 10.dp
                )
                .verticalScroll(scrollState)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (result.isBlank()) "Start to Listen" else result,
                style = TextStyle(
                    fontSize = 28.sp,
                    letterSpacing = TextUnit(
                        1.5f, TextUnitType.Sp
                    ),
                    textAlign = TextAlign.Center,
                    color = resultTextColor,
                    fontFamily = FontFamily.Serif,
                )
            )
        }

        VoiceAnimation(transcriptState.listeningStatus == ListeningStatus.LISTENING)

        Spacer(modifier = Modifier.height(30.dp))
    }
}