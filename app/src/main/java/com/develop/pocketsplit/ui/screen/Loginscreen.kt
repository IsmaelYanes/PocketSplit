package com.develop.pocketsplit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.pocketsplit.R
import com.develop.pocketsplit.ui.theme.PocketSplitTheme
import com.develop.pocketsplit.ui.viewmodel.AuthUiState
import com.develop.pocketsplit.ui.viewmodel.AuthViewModel
import com.develop.pocketsplit.ui.viewmodel.messageRes

/** Inicio de sesión / registro con Firebase Authentication. */
@Composable
fun LoginScreen(
    onContinue: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        state = state,
        modifier = modifier,
        onSubmit = { isRegister, name, email, password ->
            if (isRegister) viewModel.register(name, email, password, onContinue)
            else viewModel.login(email, password, onContinue)
        },
        onForgotPassword = viewModel::sendPasswordReset,
        onInputChanged = viewModel::clearMessages,
        onContinueAsGuest = onContinueAsGuest
    )
}

@Composable
fun LoginContent(
    state: AuthUiState,
    modifier: Modifier = Modifier,
    onSubmit: (isRegister: Boolean, name: String, email: String, password: String) -> Unit = { _, _, _, _ -> },
    onForgotPassword: (email: String) -> Unit = {},
    onInputChanged: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {}
) {
    var isRegister by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // La contraseña no se guarda en el estado persistente de la instancia.
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                if (isRegister) R.string.auth_title_register else R.string.auth_title_login
            ),
            style = MaterialTheme.typography.headlineMedium
        )

        if (isRegister) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; onInputChanged() },
                label = { Text(stringResource(R.string.auth_name)) },
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; onInputChanged() },
            label = { Text(stringResource(R.string.login_email)) },
            singleLine = true,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onInputChanged() },
            label = { Text(stringResource(R.string.login_password)) },
            singleLine = true,
            enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let {
            Text(
                text = stringResource(it.messageRes()),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (state.passwordResetSent) {
            Text(
                text = stringResource(R.string.auth_reset_sent),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = { onSubmit(isRegister, name, email, password) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    stringResource(
                        if (isRegister) R.string.auth_submit_register else R.string.auth_submit_login
                    )
                )
            }
        }

        if (!isRegister) {
            TextButton(
                onClick = { onForgotPassword(email) },
                enabled = !state.isLoading
            ) {
                Text(stringResource(R.string.auth_forgot_password))
            }
        }

        TextButton(
            onClick = { isRegister = !isRegister; onInputChanged() },
            enabled = !state.isLoading
        ) {
            Text(
                stringResource(
                    if (isRegister) R.string.auth_switch_to_login else R.string.auth_switch_to_register
                )
            )
        }

        OutlinedButton(
            onClick = onContinueAsGuest,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_continue_guest))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PocketSplitTheme { LoginContent(state = AuthUiState()) }
}