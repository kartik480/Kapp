<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

require_once 'db_config.php';

// Create connection
$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

if ($conn->connect_error) {
    die(json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error,
        "data" => []
    ]));
}

try {
    // First, check if the database exists
    $result = $conn->query("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" . DB_NAME . "'");
    if ($result->num_rows == 0) {
        // Create the database if it doesn't exist
        if (!$conn->query("CREATE DATABASE " . DB_NAME)) {
            throw new Exception("Failed to create database: " . $conn->error);
        }
        $conn->select_db(DB_NAME);
    }

    // Create the vendor banks table
    $sql = "CREATE TABLE IF NOT EXISTS tbl_vendor_banks (
        id INT AUTO_INCREMENT PRIMARY KEY,
        vendor_bank_name VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )";
    
    if (!$conn->query($sql)) {
        throw new Exception("Failed to create table: " . $conn->error);
    }

    // Check if table is empty
    $result = $conn->query("SELECT COUNT(*) as count FROM tbl_vendor_banks");
    $row = $result->fetch_assoc();
    
    if ($row['count'] == 0) {
        // Insert sample data only if table is empty
        $sampleBanks = [
            "HDFC Bank",
            "ICICI Bank",
            "State Bank of India",
            "Axis Bank",
            "Kotak Mahindra Bank"
        ];
        
        $insertSql = "INSERT INTO tbl_vendor_banks (vendor_bank_name) VALUES (?)";
        $stmt = $conn->prepare($insertSql);
        
        if (!$stmt) {
            throw new Exception("Failed to prepare insert statement: " . $conn->error);
        }
        
        foreach ($sampleBanks as $bank) {
            $stmt->bind_param("s", $bank);
            if (!$stmt->execute()) {
                throw new Exception("Failed to insert bank: " . $stmt->error);
            }
        }
        
        $stmt->close();
    }
    
    echo json_encode([
        "success" => true,
        "message" => "Table created and sample data inserted successfully",
        "data" => []
    ]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => $e->getMessage(),
        "data" => []
    ]);
}

$conn->close();
?> 