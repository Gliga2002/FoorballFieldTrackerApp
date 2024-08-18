package com.example.footballfieldtracker.ui.layout.screens.mapscreen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

// Todo: Odvoj ovo u poseban composable
@Composable
fun ServiceControlDialog(
    isServiceRunning: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nearby Fields Detection") },
        text = {
            Text(
                text = """
                    Are you sure you want to ${if (isServiceRunning) "stop" else "start"} the location tracking service?
                    ${if (isServiceRunning)
                    "This action will stop monitoring your location and you will no longer receive notifications about nearby objects."
                else
                    "This will begin monitoring your current location and you'll receive notifications if any objects come within your vicinity."
                }   
                        """.trimIndent(),
                textAlign = TextAlign.Left
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(if (isServiceRunning) "Stop Tracking" else "Start Tracking")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}