<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Include database configuration
$configFile = __DIR__ . '/db_config.php';
if (!file_exists($configFile)) {
    error_log("Database config file not found at: " . $configFile);
    echo json_encode([
        'success' => false,
        'message' => 'Database configuration file not found',
        'data' => []
    ]);
    exit();
}

require_once $configFile;

// Verify that all required constants are defined
if (!defined('DB_HOST') || !defined('DB_USER') || !defined('DB_PASS') || !defined('DB_NAME')) {
    error_log("Missing database configuration constants");
    echo json_encode([
        'success' => false,
        'message' => 'Database configuration constants are not properly set',
        'data' => []
    ]);
    exit();
}

try {
    // Get POST data
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);

    // Validate required fields
    if (!isset($data['appt_bank']) || !isset($data['appt_product']) || 
        !isset($data['appt_status']) || !isset($data['appt_sub_status'])) {
        throw new Exception('Missing required fields');
    }

    // Create database connection
    $conn = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Prepare and execute query
    $query = "INSERT INTO tbl_appointment_calling_status (appt_bank, appt_product, appt_status, appt_sub_status, notes) 
              VALUES (:appt_bank, :appt_product, :appt_status, :appt_sub_status, :notes)";
    
    $stmt = $conn->prepare($query);
    $stmt->execute([
        ':appt_bank' => $data['appt_bank'],
        ':appt_product' => $data['appt_product'],
        ':appt_status' => $data['appt_status'],
        ':appt_sub_status' => $data['appt_sub_status'],
        ':notes' => $data['notes'] ?? ''
    ]);

    // Get the inserted ID
    $insertedId = $conn->lastInsertId();

    // Prepare response
    $response = [
        'success' => true,
        'message' => 'Appointment status added successfully',
        'data' => [
            'id' => $insertedId
        ]
    ];

    echo json_encode($response, JSON_UNESCAPED_UNICODE);

} catch(PDOException $e) {
    // Handle database errors
    error_log("Database error: " . $e->getMessage());
    
    $response = [
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage(),
        'data' => []
    ];
    
    http_response_code(500);
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
} catch(Exception $e) {
    // Handle other errors
    error_log("General error: " . $e->getMessage());
    
    $response = [
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'data' => []
    ];
    
    http_response_code(400);
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
} finally {
    // Close database connection
    if (isset($conn)) {
        $conn = null;
    }
}
?> 