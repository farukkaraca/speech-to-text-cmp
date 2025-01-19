package data

data class TranscriptState(
    val listeningStatus: ListeningStatus,
    val error: Error = Error(),
    val transcript: String? = null,
    val selectedLanguage: String = DEFAULT_LANGUAGE,
    val supportedLanguages: List<String> = listOf(),
    val showPermissionNeedDialog: Boolean = false,
) {
    companion object {
        const val DEFAULT_LANGUAGE = "en-US"
    }
}

data class Error(
    val isError: Boolean = false,
    val message: String? = null,
)