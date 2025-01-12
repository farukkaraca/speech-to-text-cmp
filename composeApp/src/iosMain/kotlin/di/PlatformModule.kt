package di

import org.koin.dsl.module
import platform.SpeechToText

actual val platformModule = module {
    single { SpeechToText() }
} 