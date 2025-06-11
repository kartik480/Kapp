package com.kurakulas.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurakulas.app.ui.viewmodel.ProfileViewModel
import com.kurakulas.app.ui.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
        ) {
            // Back button under top navbar
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            when (uiState) {
                is ProfileUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProfileUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as ProfileUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ProfileUiState.Success -> {
                    val userData = (uiState as ProfileUiState.Success).profile
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Basic Information Section
                        SectionCard(title = "Basic Information") {
                            InfoRow("Username", userData.username ?: "")
                            InfoRow("First Name", userData.firstName ?: "")
                            InfoRow("Last Name", userData.lastName ?: "")
                            InfoRow("Mobile", userData.mobile ?: "")
                            InfoRow("Email", userData.emailId ?: "")
                            InfoRow("Date of Birth", userData.dob ?: "")
                            InfoRow("Employee No", userData.employeeNo ?: "")
                            InfoRow("Father's Name", userData.fatherName ?: "")
                            InfoRow("Joining Date", userData.joiningDate ?: "")
                            InfoRow("Department", userData.departmentId ?: "")
                            InfoRow("Designation", userData.designationId ?: "")
                        }

                        // Address Information Section
                        SectionCard(title = "Address Information") {
                            InfoRow("Present Address", userData.presentAddress ?: "")
                            InfoRow("Permanent Address", userData.permanentAddress ?: "")
                        }

                        // Personal Details Section
                        SectionCard(title = "Personal Details") {
                            InfoRow("Status", userData.status ?: "")
                            InfoRow("Rank", userData.rank ?: "")
                            InfoRow("Height", userData.height ?: "")
                            InfoRow("Weight", userData.weight ?: "")
                            InfoRow("Blood Group", userData.bloodGroup ?: "")
                            InfoRow("Languages", userData.languages ?: "")
                            InfoRow("Hobbies", userData.hobbies ?: "")
                        }

                        // Passport Information Section
                        SectionCard(title = "Passport Information") {
                            InfoRow("Passport No", userData.passportNo ?: "")
                            InfoRow("Passport Valid Until", userData.passportValid ?: "")
                        }

                        // Emergency Contact Section
                        SectionCard(title = "Emergency Contact") {
                            InfoRow("Emergency Number", userData.emergencyNo ?: "")
                            InfoRow("Emergency Address", userData.emergencyAddress ?: "")
                        }

                        // References Section
                        SectionCard(title = "References") {
                            InfoRow("Reference 1 Name", userData.referenceName ?: "")
                            InfoRow("Reference 1 Relation", userData.referenceRelation ?: "")
                            InfoRow("Reference 1 Mobile", userData.referenceMobile ?: "")
                            InfoRow("Reference 1 Address", userData.referenceAddress ?: "")
                            InfoRow("Reference 2 Name", userData.referenceName2 ?: "")
                            InfoRow("Reference 2 Relation", userData.referenceRelation2 ?: "")
                            InfoRow("Reference 2 Mobile", userData.referenceMobile2 ?: "")
                            InfoRow("Reference 2 Address", userData.referenceAddress2 ?: "")
                        }

                        // Bank Details Section
                        SectionCard(title = "Bank Details") {
                            InfoRow("Account Holder Name", userData.accHolderName ?: "")
                            InfoRow("Bank Name", userData.bankName ?: "")
                            InfoRow("Branch Name", userData.branchName ?: "")
                            InfoRow("Account Number", userData.accountNumber ?: "")
                            InfoRow("IFSC Code", userData.ifscCode ?: "")
                        }

                        // Work Information Section
                        SectionCard(title = "Work Information") {
                            InfoRow("Reporting To", userData.reportingTo ?: "")
                            InfoRow("Official Phone", userData.officialPhone ?: "")
                            InfoRow("Official Email", userData.officialEmail ?: "")
                            InfoRow("Work State", userData.workState ?: "")
                            InfoRow("Work Location", userData.workLocation ?: "")
                            InfoRow("Last Working Date", userData.lastWorkingDate ?: "")
                            InfoRow("Leaving Reason", userData.leavingReason ?: "")
                            InfoRow("Re-joining Date", userData.reJoiningDate ?: "")
                            InfoRow("PF Number", userData.pfNumber ?: "")
                            InfoRow("ESI Number", userData.esiNumber ?: "")
                        }
                    }
                }
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 