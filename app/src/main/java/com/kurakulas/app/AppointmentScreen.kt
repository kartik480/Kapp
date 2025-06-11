package pzn

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen() {
    var showAddAppointment by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Appointments") },
                    actions = {
                        IconButton(onClick = { showAddAppointment = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Appointment")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Your existing appointment list content here
                
                // Show Add Appointment Panel when showAddAppointment is true
                if (showAddAppointment) {
                    AddAppointmentPanel(
                        onNavigateBack = { showAddAppointment = false },
                        context = context
                    )
                }
            }
        }
    }
} 
