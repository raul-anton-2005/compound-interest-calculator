package com.example.compound_interest_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import kotlin.math.pow

// Enum para representar la frecuencia de contribución
enum class ContributionFrequency(val displayName: String) {
    MONTHLY("Mensual"),
    ANNUALLY("Anual")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompoundInterestCalculatorScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Necesario para ExposedDropdownMenuBox
@Composable
fun CompoundInterestCalculatorScreen() {
    var principalInput by remember { mutableStateOf("") }
    var rateInput by remember { mutableStateOf("") }
    var yearsInput by remember { mutableStateOf("") }
    var contributionInput by remember { mutableStateOf("") } // Renombrado de monthlyContributionInput
    var resultText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Estado para el desplegable
    val contributionFrequencies = ContributionFrequency.values()
    var selectedFrequency by remember { mutableStateOf(ContributionFrequency.MONTHLY) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Calculadora de Interés Compuesto", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = principalInput,
            onValueChange = {
                principalInput = it
                showError = false
            },
            label = { Text("Cantidad Inicial (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && principalInput.isBlank(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = contributionInput,
                onValueChange = {
                    contributionInput = it
                    showError = false
                },
                label = { Text("Contribución (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = showError && contributionInput.isBlank(),
                singleLine = true,
                modifier = Modifier.weight(1f) // Ocupa el espacio disponible
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Desplegable para frecuencia de contribución
            ExposedDropdownMenuBox(
                expanded = isFrequencyDropdownExpanded,
                onExpandedChange = { isFrequencyDropdownExpanded = !isFrequencyDropdownExpanded },
                modifier = Modifier.weight(0.8f) // Ajusta el peso según necesites
            ) {
                OutlinedTextField(
                    value = selectedFrequency.displayName,
                    onValueChange = {}, // No editable directamente
                    readOnly = true,
                    label = { Text("Frecuencia") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFrequencyDropdownExpanded)
                    },
                    modifier = Modifier.menuAnchor() // Importante para anclar el menú
                )
                ExposedDropdownMenu(
                    expanded = isFrequencyDropdownExpanded,
                    onDismissRequest = { isFrequencyDropdownExpanded = false }
                ) {
                    contributionFrequencies.forEach { frequency ->
                        DropdownMenuItem(
                            text = { Text(frequency.displayName) },
                            onClick = {
                                selectedFrequency = frequency
                                isFrequencyDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))


        OutlinedTextField(
            value = rateInput,
            onValueChange = {
                rateInput = it
                showError = false
            },
            label = { Text("Tasa de Interés Anual (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && rateInput.isBlank(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
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
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (showError) {
            Text(
                text = "Por favor, rellena todos los campos.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                val principal = principalInput.toDoubleOrNull()
                val annualRatePercent = rateInput.toDoubleOrNull()
                val years = yearsInput.toIntOrNull()
                val contributionAmount = contributionInput.toDoubleOrNull()

                if (principal == null || annualRatePercent == null || years == null || contributionAmount == null ||
                    principalInput.isBlank() || rateInput.isBlank() || yearsInput.isBlank() || contributionInput.isBlank()) { // Chequeo adicional por isBlank
                    showError = true
                    resultText = ""
                } else {
                    showError = false
                    val annualRate = annualRatePercent / 100.0 // Tasa anual como decimal

                    // --- Lógica de cálculo ajustada ---
                    val periodsPerYear = if (selectedFrequency == ContributionFrequency.MONTHLY) 12 else 1
                    val totalPeriods = years * periodsPerYear
                    val ratePerPeriod = annualRate / periodsPerYear
                    val contributionPerPeriod = contributionAmount // La entrada es por periodo seleccionado

                    // Valor futuro del principal inicial
                    // El principal se capitaliza según la tasa anual, pero por el número total de años.
                    // Podríamos también componerlo por periodo si la tasa anual se dividiera y compusiera así.
                    // Para simplificar, la tasa de interés de la cuenta siempre es anual, y se aplica anualmente al principal.
                    // Las contribuciones, sin embargo, se añaden y luego crecen con esa tasa.
                    // Una forma más precisa sería calcular el interés compuesto período a período.
                    // Vamos a recalcularlo para que el interés se aplique consistentemente.
                    // Asumiremos que el interés de la CUENTA se compone anualmente, independientemente de la frecuencia de contribución.
                    // O que la tasa anual se divide para componerse con la misma frecuencia que las contribuciones.
                    // La segunda es más común para este tipo de calculadoras.

                    val compoundingPeriodsPerYear = 12 // Asumimos que el interés de la cuenta SIEMPRE se compone mensualmente para consistencia
                    // o se podría hacer que coincida con selectedFrequency. Por simplicidad, usemos mensual.
                    val accountMonthlyRate = annualRate / compoundingPeriodsPerYear
                    val totalCompoundingPeriodsForPrincipal = years * compoundingPeriodsPerYear


                    val futureValueOfPrincipal = principal * (1 + accountMonthlyRate).pow(totalCompoundingPeriodsForPrincipal)
                    var futureValueOfContributions = 0.0

                    if (ratePerPeriod > 0) {
                        if (selectedFrequency == ContributionFrequency.MONTHLY) {
                            // FV de Anualidad Ordinaria Mensual
                            futureValueOfContributions = contributionPerPeriod *
                                    (((1 + accountMonthlyRate).pow(totalCompoundingPeriodsForPrincipal) - 1) / accountMonthlyRate)
                        } else { // ANNUALLY
                            // Para contribuciones anuales, necesitamos sumar el valor futuro de cada contribución anual
                            // capitalizada por los años restantes.
                            for (i in 0 until years) {
                                futureValueOfContributions += contributionPerPeriod * (1 + annualRate).pow(years - 1 - i)
                            }
                            // O usando la fórmula de anualidad si el interés se compone anualmente también para estas:
                            // futureValueOfContributions = contributionPerPeriod * (((1 + annualRate).pow(years) - 1) / annualRate)
                            // La primera es más precisa si las contribuciones se hacen y luego la cuenta sigue capitalizando mensualmente.
                            // Para simplificar y alinear con la capitalización mensual de la cuenta:
                            // Consideramos cada contribución anual y la capitalizamos mensualmente por los periodos restantes.
                            var tempFvContributionsAnnual = 0.0
                            for (yearNum in 0 until years) {
                                // Contribución hecha al final del año 'yearNum' (o inicio del siguiente)
                                // Crecerá por (years - (yearNum+1)) * 12 periodos mensuales
                                val periodsRemainingForThisContribution = (years - (yearNum + 1)) * compoundingPeriodsPerYear
                                tempFvContributionsAnnual += contributionAmount * (1 + accountMonthlyRate).pow(periodsRemainingForThisContribution.toDouble())
                                if (yearNum < years) { // Sumar la contribución del año actual también (que no ha crecido aún si es al final)
                                    // Esto se complica. Usemos la fórmula de anualidad estándar y ajustemos la tasa y periodos.
                                }
                            }
                            // Opción más simple para contribuciones ANUALES con capitalización mensual de la cuenta:
                            // Tratar cada contribución anual como un lump sum que luego se capitaliza.
                            // O, si la tasa de interés anual se usa para la anualidad anual:
                            futureValueOfContributions = contributionPerPeriod * (((1 + annualRate).pow(years) - 1) / annualRate)
                            // Esto asume que la contribución anual se hace y se capitaliza anualmente a la tasa anual.
                            // Si la cuenta capitaliza mensualmente, es más complejo.
                            //
                            // **REVISIÓN DE LÓGICA PARA CONTRIBUCIÓN ANUAL CON CAPITALIZACIÓN MENSUAL DE LA CUENTA:**
                            // Si el interés de la cuenta se compone mensualmente (accountMonthlyRate):
                            // Una contribución anual 'C' hecha al final del año 1, crecerá por (N-1)*12 meses.
                            // La del año 2, por (N-2)*12 meses, etc.
                            // La última contribución al final del año N, no crece.
                            // FV = C(1+mr)^((N-1)12) + C(1+mr)^((N-2)12) + ... + C(1+mr)^0
                            // Esto es una serie geométrica.
                            // O, más simple: si la contribución es anual, pero la tasa de la cuenta es anual (compuesta anualmente), es directo:
                            // FV_anualidad = C * [((1+R)^T - 1) / R]
                            // Si la tasa de la cuenta es anual (compuesta mensualmente), y la contribución es anual:
                            // Es como si cada contribución anual fuera un "principal" que luego se compone mensualmente.
                            // Vamos a mantener la fórmula de anualidad estándar y ajustar r y n según la frecuencia:
                            // r = tasa por período de contribución, n = número total de períodos de contribución.
                            // La TASA DE INTERÉS DE LA CUENTA (annualRate) se usa para determinar la tasa por período de contribución.

                            val rateForContributionPeriod = if (selectedFrequency == ContributionFrequency.MONTHLY) annualRate / 12 else annualRate
                            val numberOfContributions = if (selectedFrequency == ContributionFrequency.MONTHLY) years * 12 else years

                            if (rateForContributionPeriod > 0) {
                                futureValueOfContributions = contributionAmount *
                                        (((1 + rateForContributionPeriod).pow(numberOfContributions) - 1) / rateForContributionPeriod)
                            } else {
                                futureValueOfContributions = contributionAmount * numberOfContributions
                            }
                        }

                    } else { // Tasa de interés es 0
                        val numberOfContributionsTotal = if (selectedFrequency == ContributionFrequency.MONTHLY) years * 12 else years
                        futureValueOfContributions = contributionAmount * numberOfContributionsTotal
                    }

                    // El principal inicial se capitaliza según la tasa de la cuenta (mensual)
                    val effectivePrincipalFutureValue = principal * (1 + (annualRate/12.0)).pow(years * 12.0)


                    val totalAmount = effectivePrincipalFutureValue + futureValueOfContributions
                    val currencyFormat = NumberFormat.getCurrencyInstance()
                    resultText = "Cantidad Final: ${currencyFormat.format(totalAmount)}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (resultText.isNotEmpty()) {
            Text(text = resultText, style = MaterialTheme.typography.titleMedium)
        }
    }
}
