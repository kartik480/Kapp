data class BankAccountRequest(
    val b_bank_name: String,
    val b_account_type: String,
    val b_account_no: String,
    val b_branch_name: String,
    val b_ifsc_code: String
)

// Remove the separate BankAccountResponse since we're using ApiResponse<Unit> 
