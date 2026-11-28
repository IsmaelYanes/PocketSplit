package com.develop.pocketsplit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.pocketsplit.R
import com.develop.pocketsplit.model.User
import com.develop.pocketsplit.ui.components.ChangeEmailDialog
import com.develop.pocketsplit.ui.components.ChangePasswordDialog
import com.develop.pocketsplit.ui.components.DeleteAccountDialog
import com.develop.pocketsplit.ui.components.EditNameDialog
import com.develop.pocketsplit.ui.components.SettingsHeader
import com.develop.pocketsplit.ui.components.SettingsRow
import com.develop.pocketsplit.ui.theme.PocketSplitTheme
import com.develop.pocketsplit.ui.viewmodel.AuthUiState
import com.develop.pocketsplit.ui.viewmodel.AuthViewModel

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al entrar: se limpian avisos antiguos y se recarga la cuenta (p. ej. correo ya confirmado).
    LaunchedEffect(Unit) {
        viewModel.clearMessages()
        viewModel.refreshAccount()
    }

    AccountContent(
        user = user,
        uiState = uiState,
        modifier = modifier,
        onBack = onBack,
        onUpdateName = { name, onDone ->
            viewModel.updateName(name, onSuccess = onDone)
        },
        onChangeEmail = { email, password, onDone ->
            viewModel.changeEmail(email, password, onSuccess = onDone)
        },
        onChangePassword = { current, new, onDone ->
            viewModel.changePassword(current, new, onSuccess = onDone)
        },
        onDeleteAccount = { password, onDone ->
            viewModel.deleteAccount(password) {
                onDone()
                onSignedOut()
            }
        },
        onClearMessages = viewModel::clearMessages
    )
}

@Composable
fun AccountContent(
    user: User?,
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onUpdateName: (name: String, onDone: () -> Unit) -> Unit = { _, _ -> },
    onChangeEmail: (email: String, password: String, onDone: () -> Unit) -> Unit = { _, _, _ -> },
    onChangePassword: (current: String, new: String, onDone: () -> Unit) -> Unit = { _, _, _ -> },
    onDeleteAccount: (password: String, onDone: () -> Unit) -> Unit = { _, _ -> },
    onClearMessages: () -> Unit = {}
) {
    var showEditNameDialog by rememberSaveable { mutableStateOf(false) }
    var showChangeEmailDialog by rememberSaveable { mutableStateOf(false) }
    var showChangePasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsHeader(
            title = stringResource(R.string.settings_account),
            onBack = onBack
        )

        if (user != null) {
            uiState.emailVerificationSentTo?.let {
                Notice(text = stringResource(R.string.account_email_verification_sent, it))
            }
            if (uiState.passwordChanged) {
                Notice(text = stringResource(R.string.account_password_changed))
            }

            // Sección: Datos personales
            Text(
                text = stringResource(R.string.account_personal_data),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            SettingsRow(
                title = stringResource(R.string.account_field_name),
                supportingText = user.name.ifBlank { stringResource(R.string.settings_user_fallback_name) },
                onClick = { onClearMessages(); showEditNameDialog = true }
            )

            SettingsRow(
                title = stringResource(R.string.account_field_email),
                supportingText = user.email,
                onClick = { onClearMessages(); showChangeEmailDialog = true }
            )

            SettingsRow(
                title = stringResource(R.string.account_field_password),
                supportingText = stringResource(R.string.account_password_mask),
                onClick = { onClearMessages(); showChangePasswordDialog = true }
            )

            // Sección: Zona de peligro
            Text(
                text = stringResource(R.string.account_danger_zone),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )

            SettingsRow(
                title = stringResource(R.string.settings_delete_account),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { onClearMessages(); showDeleteAccountDialog = true }
            )
        }
    }

    if (showEditNameDialog && user != null) {
        EditNameDialog(
            currentName = user.name,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onConfirm = { newName ->
                onUpdateName(newName) { showEditNameDialog = false }
            },
            onDismiss = {
                onClearMessages()
                showEditNameDialog = false
            }
        )
    }

    if (showChangeEmailDialog && user != null) {
        ChangeEmailDialog(
            currentEmail = user.email,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onConfirm = { newEmail, password ->
                onChangeEmail(newEmail, password) { showChangeEmailDialog = false }
            },
            onDismiss = {
                onClearMessages()
                showChangeEmailDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onConfirm = { current, new ->
                onChangePassword(current, new) { showChangePasswordDialog = false }
            },
            onDismiss = {
                onClearMessages()
                showChangePasswordDialog = false
            }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onConfirm = { password ->
                onDeleteAccount(password) { showDeleteAccountDialog = false }
            },
            onDismiss = {
                onClearMessages()
                showDeleteAccountDialog = false
            }
        )
    }
}

/** Aviso informativo; se anuncia a los lectores de pantalla al aparecer. */
@Composable
private fun Notice(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountScreenPreview() {
    PocketSplitTheme {
        AccountContent(
            user = User("1", "Ana", "ana@correo.com"),
            uiState = AuthUiState(passwordChanged = true)
        )
    }
}