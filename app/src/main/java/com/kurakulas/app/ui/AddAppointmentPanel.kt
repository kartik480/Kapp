package com.kurakulas.app.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import pzn.api.RetrofitClient
import pzn.api.AppointmentRequest
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ContentResolver
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Add
import pzn.api.BankAccountRequest
import pzn.api.BankAccountResponse
import com.kurakulas.app.ui.models.BankAccountDetails
import pzn.api.RelationshipWithBankRequest
import com.kurakulas.app.ui.models.RelationshipWithBankDetails

// Helper functions for file handling
private fun getAppointmentFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameIndex)
    }
}

private fun getFileExtension(contentResolver: ContentResolver, uri: Uri): String? {
    val mime = MimeTypeMap.getSingleton()
    return contentResolver.getType(uri)?.let { mime.getExtensionFromMimeType(it) }
}

private fun uriToFile(context: Context, uri: Uri, fileName: String?): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName ?: "temp_file")
    FileOutputStream(file).use { outputStream ->
        inputStream?.copyTo(outputStream)
    }
    return file
}

class AddAppointmentPanel(private val context: Context) {

    private var bankName = ""
    private var accountType = ""
    private var accountNumber = ""
    private var branchName = ""
    private var ifscCode = ""
    private var bankAccounts = mutableListOf<BankAccountDetails>()

    private var relationshipBankName = ""
    private var loanType = ""
    private var loanAmount = ""
    private var roi = ""
    private var tenure = ""
    private var emi = ""
    private var firstEmiDate = ""
    private var lastEmiDate = ""
    private var loanAccountName = ""
    private var relationshipWithBanks = mutableListOf<RelationshipWithBankDetails>()

    fun addBankAccount(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                Log.d("BankAccountAPI", "Starting bank account addition...")
                val request = BankAccountRequest(
                    b_bank_name = bankName,
                    b_account_type = accountType,
                    b_account_no = accountNumber,
                    b_branch_name = branchName,
                    b_ifsc_code = ifscCode
                )
                Log.d("BankAccountAPI", "Request data: $request")
                
                val response = RetrofitClient.apiService.addBankAccount(request)
                Log.d("BankAccountAPI", "Response code: ${response.code()}")
                Log.d("BankAccountAPI", "Response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    // Clear the input fields
                    bankName = ""
                    accountType = ""
                    accountNumber = ""
                    branchName = ""
                    ifscCode = ""
                    
                    // Add to the list of bank accounts
                    bankAccounts = (bankAccounts + BankAccountDetails(
                        bankName = request.b_bank_name,
                        accountType = request.b_account_type,
                        accountNumber = request.b_account_no,
                        branchName = request.b_branch_name,
                        ifscCode = request.b_ifsc_code
                    )).toMutableList()
                    
                    Toast.makeText(context, "Bank account added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("BankAccountAPI", "Error response: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Failed to add bank account", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BankAccountAPI", "Exception occurred", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addRelationshipWithBank(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                Log.d("RelationshipWithBankAPI", "Starting relationship with bank addition...")
                val request = RelationshipWithBankRequest(
                    r_bank_name = relationshipBankName,
                    r_loan_type = loanType,
                    r_loan_amount = loanAmount,
                    r_roi = roi,
                    r_tenure = tenure,
                    r_emi = emi,
                    first_emi_date = firstEmiDate,
                    last_emi_date = lastEmiDate,
                    loan_account_name = loanAccountName
                )
                Log.d("RelationshipWithBankAPI", "Request data: $request")
                
                val response = RetrofitClient.apiService.addRelationshipWithBank(request)
                Log.d("RelationshipWithBankAPI", "Response code: ${response.code()}")
                Log.d("RelationshipWithBankAPI", "Response body: ${response.body()}")
                
                if (response.isSuccessful) {
                    // Clear the input fields
                    relationshipBankName = ""
                    loanType = ""
                    loanAmount = ""
                    roi = ""
                    tenure = ""
                    emi = ""
                    firstEmiDate = ""
                    lastEmiDate = ""
                    loanAccountName = ""
                    
                    // Add to the list of relationship with banks
                    relationshipWithBanks = (relationshipWithBanks + RelationshipWithBankDetails(
                        bankName = request.r_bank_name,
                        loanType = request.r_loan_type,
                        loanAmount = request.r_loan_amount,
                        roi = request.r_roi,
                        tenure = request.r_tenure,
                        emi = request.r_emi,
                        firstEmiDate = request.first_emi_date,
                        lastEmiDate = request.last_emi_date,
                        loanAccountName = request.loan_account_name
                    )).toMutableList()
                    
                    Toast.makeText(context, "Relationship with bank added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("RelationshipWithBankAPI", "Error response: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Failed to add relationship with bank", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("RelationshipWithBankAPI", "Exception occurred", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 
