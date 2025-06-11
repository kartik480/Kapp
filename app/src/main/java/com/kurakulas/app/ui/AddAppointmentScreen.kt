package com.kurakulas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurakulas.app.ui.viewmodel.AddAppointmentViewModel
import com.kurakulas.app.data.model.State
import com.kurakulas.app.data.model.Location
import com.kurakulas.app.data.model.Sublocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddAppointmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val states by viewModel.states.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val sublocations by viewModel.sublocations.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedSublocation by viewModel.selectedSublocation.collectAsState()
    var showFileFormatDialog by remember { mutableStateOf(false) }

    var expandedState by remember { mutableStateOf(false) }
    var expandedLocation by remember { mutableStateOf(false) }
    var expandedSublocation by remember { mutableStateOf(false) }

    if (showFileFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFileFormatDialog = false },
            title = { Text("Select File Format") },
            text = {
                Column {
                    listOf("JPEG", "PNG", "PDF").forEach { format ->
                        TextButton(
                            onClick = {
                                // TODO: Implement file picker for selected format
                                showFileFormatDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(format)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFileFormatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Appointment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // State Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedState,
                onExpandedChange = { expandedState = it }
            ) {
                OutlinedTextField(
                    value = selectedState?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("State") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedState,
                    onDismissRequest = { expandedState = false }
                ) {
                    states.forEach { state ->
                        DropdownMenuItem(
                            text = { Text(state.name) },
                            onClick = {
                                viewModel.onStateSelected(state)
                                expandedState = false
                            }
                        )
                    }
                }
            }

            // Location Dropdown - Only show if locations exist for selected state
            if (selectedState != null && locations.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandedLocation,
                    onExpandedChange = { expandedLocation = it }
                ) {
                    OutlinedTextField(
                        value = selectedLocation?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLocation,
                        onDismissRequest = { expandedLocation = false }
                    ) {
                        locations.forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location.name) },
                                onClick = {
                                    viewModel.onLocationSelected(location)
                                    expandedLocation = false
                                }
                            )
                        }
                    }
                }
            }

            // Sublocation Dropdown - Only show if sublocations exist for selected location
            if (selectedLocation != null && sublocations.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandedSublocation,
                    onExpandedChange = { expandedSublocation = it }
                ) {
                    OutlinedTextField(
                        value = selectedSublocation?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sublocation") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSublocation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedSublocation,
                        onDismissRequest = { expandedSublocation = false }
                    ) {
                        sublocations.forEach { sublocation ->
                            DropdownMenuItem(
                                text = { Text(sublocation.name) },
                                onClick = {
                                    viewModel.onSublocationSelected(sublocation)
                                    expandedSublocation = false
                                }
                            )
                        }
                    }
                }
            }

            // Basic Information Section
            SectionCard(title = "Basic Information") {
                OutlinedTextField(
                    value = state.mobileNumber,
                    onValueChange = { viewModel.updateMobileNumber(it) },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.emailId,
                    onValueChange = { viewModel.updateEmailId(it) },
                    label = { Text("Email ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bank Account Details Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Bank Account Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Add Button
                    Button(
                        onClick = { viewModel.addBankAccount() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Bank Account",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Table Header
                    TableHeader(
                        columns = listOf(
                            "Bank Name" to 1f,
                            "Account Type" to 1f,
                            "Account Number" to 1f,
                            "Branch Name" to 1f,
                            "IFSC Code" to 1f,
                            "Action" to 0.5f
                        )
                    )
                }
            }

            // Relationship with Bank Section
            SectionCard(title = "Relationship with Bank") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loan Details",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { viewModel.addLoanDetails() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                // Loan Details Table Header
                TableHeader(
                    columns = listOf(
                        "Bank Name" to 1f,
                        "Loan Type" to 1f,
                        "Loan Amount" to 1f,
                        "ROI" to 1f,
                        "Tenure" to 1f,
                        "Action" to 0.5f
                    )
                )
            }

            // Vehicle Details Section
            SectionCard(title = "Vehicle Details") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vehicles",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { viewModel.addVehicleDetails() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                // Vehicle Details Table Header
                TableHeader(
                    columns = listOf(
                        "Vehicle Number" to 1f,
                        "Make" to 1f,
                        "Model" to 1f,
                        "Year" to 1f,
                        "Engine No." to 1f,
                        "Action" to 0.5f
                    )
                )
            }

            // Property Details Section
            SectionCard(title = "Property Details") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Properties",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { viewModel.addPropertyDetails() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                // Property Details Table Header
                TableHeader(
                    columns = listOf(
                        "Property Type" to 1f,
                        "Area" to 1f,
                        "Land Area" to 1f,
                        "SFT" to 1f,
                        "Market Value" to 1f,
                        "Action" to 0.5f
                    )
                )
            }

            // Credit Card Details Section
            SectionCard(title = "Credit Card Details") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Credit Cards",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { viewModel.addCreditCardDetails() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                // Credit Card Details Table Header
                TableHeader(
                    columns = listOf(
                        "Bank Name" to 1f,
                        "Credit Limit" to 1f,
                        "Action" to 0.5f
                    )
                )
            }

            // Appointment Details Section
            Text(
                text = "Appointment Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Appointment Bank Dropdown
                var expandedAppointmentBank by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAppointmentBank,
                    onExpandedChange = { expandedAppointmentBank = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedAppointmentBank,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Appointment Bank") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAppointmentBank) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAppointmentBank,
                        onDismissRequest = { expandedAppointmentBank = false }
                    ) {
                        listOf("SBI", "HDFC", "ICICI", "Axis", "PNB", "Kotak", "Citi").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateAppointmentBank(option)
                                    expandedAppointmentBank = false
                                }
                            )
                        }
                    }
                }

                // Appointment Product Dropdown
                var expandedAppointmentProduct by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAppointmentProduct,
                    onExpandedChange = { expandedAppointmentProduct = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedAppointmentProduct,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Appointment Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAppointmentProduct) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAppointmentProduct,
                        onDismissRequest = { expandedAppointmentProduct = false }
                    ) {
                        listOf("Home Loan", "Car Loan", "Personal Loan", "Business Loan", "Credit Card").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateAppointmentProduct(option)
                                    expandedAppointmentProduct = false
                                }
                            )
                        }
                    }
                }

                // Appointment Status Dropdown
                var expandedAppointmentStatus by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAppointmentStatus,
                    onExpandedChange = { expandedAppointmentStatus = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedAppointmentStatus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Appointment Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAppointmentStatus) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAppointmentStatus,
                        onDismissRequest = { expandedAppointmentStatus = false }
                    ) {
                        listOf("Scheduled", "Completed", "Cancelled", "Rescheduled", "No Show").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateAppointmentStatus(option)
                                    expandedAppointmentStatus = false
                                }
                            )
                        }
                    }
                }

                // Appointment Sub Status Dropdown
                var expandedAppointmentSubStatus by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAppointmentSubStatus,
                    onExpandedChange = { expandedAppointmentSubStatus = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedAppointmentSubStatus,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Appointment Sub Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAppointmentSubStatus) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAppointmentSubStatus,
                        onDismissRequest = { expandedAppointmentSubStatus = false }
                    ) {
                        listOf("Pending", "In Progress", "Approved", "Rejected", "On Hold").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.updateAppointmentSubStatus(option)
                                    expandedAppointmentSubStatus = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.appointmentNote,
                    onValueChange = { viewModel.updateAppointmentNote(it) },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun TableHeader(columns: List<Pair<String, Float>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        columns.forEach { (text, weight) ->
            Text(
                text = text,
                modifier = Modifier.weight(weight),
                fontWeight = FontWeight.Bold
            )
        }
    }
} 
