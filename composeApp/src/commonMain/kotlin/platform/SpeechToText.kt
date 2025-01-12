package platform

import data.PermissionRequestStatus
import data.TranscriptState
import kotlinx.coroutines.flow.MutableStateFlow

expect class SpeechToText {
    val transcriptState: MutableStateFlow<TranscriptState>
    fun startTranscribing()
    fun stopTranscribing()
    fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit)
    fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit)
    fun setLanguage(languageCode: String)
    fun copyText(text: String)
}