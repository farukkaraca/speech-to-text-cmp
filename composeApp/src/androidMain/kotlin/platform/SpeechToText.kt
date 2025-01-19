package platform

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import data.Error
import data.ListeningStatus
import data.PermissionRequestStatus
import data.RecognizerError
import data.TranscriptState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

actual class SpeechToText(
    private val context: Context,
    private val activity: Activity
) {
    private val _transcriptState = MutableStateFlow(
        TranscriptState(
            listeningStatus = ListeningStatus.INACTIVE,
            error = Error(isError = false),
            transcript = null,
        )
    )

    actual val transcriptState: MutableStateFlow<TranscriptState>
        get() = _transcriptState

    private var permissionLauncher = initPermissionLauncher()
    private var _permissionCallback: ((PermissionRequestStatus) -> Unit)? = null

    private fun initPermissionLauncher() =
        (activity as ComponentActivity).activityResultRegistry.register(
            "permission",
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                _permissionCallback?.invoke(PermissionRequestStatus.ALLOWED)
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    _permissionCallback?.invoke(PermissionRequestStatus.NEVER_ASK_AGAIN)
                } else {
                    _permissionCallback?.invoke(PermissionRequestStatus.NOT_ALLOWED)
                }
            }
            _permissionCallback = null
        }

    init {
        initializeSpeechRecognizer()
        getSupportedLanguages { supportedLanguages ->
            _transcriptState.update {
                it.copy(
                    supportedLanguages = supportedLanguages,
                )
            }
        }
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            Log.d("SpeechToText", "SpeechRecognizer initialized")
        } else {
            Log.e("SpeechToText", "SpeechRecognizer not available on this device")
        }
    }

    actual fun startTranscribing() {
        if (speechRecognizer == null) {
            _transcriptState.update {
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
                    _transcriptState.update {
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
                    _transcriptState.update {
                        it.copy(listeningStatus = ListeningStatus.INACTIVE)
                    }
                }

                override fun onError(error: Int) {
                    _transcriptState.update {
                        it.copy(
                            listeningStatus = ListeningStatus.INACTIVE,
                            error = Error(isError = true, message = error.toString())
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _transcriptState.update {
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
                        _transcriptState.update {
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
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionResult(PermissionRequestStatus.ALLOWED)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                _permissionCallback = onPermissionResult
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    _permissionCallback = onPermissionResult
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    onPermissionResult(PermissionRequestStatus.NEVER_ASK_AGAIN)
                }
            }
        }
    }

    actual fun setLanguage(languageCode: String) {
        _transcriptState.update {
            it.copy(selectedLanguage = languageCode)
        }
    }

    actual fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit) {
        val supportedLanguages = listOf(
            "en-US", // English (United States)
            "en-GB", // English (United Kingdom)
            "en-AU", // English (Australia)
            "en-CA", // English (Canada)
            "es-ES", // Spanish (Spain)
            "es-MX", // Spanish (Mexico)
            "es-AR", // Spanish (Argentina)
            "fr-FR", // French (France)
            "fr-CA", // French (Canada)
            "de-DE", // German
            "it-IT", // Italian
            "pt-PT", // Portuguese (Portugal)
            "pt-BR", // Portuguese (Brazil)
            "ru-RU", // Russian
            "zh-CN", // Chinese (Simplified)
            "zh-TW", // Chinese (Traditional)
            "ja-JP", // Japanese
            "ko-KR", // Korean
            "ar-SA", // Arabic (Saudi Arabia)
            "ar-AE", // Arabic (UAE)
            "tr-TR", // Turkish
            "hi-IN", // Hindi
            "th-TH", // Thai
            "vi-VN", // Vietnamese
            "id-ID", // Indonesian
            "ms-MY", // Malay
            "fil-PH", // Filipino
            "nl-NL", // Dutch
            "pl-PL", // Polish
            "ro-RO", // Romanian
            "hu-HU", // Hungarian
            "cs-CZ", // Czech
            "el-GR", // Greek
            "sv-SE", // Swedish
            "da-DK", // Danish
            "no-NO", // Norwegian
            "fi-FI", // Finnish
            "he-IL", // Hebrew
            "bn-IN", // Bengali
            "ta-IN"  // Tamil
        ).filter { locale ->
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                }
                intent.resolveActivity(context.packageManager) != null &&
                        SpeechRecognizer.isRecognitionAvailable(context)
            } catch (e: Exception) {
                false
            }
        }

        onLanguagesResult(supportedLanguages)
    }

    actual fun copyText(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Speech Text", text)
        clipboard.setPrimaryClip(clip)
    }

    actual fun showNeedPermission() {
        _transcriptState.update {
            it.copy(
                showPermissionNeedDialog = true
            )
        }
    }

    actual fun dismissPermissionDialog() {
        _transcriptState.update {
            it.copy(
                showPermissionNeedDialog = false
            )
        }
    }

    actual fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        dismissPermissionDialog()
    }
}