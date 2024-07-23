sealed class RecognizerError(message: String) : Exception(message) {
    object NilRecognizer : RecognizerError("Can't initialize speech recognizer")
    object NotAuthorizedToRecognize : RecognizerError("Not authorized to recognize speech")
    object NotPermittedToRecord : RecognizerError("Not permitted to record audio")
    object RecognizerIsUnavailable : RecognizerError("Recognizer is unavailable")
}

