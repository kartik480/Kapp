package pzn.api

data class RelationshipWithBankRequest(
    val r_bank_name: String,
    val r_loan_type: String,
    val r_loan_amount: String,
    val r_roi: String,
    val r_tenure: String,
    val r_emi: String,
    val first_emi_date: String,
    val last_emi_date: String,
    val loan_account_name: String
) 
