package com.example.compound_interest_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompoundInterestCalculatorScreen()
        }
    }
}

@Composable
fun CompoundInterestCalculatorScreen() {
    var principalInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var yearsInput by remember { mutableStateOf("") }
    var monthlyContributionInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Calculadora de Interés Compuesto", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = principalInput,
            onValueChange = {
                principalInput = it
                showError = false
            },
            label = { Text("Cantidad Inicial") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && principalInput.isBlank(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = monthlyContributionInput,
            onValueChange = {
                monthlyContributionInput = it
                showError = false
            },
            label = { Text("Contribución Mensual") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && monthlyContributionInput.isBlank(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = rateInput,
            onValueChange = {
                rateInput = it
                showError = false
            },
            label = { Text("Tasa de Interés Anual (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && rateInput.isBlank(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = yearsInput,
            onValueChange = {
                yearsInput = it
                showError = false
            },
            label = { Text("Años") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && yearsInput.isBlank(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (showError) {
            Text(
                text = "Por favor, rellena todos los campos.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(onClick = {
            val principal = principalInput.toDoubleOrNull()
            val annualRate = rateInput.toDoubleOrNull()
            val years = yearsInput.toIntOrNull()
            val monthlyContribution = monthlyContributionInput.toDoubleOrNull()

            if (principal == null || annualRate == null || years == null || monthlyContribution == null) {
                showError = true
                resultText = ""
            } else {
                showError = false
                val monthlyRate = annualRate / 100 / 12
                val numberOfMonths = years * 12

                // Calcular el valor futuro del principal inicial
                val futureValueOfPrincipal = principal * (1 + monthlyRate).pow(numberOfMonths)

                // Calcular el valor futuro de las contribuciones mensuales (anualidad)
                val futureValueOfAnnuity = if (monthlyRate > 0) {
                    monthlyContribution * (((1 + monthlyRate).pow(numberOfMonths) - 1) / monthlyRate)
                } else {
                    // Si la tasa es 0, simplemente es la suma de las contribuciones
                    monthlyContribution * numberOfMonths
                }

                val totalAmount = futureValueOfPrincipal + futureValueOfAnnuity
                val currencyFormat =
                    NumberFormat.getCurrencyInstance() // Puedes localizarlo si es necesario, ej: Locale("es", "ES")
                resultText = "Cantidad Final: ${currencyFormat.format(totalAmount)}"
            }
        }) {
            Text("Calcular")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (resultText.isNotEmpty()) {
            Text(text = resultText, style = MaterialTheme.typography.titleMedium)
        }
    }
}