package presentation.components

import PermissionDialogEvents
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import data.ListeningStatus
import data.TranscriptState

@Composable
fun AppContent(
    snackbarHostState: SnackbarHostState,
    transcriptState: TranscriptState,
    onLanguageSelected: (String) -> Unit,
    onClickMic: () -> Unit,
    onClickCopy: () -> Unit,
    permissionDialogEvents: PermissionDialogEvents
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    var selectedLanguage by remember {
        mutableStateOf(
            transcriptState.selectedLanguage
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            supportedLanguages = transcriptState.supportedLanguages,
            onSelected = {
                onLanguageSelected(it)
                selectedLanguage = it
                showLanguageDialog = false
            },
            onDismissRequest = {
                showLanguageDialog = false
            },
            selectedLanguage = selectedLanguage
        )
    }

    if (transcriptState.showPermissionNeedDialog) {
        PermissionNeedDialog(
            onDismissRequest = {permissionDialogEvents.onDismissRequest()},
            onClickGoToSettings = {permissionDialogEvents.onClickGoToSettings()}
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomBar(
                onClickShowLanguages = {
                    showLanguageDialog = true
                },
                onClickMic = {
                    onClickMic()
                },
                onClickCopy = {
                    onClickCopy()
                },
                isListening = transcriptState.listeningStatus == ListeningStatus.LISTENING
            )
        }
    ) { paddingValues ->
        Content(
            paddingValues = paddingValues,
            transcriptState = transcriptState,
        )
    }
}