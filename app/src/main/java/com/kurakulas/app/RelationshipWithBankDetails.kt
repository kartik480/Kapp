package pzn.api

data class RelationshipWithBankDetails(
    val bankName: String,
    val loanType: String,
    val loanAmount: String,
    val roi: String,
    val tenure: String,
    val emi: String,
    val firstEmiDate: String,
    val lastEmiDate: String,
    val loanAccountName: String
) 
