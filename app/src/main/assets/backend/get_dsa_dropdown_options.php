<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('log_errors', 1);
ini_set('error_log', 'php_errors.log');

try {
    require_once 'db_config.php';

    // Create connection
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }

    // Get vendor banks
    $vendorBanksQuery = "SELECT DISTINCT vendor_bank_name FROM tbl_vendor_bank WHERE vendor_bank_name IS NOT NULL ORDER BY vendor_bank_name";
    $vendorBanksResult = $conn->query($vendorBanksQuery);
    if (!$vendorBanksResult) {
        throw new Exception("Failed to fetch vendor banks: " . $conn->error);
    }
    $vendorBanks = [];
    while ($row = $vendorBanksResult->fetch_assoc()) {
        $vendorBanks[] = $row['vendor_bank_name'];
    }

    // Get loan types
    $loanTypesQuery = "SELECT DISTINCT loan_type FROM tbl_loan_type WHERE loan_type IS NOT NULL ORDER BY loan_type";
    $loanTypesResult = $conn->query($loanTypesQuery);
    if (!$loanTypesResult) {
        throw new Exception("Failed to fetch loan types: " . $conn->error);
    }
    $loanTypes = [];
    while ($row = $loanTypesResult->fetch_assoc()) {
        $loanTypes[] = $row['loan_type'];
    }

    // Get branch states
    $branchStatesQuery = "SELECT DISTINCT branch_state_name FROM tbl_branch_state WHERE branch_state_name IS NOT NULL ORDER BY branch_state_name";
    $branchStatesResult = $conn->query($branchStatesQuery);
    if (!$branchStatesResult) {
        throw new Exception("Failed to fetch branch states: " . $conn->error);
    }
    $branchStates = [];
    while ($row = $branchStatesResult->fetch_assoc()) {
        $branchStates[] = $row['branch_state_name'];
    }

    // Get branch locations
    $branchLocationsQuery = "SELECT DISTINCT branch_location FROM tbl_branch_location WHERE branch_location IS NOT NULL ORDER BY branch_location";
    $branchLocationsResult = $conn->query($branchLocationsQuery);
    if (!$branchLocationsResult) {
        throw new Exception("Failed to fetch branch locations: " . $conn->error);
    }
    $branchLocations = [];
    while ($row = $branchLocationsResult->fetch_assoc()) {
        $branchLocations[] = $row['branch_location'];
    }

    // Prepare response
    $response = [
        "success" => true,
        "message" => "Dropdown options retrieved successfully",
        "data" => [
            "vendor_banks" => $vendorBanks,
            "loan_types" => $loanTypes,
            "branch_states" => $branchStates,
            "branch_locations" => $branchLocations
        ]
    ];

    echo json_encode($response);

} catch (Exception $e) {
    error_log("Error in get_dsa_dropdown_options.php: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error: " . $e->getMessage(),
        "data" => [
            "vendor_banks" => [],
            "loan_types" => [],
            "branch_states" => [],
            "branch_locations" => []
        ]
    ]);
} finally {
    if (isset($conn)) {
        $conn->close();
    }
}
?> 