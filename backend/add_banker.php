<?php
header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");
header("Connection: close");

// Function to send JSON response
function sendJsonResponse($success, $message, $data = null) {
    $response = json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ], JSON_UNESCAPED_UNICODE);
    
    // Ensure proper content length
    header('Content-Length: ' . strlen($response));
    echo $response;
    exit;
}

require_once 'db_config.php';

// Create connection
$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

if ($conn->connect_error) {
    sendJsonResponse(false, "Database connection failed: " . $conn->connect_error);
}

try {
    // Get the raw POST data
    $raw_data = file_get_contents("php://input");
    if (empty($raw_data)) {
        sendJsonResponse(false, "No data received");
    }

    $data = json_decode($raw_data, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        sendJsonResponse(false, "Invalid JSON data: " . json_last_error_msg());
    }

    // Validate required fields
    $required_fields = [
        'vendor_bank', 
        'banker_name', 
        'phone_number', 
        'email_id', 
        'banker_designation', 
        'loan_type', 
        'state', 
        'location', 
        'address'
    ];

    foreach ($required_fields as $field) {
        if (!isset($data[$field]) || empty($data[$field])) {
            sendJsonResponse(false, "Missing required field: $field");
        }
    }

    // Create table if not exists
    $create_table_sql = "CREATE TABLE IF NOT EXISTS tbl_bankers (
        id INT AUTO_INCREMENT PRIMARY KEY,
        vendor_bank VARCHAR(255) NOT NULL,
        banker_name VARCHAR(255) NOT NULL,
        phone_number VARCHAR(20) NOT NULL,
        email_id VARCHAR(255) NOT NULL,
        banker_designation VARCHAR(100) NOT NULL,
        loan_type VARCHAR(100) NOT NULL,
        state VARCHAR(100) NOT NULL,
        location VARCHAR(100) NOT NULL,
        visiting_card VARCHAR(255),
        address TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )";

    if (!$conn->query($create_table_sql)) {
        throw new Exception("Error creating table: " . $conn->error);
    }

    // Prepare the INSERT query
    $sql = "INSERT INTO tbl_bankers (
        vendor_bank, banker_name, phone_number, email_id, 
        banker_designation, loan_type, state, location, 
        visiting_card, address
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Error preparing statement: " . $conn->error);
    }

    $stmt->bind_param(
        "ssssssssss",
        $data['vendor_bank'],
        $data['banker_name'],
        $data['phone_number'],
        $data['email_id'],
        $data['banker_designation'],
        $data['loan_type'],
        $data['state'],
        $data['location'],
        $data['visiting_card'],
        $data['address']
    );

    if (!$stmt->execute()) {
        throw new Exception("Error executing statement: " . $stmt->error);
    }

    $banker_id = $conn->insert_id;
    $stmt->close();
    $conn->close();

    sendJsonResponse(true, "Banker added successfully", ["id" => $banker_id]);

} catch (Exception $e) {
    error_log("Error in add_banker.php: " . $e->getMessage());
    sendJsonResponse(false, "Error: " . $e->getMessage());
}
?> 