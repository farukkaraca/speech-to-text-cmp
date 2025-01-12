package di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import presentation.viewmodel.SpeechToTextViewModel

expect val platformModule: Module

val sharedModule = module {
    viewModel { SpeechToTextViewModel(get())}
}