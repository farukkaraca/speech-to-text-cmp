import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import data.Error
import data.ListeningStatus
import data.PermissionRequestStatus
import data.TranscriptState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

actual class SpeechToText {

    constructor(context: Context) {
        this.context = context
        this.activity = context as Activity
        initializeSpeechRecognizer()

        getSupportedLanguages { supportedLanguages ->
            transcriptState.update {
                it.copy(
                    supportedLanguages = supportedLanguages,
                )
            }
        }
    }

    actual constructor()

    private var _transcriptState = MutableStateFlow(
        TranscriptState(
            listeningStatus = ListeningStatus.INACTIVE,
            error = Error(isError = false),
            transcript = null,
        )
    )

    actual val transcriptState: MutableStateFlow<TranscriptState>
        get() = _transcriptState

    private lateinit var context: Context
    private lateinit var activity: Activity
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
            Log.d("SpeechToText", "SpeechRecognizer initialized")
        } else {
            Log.e("SpeechToText", "SpeechRecognizer not available on this device")
        }
    }

    actual fun startTranscribing() {
        if (speechRecognizer == null) {
            transcriptState.update {
                it.copy(
                    listeningStatus = ListeningStatus.INACTIVE,
                    error = Error(isError = true, message = RecognizerError.NilRecognizer.message)
                )
            }
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            }

            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                _transcriptState.value.selectedLanguage
            )


            recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    transcriptState.update {
                        it.copy(listeningStatus = ListeningStatus.LISTENING)
                    }
                }

                override fun onBeginningOfSpeech() {

                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {

                }

                override fun onEndOfSpeech() {
                    transcriptState.update {
                        it.copy(listeningStatus = ListeningStatus.INACTIVE)
                    }
                }

                override fun onError(error: Int) {
                    transcriptState.update {
                        it.copy(
                            listeningStatus = ListeningStatus.INACTIVE,
                            error = Error(isError = true, message = error.toString())
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        transcriptState.update {
                            it.copy(
                                listeningStatus = ListeningStatus.INACTIVE,
                                error = Error(isError = false),
                                transcript = matches[0]
                            )
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches =
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        transcriptState.update {
                            it.copy(
                                transcript = matches[0],
                                error = Error(isError = false)
                            )
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {

                }

            }

            speechRecognizer?.setRecognitionListener(recognitionListener)
            speechRecognizer?.startListening(intent)
        }
    }

    actual fun stopTranscribing() {
        speechRecognizer?.stopListening()
    }

    actual fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
            onPermissionResult(PermissionRequestStatus.NOT_ALLOWED)
        } else {
            onPermissionResult(PermissionRequestStatus.ALLOWED)
        }
    }

    actual fun setLanguage(languageCode: String) {
        transcriptState.update {
            it.copy(selectedLanguage = languageCode)
        }
    }

    actual fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit) {
        val availableLocales = ArrayList<String>()

        Locale.getAvailableLocales().map {
            val langCode = it.language.toString()
            if (!availableLocales.contains(langCode)) {
                availableLocales.add(langCode)
            }
        }

        onLanguagesResult(availableLocales)
    }
}
