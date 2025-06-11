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
        "message" => "Database connection failed",
        "data" => []
    ]));
}

try {
    // Fetch all vendor banks from tbl_vendor_banks
    $sql = "SELECT vendor_bank_name FROM tbl_vendor_banks ORDER BY vendor_bank_name";
    $result = $conn->query($sql);

    if (!$result) {
        throw new Exception($conn->error);
    }

    $banks = [];
    while ($row = $result->fetch_assoc()) {
        $banks[] = $row['vendor_bank_name'];
    }

    echo json_encode([
        "success" => true,
        "message" => "Vendor banks fetched successfully",
        "data" => [
            "vendor_banks" => $banks
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