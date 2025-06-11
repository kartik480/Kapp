<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Access-Control-Max-Age: 86400"); // 24 hours

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Include database connection
require_once 'db_config.php';

try {
    // Get POST data
    $data = json_decode(file_get_contents('php://input'), true);

    // Validate required fields
    $required_fields = ['database_id', 'p_property_type', 'p_area', 'p_lands', 'p_sft', 'p_market_value'];
    foreach ($required_fields as $field) {
        if (!isset($data[$field]) || empty($data[$field])) {
            throw new Exception("Missing required field: $field");
        }
    }

    // Prepare and execute the SQL query
    $stmt = $conn->prepare("INSERT INTO tbl_database_property_details (database_id, p_property_type, p_area, p_lands, p_sft, p_market_value) VALUES (?, ?, ?, ?, ?, ?)");
    
    if (!$stmt) {
        throw new Exception('Prepare statement failed: ' . $conn->error);
    }

    $stmt->bind_param("isssss", 
        $data['database_id'],
        $data['p_property_type'],
        $data['p_area'],
        $data['p_lands'],
        $data['p_sft'],
        $data['p_market_value']
    );

    if (!$stmt->execute()) {
        throw new Exception('Execute failed: ' . $stmt->error);
    }

    // Get the inserted ID
    $inserted_id = $conn->insert_id;

    echo json_encode([
        'success' => true,
        'message' => 'Property details added successfully',
        'data' => [
            'id' => $inserted_id,
            'database_id' => $data['database_id'],
            'p_property_type' => $data['p_property_type'],
            'p_area' => $data['p_area'],
            'p_lands' => $data['p_lands'],
            'p_sft' => $data['p_sft'],
            'p_market_value' => $data['p_market_value']
        ]
    ]);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
}
?>