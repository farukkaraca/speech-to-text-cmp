package platform

import data.Error
import data.ListeningStatus
import data.PermissionRequestStatus
import data.RecognizerError
import data.TranscriptState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptions
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionSetActiveOptions
import platform.AVFAudio.setActive
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.localeIdentifier
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIPasteboard
import kotlin.coroutines.resume

actual class SpeechToText {

    private var _transcriptState = MutableStateFlow(
        TranscriptState(
            listeningStatus = ListeningStatus.INACTIVE,
            error = Error(isError = false),
            transcript = null,
        )
    )

    actual val transcriptState: MutableStateFlow<TranscriptState>
        get() = _transcriptState

    private var audioEngine: AVAudioEngine? = null
    private var request: SFSpeechAudioBufferRecognitionRequest? = null
    private var task: SFSpeechRecognitionTask? = null
    private var recognizer = SFSpeechRecognizer()
    private val customScope = CoroutineScope(Dispatchers.Default)

    init {
        getSupportedLanguages { supportedLanguages ->
            transcriptState.update {
                it.copy(
                    supportedLanguages = supportedLanguages,
                )
            }
        }
    }

    actual fun startTranscribing() {
        if (recognizer.isAvailable()) {
            try {
                val (engine, req) = prepareEngine()
                audioEngine = engine
                request = req
                task = recognizer.recognitionTaskWithRequest(req) { result, error ->
                    if (result != null) {
                        updateTranscript(
                            isError = false,
                            message = result.bestTranscription.formattedString
                        )
                    } else if (error != null) {
                        //updateTranscript(isError = true, message = error.localizedDescription)
                        resetTranscription()
                    }
                }
            } catch (e: Exception) {
                updateTranscript(isError = true, message = e.message)
                resetTranscription()
            }

            transcriptState.update {
                it.copy(listeningStatus = ListeningStatus.LISTENING)
            }

        } else {
            transcriptState.update {
                it.copy(
                    listeningStatus = ListeningStatus.INACTIVE,
                    error = Error(
                        isError = true,
                        message = RecognizerError.RecognizerIsUnavailable.message
                    )
                )
            }
        }
    }

    actual fun stopTranscribing() {
        resetTranscription()
        transcriptState.update {
            it.copy(
                listeningStatus = ListeningStatus.INACTIVE
            )
        }
    }

    private fun resetTranscription() {
        task?.cancel()
        audioEngine?.stop()
        audioEngine = null
        request = null
        task = null
    }

    actual fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit) {
        customScope.launch {
            val hasRecordPermission = hasPermissionToRecord()
            val hasSpeechPermission = hasAuthorizationToRecognize()
            
            when {
                hasRecordPermission && hasSpeechPermission -> {
                    onPermissionResult(PermissionRequestStatus.ALLOWED)
                }
                !hasRecordPermission || !hasSpeechPermission -> {
                    val recordAuthStatus = AVAudioSession.sharedInstance().recordPermission
                    val speechAuthStatus = SFSpeechRecognizer.authorizationStatus()
                    
                    if (recordAuthStatus == AVAudioSessionRecordPermissionDenied ||
                        speechAuthStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusDenied) {
                        onPermissionResult(PermissionRequestStatus.NEVER_ASK_AGAIN)
                    } else {
                        onPermissionResult(PermissionRequestStatus.NOT_ALLOWED)
                    }
                }
            }
        }
    }

    private suspend fun hasAuthorizationToRecognize(): Boolean =
        suspendCancellableCoroutine { continuation ->
            SFSpeechRecognizer.requestAuthorization { status ->
                continuation.resume(status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized)
            }
        }

    private suspend fun hasPermissionToRecord(): Boolean =
        suspendCancellableCoroutine { continuation ->
            AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                continuation.resume(granted)
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun prepareEngine(): Pair<AVAudioEngine, SFSpeechAudioBufferRecognitionRequest> {
        val audioEngine = AVAudioEngine()
        val request = SFSpeechAudioBufferRecognitionRequest()
            .apply {
                shouldReportPartialResults = true
            }

        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            AVAudioSessionModeMeasurement,
            AVAudioSessionCategoryOptions.MIN_VALUE,
            null
        )
        audioSession.setActive(
            true,
            AVAudioSessionSetActiveOptions.MIN_VALUE,
            null
        )

        val inputNode = audioEngine.inputNode
        val recordingFormat = inputNode.outputFormatForBus(0u)
        inputNode.installTapOnBus(0u, 1024u, recordingFormat) { buffer, _ ->
            request.appendAudioPCMBuffer(buffer!!)
        }

        audioEngine.prepare()
        audioEngine.startAndReturnError(null)

        return Pair(audioEngine, request)
    }

    private fun updateTranscript(isError: Boolean, message: String?) {
        if (!isError) {
            transcriptState.update {
                it.copy(
                    transcript = message,
                    error = Error(isError = false)
                )
            }
        } else {
            transcriptState.update {
                it.copy(
                    listeningStatus = ListeningStatus.INACTIVE,
                    error = Error(isError = true, message = message),
                    transcript = null
                )
            }
        }
    }

    actual fun setLanguage(languageCode: String) {
        val locale = NSLocale(languageCode)
        recognizer = SFSpeechRecognizer(locale)
    }

    actual fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit) {
        val supportedLocales = SFSpeechRecognizer.supportedLocales()
        val languages = supportedLocales.map { (it as NSLocale).localeIdentifier() }
        onLanguagesResult(languages)
    }

    actual fun copyText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun showNeedPermission() {
        transcriptState.update {
            it.copy(
                showPermissionNeedDialog = true,
            )
        }
    }

    actual fun dismissPermissionDialog() {
        transcriptState.update {
            it.copy(
                showPermissionNeedDialog = false,
            )
        }
    }

    actual fun openAppSettings() {
        val recordAuthStatus = AVAudioSession.sharedInstance().recordPermission
        val speechAuthStatus = SFSpeechRecognizer.authorizationStatus()
        
        when {
            recordAuthStatus == AVAudioSessionRecordPermissionDenied -> {
                val micSettingsUrl = NSURL.URLWithString("prefs:root=Privacy&path=MICROPHONE")
                if (micSettingsUrl != null && UIApplication.sharedApplication.canOpenURL(micSettingsUrl)) {
                    UIApplication.sharedApplication.openURL(
                        micSettingsUrl,
                        mapOf<Any?, Any>(),
                        null
                    )
                } else {
                    openGeneralSettings()
                }
            }
            speechAuthStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusDenied -> {
                val speechSettingsUrl = NSURL.URLWithString("prefs:root=Privacy&path=SPEECH_RECOGNITION")
                if (speechSettingsUrl != null && UIApplication.sharedApplication.canOpenURL(speechSettingsUrl)) {
                    UIApplication.sharedApplication.openURL(
                        speechSettingsUrl,
                        mapOf<Any?, Any>(),
                        null
                    )
                } else {
                    openGeneralSettings()
                }
            }
            else -> {
                openGeneralSettings()
            }
        }
        dismissPermissionDialog()
    }

    private fun openGeneralSettings() {
        val generalSettingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (generalSettingsUrl != null) {
            UIApplication.sharedApplication.openURL(
                generalSettingsUrl,
                mapOf<Any?, Any>(),
                null
            )
        }
    }
}