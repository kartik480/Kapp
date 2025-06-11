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
    // Check if table exists
    $table_check = $conn->query("SHOW TABLES LIKE 'tbl_vendor_bank'");
    if ($table_check->num_rows == 0) {
        throw new Exception("Table 'tbl_vendor_bank' does not exist");
    }

    // Check table structure
    $structure = $conn->query("DESCRIBE tbl_vendor_bank");
    $columns = [];
    while ($row = $structure->fetch_assoc()) {
        $columns[] = $row;
    }

    // Get data from table
    $result = $conn->query("SELECT * FROM tbl_vendor_bank");
    $data = [];
    while ($row = $result->fetch_assoc()) {
        $data[] = $row;
    }

    echo json_encode([
        "success" => true,
        "message" => "Table check completed",
        "data" => [
            "table_exists" => true,
            "columns" => $columns,
            "records" => $data
        ]
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