<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Include database configuration
require_once 'config.php';

// Get POST data
$data = json_decode(file_get_contents('php://input'), true);

// Validate required fields
if (!isset($data['b_bank_name']) || !isset($data['b_account_type']) || 
    !isset($data['b_account_no']) || !isset($data['b_branch_name']) || 
    !isset($data['b_ifsc_code'])) {
    echo json_encode([
        'success' => false,
        'message' => 'All fields are required'
    ]);
    exit();
}

// Sanitize input data
$bank_name = $conn->real_escape_string($data['b_bank_name']);
$account_type = $conn->real_escape_string($data['b_account_type']);
$account_no = $conn->real_escape_string($data['b_account_no']);
$branch_name = $conn->real_escape_string($data['b_branch_name']);
$ifsc_code = $conn->real_escape_string($data['b_ifsc_code']);

// Create table if not exists
$create_table_sql = "CREATE TABLE IF NOT EXISTS tbl_database_bank_account_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    b_bank_name VARCHAR(255) NOT NULL,
    b_account_type VARCHAR(50) NOT NULL,
    b_account_no VARCHAR(50) NOT NULL,
    b_branch_name VARCHAR(255) NOT NULL,
    b_ifsc_code VARCHAR(11) NOT NULL
)";

if (!$conn->query($create_table_sql)) {
    echo json_encode([
        'success' => false,
        'message' => 'Error creating table: ' . $conn->error
    ]);
    exit();
}

// Prepare and execute the SQL query
$sql = "INSERT INTO tbl_database_bank_account_details 
        (b_bank_name, b_account_type, b_account_no, b_branch_name, b_ifsc_code) 
        VALUES (?, ?, ?, ?, ?)";

$stmt = $conn->prepare($sql);
$stmt->bind_param("sssss", $bank_name, $account_type, $account_no, $branch_name, $ifsc_code);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'message' => 'Bank account details added successfully',
        'data' => $conn->insert_id // Returns the ID of the inserted record
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Error adding bank account details: ' . $stmt->error
    ]);
}

$stmt->close();
$conn->close();
?> 