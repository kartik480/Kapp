<?php
error_reporting(0);
ini_set('display_errors', 0);

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

require_once 'db_config.php';

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

    if ($conn->connect_error) {
        throw new Exception("Database connection failed: " . $conn->connect_error);
    }

    // Check if table exists
    $result = $conn->query("SHOW TABLES LIKE 'tbl_bankers'");
    if ($result->num_rows == 0) {
        // Create table if it doesn't exist
        $sql = "CREATE TABLE IF NOT EXISTS tbl_bankers (
            id INT AUTO_INCREMENT PRIMARY KEY,
            vendor_bank VARCHAR(255) NOT NULL,
            banker_name VARCHAR(255) NOT NULL,
            banker_designation VARCHAR(255) NOT NULL,
            phone_number VARCHAR(20) NOT NULL,
            email_id VARCHAR(255) NOT NULL,
            loan_type VARCHAR(255) NOT NULL,
            state VARCHAR(255) NOT NULL,
            location VARCHAR(255) NOT NULL,
            visiting_card VARCHAR(255),
            address TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )";
        
        if (!$conn->query($sql)) {
            throw new Exception("Failed to create table: " . $conn->error);
        }
        
        echo json_encode([
            "success" => true,
            "message" => "Table created successfully",
            "data" => []
        ]);
    } else {
        // Check table structure
        $result = $conn->query("DESCRIBE tbl_bankers");
        $columns = [];
        while ($row = $result->fetch_assoc()) {
            $columns[] = $row;
        }
        
        echo json_encode([
            "success" => true,
            "message" => "Table exists",
            "data" => [
                "columns" => $columns
            ]
        ]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => $e->getMessage(),
        "data" => []
    ]);
} finally {
    if (isset($conn)) {
        $conn->close();
    }
}
?> 