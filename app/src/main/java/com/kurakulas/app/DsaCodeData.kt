package pzn.api

data class DsaCodeData(
    val vendorBank: String,
    val dsaCode: String,
    val dsaName: String,
    val loanType: String,
    val state: String,
    val location: String,
    val contactPerson: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    val registrationDate: String? = null,
    val status: String? = null,
    val performanceMetrics: DsaCodePerformanceMetrics? = null
)

data class DsaCodePerformanceMetrics(
    val totalApplications: Int = 0,
    val approved: Int = 0,
    val rejected: Int = 0,
    val pending: Int = 0,
    val successRate: Double = 0.0
) 
