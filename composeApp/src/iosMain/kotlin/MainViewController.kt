import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val speechToText = SpeechToText()
    App(speechToText)
}