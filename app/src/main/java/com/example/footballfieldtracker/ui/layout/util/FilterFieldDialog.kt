package com.example.footballfieldtracker.ui.layout.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footballfieldtracker.data.model.LocationData
import com.example.footballfieldtracker.data.model.User
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterFieldDialog(
    context: Context,
    currentUserLocation: LocationData,
    markerViewModel: MarkerViewModel,
    onDismiss: () -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }


    // Show the DateRangePicker within the main dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter fields") },
        text = {
            Column {
                // Author input
                OutlinedTextField(
                    value = markerViewModel.filteredName,
                    onValueChange = { markerViewModel.filteredName = it },
                    label = { Text("Author") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Type spinner
                val options = listOf(
                    "Any Type",
                    "Gradska Liga",
                    "Okruzna Liga",
                    "Zona Zapad",
                    "Zona Istok",
                    "Sprska Liga Istok",
                    "Prva Liga"
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = markerViewModel.filteredSelectedOption,
                        onValueChange = { /* No-op */ },
                        label = { Text("Select Option") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    markerViewModel.filteredSelectedOption = option
                                    expanded = false
                                },
                                text = {
                                    Text(option)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

         
                // Todo: Ovo da ne prikazes ako nije dozvolio lokaciju!!
                // Radius slider
                Text("Radius: ${markerViewModel.filteredRadius} km")
                Slider(
                    value = markerViewModel.filteredRadius.toFloat(),
                    onValueChange = { markerViewModel.filteredRadius = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 5 // Number of steps in the slider
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "${markerViewModel.dateRange}")

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(onClick = { showDatePicker = true }) {
                        Text(text = "SetDateRange")
                    }
                }

                // Date Range picker
                if (showDatePicker) {
                    DatePickerDialog(
                        setDateRange = {markerViewModel.dateRange = it}
                    ) {
                        showDatePicker = false
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    markerViewModel.applyFilters(currentUserLocation) { isFound ->
                       if (isFound) {
                           markerViewModel.resetFilter()
                           onDismiss()
                           Toast.makeText(context, "Filter successfully applied.", Toast.LENGTH_SHORT).show()
                       } else {
                           Toast.makeText(context, "We were unable to find any data for the requested field filter.", Toast.LENGTH_SHORT).show()
                       }
                    }

                }
            ) {
                Text("Filter")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

}