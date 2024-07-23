import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import components.Content
import data.ListeningStatus
import data.PermissionRequestStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(speechToText: SpeechToText = SpeechToText()) {
    MaterialTheme {
        val transcriptState = speechToText.transcriptState.collectAsState()

        Content(
            isListening = transcriptState.value.listeningStatus == ListeningStatus.LISTENING,
            transcriptState = transcriptState.value,
            supportedLanguages = transcriptState.value.supportedLanguages,
            onLanguageSelected = {
                speechToText.setLanguage(it)
            },
            onClickMic = {
                if (transcriptState.value.listeningStatus == ListeningStatus.INACTIVE) {
                    speechToText.requestPermission { requestPermissionResult ->
                        when (requestPermissionResult) {
                            PermissionRequestStatus.ALLOWED -> {
                                speechToText.startTranscribing()
                            }

                            PermissionRequestStatus.NOT_ALLOWED -> {
                                println("access refused")
                            }

                            else -> {
                                println("never ask again")
                            }
                        }
                    }
                } else {
                    speechToText.stopTranscribing()
                }
            }
        )
    }
}
