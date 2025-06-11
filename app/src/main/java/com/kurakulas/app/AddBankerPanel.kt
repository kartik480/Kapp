package pzn

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ContentResolver
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import pzn.api.BankerRequest
import pzn.api.RetrofitClient
import com.kurakulas.app.data.api.ApiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBankerPanel(
    onNavigateBack: () -> Unit,
    context: Context
) {
    // Helper function for file handling
    fun getBankerFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        }
    }

    fun uriToFile(context: Context, uri: Uri, fileName: String?): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, fileName ?: "temp_file")
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    var vendorBank by remember { mutableStateOf("") }
    var bankerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailId by remember { mutableStateOf("") }
    var bankerDesignation by remember { mutableStateOf("") }
    var loanType by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var visitingCardUri by remember { mutableStateOf<Uri?>(null) }
    var visitingCardFileName by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // File picker launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            visitingCardUri = it
            visitingCardFileName = getBankerFileName(context.contentResolver, it)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Banker",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = vendorBank,
                onValueChange = { vendorBank = it },
                label = { Text("Vendor Bank") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = bankerName,
                onValueChange = { bankerName = it },
                label = { Text("Banker Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 10) phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = emailId,
                onValueChange = { emailId = it },
                label = { Text("Email ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = bankerDesignation,
                onValueChange = { bankerDesignation = it },
                label = { Text("Designation") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = loanType,
                onValueChange = { loanType = it },
                label = { Text("Loan Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                minLines = 3
            )

            // Visiting Card Section
            Text(
                text = "Visiting Card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = visitingCardFileName ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Visiting Card") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { launcher.launch("application/pdf,image/*") }) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        }
                    }
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (vendorBank.isBlank() || bankerName.isBlank() || phoneNumber.isBlank() || 
                        emailId.isBlank() || bankerDesignation.isBlank() || loanType.isBlank() || 
                        state.isBlank() || location.isBlank() || address.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        try {
                            Log.d("BankerAPI", "=== Starting Banker Submission ===")
                            Log.d("BankerAPI", "Base URL: ${ApiConfig.BASE_URL}")
                            
                            // Create banker request
                            val request = BankerRequest(
                                vendor_bank = vendorBank,
                                banker_name = bankerName,
                                phone_number = phoneNumber,
                                email_id = emailId,
                                banker_designation = bankerDesignation,
                                loan_type = loanType,
                                state = state,
                                location = location,
                                visiting_card = "",
                                address = address
                            )
                            
                            Log.d("BankerAPI", "Request data: $request")
                            Log.d("BankerAPI", "Full URL: ${ApiConfig.BASE_URL}add_banker.php")
                            
                            val response = RetrofitClient.apiService.addBanker(request)
                            Log.d("BankerAPI", "Response code: ${response.code()}")
                            Log.d("BankerAPI", "Response headers: ${response.headers()}")
                            
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                Log.d("BankerAPI", "Response body: $responseBody")
                                
                                if (responseBody?.success == true) {
                                    Log.d("BankerAPI", "Banker added successfully")
                                    
                                    // Handle visiting card upload if selected
                                    visitingCardUri?.let { uri ->
                                        try {
                                            Log.d("BankerAPI", "Starting visiting card upload...")
                                            val file = uriToFile(context, uri, visitingCardFileName ?: "visiting_card")
                                            
                                            val requestBody = MultipartBody.Builder()
                                                .setType(MultipartBody.FORM)
                                                .addFormDataPart(
                                                    "visiting_card",
                                                    file.name,
                                                    file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                                                )
                                                .build()
                                            
                                            Log.d("BankerAPI", "Uploading visiting card: ${file.name}")
                                            val uploadResponse = RetrofitClient.apiService.uploadBankerVisitingCard(requestBody)
                                            Log.d("BankerAPI", "Upload response code: ${uploadResponse.code()}")
                                            Log.d("BankerAPI", "Upload response body: ${uploadResponse.body()}")
                                            
                                        } catch (e: Exception) {
                                            Log.e("BankerAPI", "Error uploading visiting card", e)
                                        }
                                    }

                                    Toast.makeText(context, "Banker added successfully", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                } else {
                                    val errorMessage = responseBody?.message ?: "Unknown error occurred"
                                    Log.e("BankerAPI", "API Error: $errorMessage")
                                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("BankerAPI", "Error response code: ${response.code()}")
                                Log.e("BankerAPI", "Error response body: $errorBody")
                                Toast.makeText(context, "Failed to add banker: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("BankerAPI", "Exception occurred", e)
                            Log.e("BankerAPI", "Stack trace: ${e.stackTraceToString()}")
                            val errorMessage = when (e) {
                                is org.json.JSONException -> "Invalid response from server"
                                else -> e.message ?: "Unknown error occurred"
                            }
                            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Add Banker")
            }
        }
    }
} 
