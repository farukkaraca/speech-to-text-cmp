import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import presentation.components.AppContent
import presentation.viewmodel.SpeechToTextViewModel
import theme.AppTheme

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App(
    darkTheme: Boolean,
    dynamicColor: Boolean,
) {
    val viewModel = koinViewModel<SpeechToTextViewModel>()
    val transcriptState = viewModel.transcriptState.collectAsState()
    val uiEvent = viewModel.uiEvent.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent.value) {
        uiEvent.value?.let { event ->
            when (event) {
                is SpeechToTextViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    viewModel.onUiEventHandled()
                }
            }
        }
    }

    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
    ) {
        AppContent(
            snackbarHostState = snackbarHostState,
            transcriptState = transcriptState.value,
            onLanguageSelected = viewModel::onLanguageSelected,
            onClickMic = viewModel::onClickMic,
            onClickCopy = viewModel::onClickCopy,

            permissionDialogEvents = object : PermissionDialogEvents {
                override fun onDismissRequest() {
                    viewModel.onDismissRequest()
                }

                override fun onClickGoToSettings() {
                    viewModel.openAppSettings()
                }
            }
        )
    }
}

interface PermissionDialogEvents {
    fun onDismissRequest()
    fun onClickGoToSettings()
}