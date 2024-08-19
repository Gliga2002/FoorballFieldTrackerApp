package com.example.footballfieldtracker.ui.layout.screens.mapscreen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.footballfieldtracker.ui.layout.util.ProfileImage
import com.example.footballfieldtracker.ui.viewmodels.MarkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldDialog(
    context: Context,
    // Todo: promeni ime
    mapViewModel: MarkerViewModel,
    onDismiss: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    val options = listOf(
        "Gradska Liga",
        "Okruzna Liga",
        "Zona Zapad",
        "Zona Istok",
        "Sprska Liga Istok",
        "Prva Liga"
    )


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add field") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = mapViewModel.address)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mapViewModel.name,
                    onValueChange = { mapViewModel.name = it },
                    label = { Text("Enter Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))


                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = mapViewModel.selectedOption,
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
                                    mapViewModel.selectedOption = option
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

                Spacer(modifier = Modifier.height(16.dp))

                ProfileImage(mapViewModel.imageUri) { newUri ->
                    mapViewModel.imageUri = newUri // Ažurira sliku kada korisnik izabere novu
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                mapViewModel.saveData() { success, errorMsg ->
                    if (success) {
                        mapViewModel.resetState()
                        onDismiss()
                    } else {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }

                }

            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}