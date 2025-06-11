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
    // Fetch locations from tbl_location
    $sql = "SELECT location AS location_name FROM tbl_location ORDER BY location";
    $result = $conn->query($sql);

    if (!$result) {
        throw new Exception($conn->error);
    }

    $locations = [];
    while ($row = $result->fetch_assoc()) {
        $locations[] = $row['location_name'];
    }

    echo json_encode([
        "success" => true,
        "message" => "Locations fetched successfully",
        "data" => $locations
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