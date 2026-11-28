package com.develop.pocketsplit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.pocketsplit.R
import com.develop.pocketsplit.model.User
import com.develop.pocketsplit.ui.components.DeleteAccountDialog
import com.develop.pocketsplit.ui.components.EditNameDialog
import com.develop.pocketsplit.ui.components.SettingsRow
import com.develop.pocketsplit.ui.theme.PocketSplitTheme
import com.develop.pocketsplit.ui.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onOpenAccount: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    onSignIn: () -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditName by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }

    SettingsContent(
        user = user,
        modifier = modifier,
        onOpenAccountClick = onOpenAccount,
        onEditNameClick = { viewModel.clearMessages(); showEditName = true },
        onLogoutClick = {
            viewModel.logout()
            onSignedOut()
        },
        onDeleteAccountClick = { viewModel.clearMessages(); showDelete = true },
        onSignInClick = onSignIn
    )

    if (showEditName) {
        EditNameDialog(
            currentName = user?.name.orEmpty(),
            isLoading = state.isLoading,
            error = state.error,
            onConfirm = { newName -> viewModel.updateName(newName) { showEditName = false } },
            onDismiss = { viewModel.clearMessages(); showEditName = false }
        )
    }

    if (showDelete) {
        DeleteAccountDialog(
            isLoading = state.isLoading,
            error = state.error,
            onConfirm = { password ->
                viewModel.deleteAccount(password) {
                    showDelete = false
                    onSignedOut()
                }
            },
            onDismiss = { viewModel.clearMessages(); showDelete = false }
        )
    }
}

@Composable
fun SettingsContent(
    user: User?,
    modifier: Modifier = Modifier,
    onOpenAccountClick: () -> Unit = {},
    onEditNameClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.settings_account),
            style = MaterialTheme.typography.titleMedium
        )

        if (user == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = stringResource(R.string.settings_guest_message),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_sign_in))
            }
        } else {
            SettingsRow(
                title = user.name.ifBlank { stringResource(R.string.settings_user_fallback_name) },
                supportingText = user.email,
                onClick = onOpenAccountClick
            )
            OutlinedButton(onClick = onEditNameClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_edit_name))
            }
            OutlinedButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_logout))
            }
            TextButton(onClick = onDeleteAccountClick, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.settings_delete_account),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    PocketSplitTheme {
        SettingsContent(user = User("1", "Ana", "ana@correo.com"))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenGuestPreview() {
    PocketSplitTheme { SettingsContent(user = null) }
}