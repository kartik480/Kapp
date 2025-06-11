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
    // Fetch all pincodes from tbl_pincode
    $sql = "SELECT pincode FROM tbl_pincode ORDER BY pincode";
    $result = $conn->query($sql);

    if (!$result) {
        throw new Exception($conn->error);
    }

    $pincodes = [];
    while ($row = $result->fetch_assoc()) {
        $pincodes[] = $row['pincode'];
    }

    echo json_encode([
        "success" => true,
        "message" => "Pincodes fetched successfully",
        "data" => $pincodes
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