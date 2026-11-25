package com.develop.pocketsplit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.develop.pocketsplit.R
import com.develop.pocketsplit.model.AuthError
import com.develop.pocketsplit.ui.viewmodel.messageRes

/** Cambio de contraseña: pide la actual (reautenticación) y la nueva. */
@Composable
fun ChangePasswordDialog(
    isLoading: Boolean,
    error: AuthError?,
    onConfirm: (currentPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.account_change_password_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text(stringResource(R.string.account_current_password)) },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text(stringResource(R.string.account_new_password)) },
                    singleLine = true,
                    enabled = !isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                error?.let {
                    Text(
                        text = stringResource(it.messageRes()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current, new) }, enabled = !isLoading) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}