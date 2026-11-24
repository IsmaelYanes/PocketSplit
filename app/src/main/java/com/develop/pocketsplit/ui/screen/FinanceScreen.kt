package com.develop.pocketsplit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.pocketsplit.R
import com.develop.pocketsplit.model.CategoryType
import com.develop.pocketsplit.ui.components.ConfirmDeleteDialog
import com.develop.pocketsplit.ui.components.RenameFinanceDialog
import com.develop.pocketsplit.ui.viewmodel.FinanceViewModel

@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = viewModel(),
    onBack: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

    val finance by viewModel.finance.collectAsStateWithLifecycle()
    val displayName = finance?.name ?: stringResource(R.string.finance_name_placeholder)
    var menuExpanded by remember { mutableStateOf(false) }
    var showRename by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }

    // Seleccionamos por defecto la primera categoría disponible
    val selectedCategory = viewModel.categories.firstOrNull()

    Column(
        modifier = modifier // 2. Usamos el modifier recibido del Scaffold
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera con flecha de volver, nombre de la finanza y menú de opciones
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.finance_back)
                )
            }
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.finance_more_options)
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.finance_rename)) },
                        onClick = { menuExpanded = false; showRename = true }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.finance_delete)) },
                        onClick = { menuExpanded = false; showDelete = true }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de Balance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.finance_total_balance),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "$${viewModel.currentBalance}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (viewModel.currentBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Formulario rápido para agregar transacción
        Text(
            text = stringResource(R.string.finance_add_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.finance_description_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text(stringResource(R.string.finance_amount_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { selectedType = CategoryType.INCOME },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == CategoryType.INCOME) Color.Green else Color.Gray)
            ) {
                Text(stringResource(R.string.finance_income))
            }
            Button(
                onClick = { selectedType = CategoryType.EXPENSE },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == CategoryType.EXPENSE) Color.Red else Color.Gray)
            ) {
                Text(stringResource(R.string.finance_expense))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount > 0 && description.isNotBlank() && selectedCategory != null) {
                    viewModel.addTransaction(
                        categoryId = selectedCategory.id,
                        amount = amount,
                        type = selectedType,
                        description = description
                    )
                    description = ""
                    amountText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.finance_save))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de transacciones recientes
        Text(
            text = stringResource(R.string.finance_recent_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(viewModel.transactions) { transaction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = transaction.description, style = MaterialTheme.typography.bodyLarge)
                            Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = "${if (transaction.type == CategoryType.INCOME) "+" else "-"}$${transaction.amount}",
                            color = if (transaction.type == CategoryType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
    if (showRename) {
        RenameFinanceDialog(
            currentName = finance?.name.orEmpty(),
            onDismiss = { showRename = false },
            onConfirm = { viewModel.renameFinance(it); showRename = false }
        )
    }

    if (showDelete) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.finance_delete_title),
            message = stringResource(R.string.finance_delete_message, displayName),
            onDismiss = { showDelete = false },
            onConfirm = { showDelete = false; viewModel.deleteFinance(onDeleted = onBack) }
        )
    }
}