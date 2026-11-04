package com.develop.pocketsplit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.develop.pocketsplit.model.CategoryType
import com.develop.pocketsplit.ui.viewmodel.FinanceViewModel

@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier, // 1. Añadimos el modifier aquí
    viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

    // Seleccionamos por defecto la primera categoría disponible
    val selectedCategory = viewModel.categories.firstOrNull()

    Column(
        modifier = modifier // 2. Usamos el modifier recibido del Scaffold
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título de la App
        Text(text = "PocketSplit - Finanzas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de Balance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Balance Total", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "$${viewModel.currentBalance}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (viewModel.currentBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Formulario rápido para agregar transacción
        Text(text = "Agregar Movimiento", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (ej. Café, Nómina)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Monto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { selectedType = CategoryType.INCOME },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == CategoryType.INCOME) Color.Green else Color.Gray)
            ) {
                Text("Ingreso")
            }
            Button(
                onClick = { selectedType = CategoryType.EXPENSE },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedType == CategoryType.EXPENSE) Color.Red else Color.Gray)
            ) {
                Text("Gasto")
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
            Text("Guardar Transacción")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de transacciones recientes
        Text(text = "Movimientos Recientes", style = MaterialTheme.typography.titleMedium)
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
}