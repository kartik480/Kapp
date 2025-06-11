<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/appointment_errors.log');

// Set headers
header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");
header("Connection: close");

// Function to log debug messages
function logDebug($message) {
    error_log(date('[Y-m-d H:i:s] ') . $message . "\n", 3, __DIR__ . '/appointment_debug.log');
}

// Function to send JSON response
function sendJsonResponse($success, $message, $data = null) {
    $response = json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ], JSON_UNESCAPED_UNICODE);
    echo $response;
    exit;
}

try {
    require_once 'db_config.php';
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }

    $raw_data = file_get_contents("php://input");
    logDebug("Received raw data: " . $raw_data);
    if (empty($raw_data)) {
        throw new Exception("No data received");
    }
    $data = json_decode($raw_data, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("Invalid JSON data: " . json_last_error_msg());
    }
    logDebug("Decoded data: " . print_r($data, true));

    // Validate required fields
    $required_fields = [
        'database_id', 'unique_id', 'mobile_number', 'lead_name', 'email_id', 'company_name',
        'alternative_mobile', 'state', 'location', 'sub_location', 'pin_code', 'source',
        'user_qualification', 'residental_address', 'customer_type'
    ];
    foreach ($required_fields as $field) {
        if (!isset($data[$field]) || empty($data[$field])) {
            throw new Exception("Missing required field: $field");
        }
    }

    // Create table if not exists
    $create_table_sql = "CREATE TABLE IF NOT EXISTS tbl_appointment (
        id INT AUTO_INCREMENT PRIMARY KEY,
        database_id INT NOT NULL,
        unique_id VARCHAR(100) NOT NULL,
        mobile_number VARCHAR(20) NOT NULL,
        lead_name VARCHAR(255) NOT NULL,
        email_id VARCHAR(255) NOT NULL,
        company_name VARCHAR(255) NOT NULL,
        alternative_mobile VARCHAR(20) NOT NULL,
        state VARCHAR(100) NOT NULL,
        location VARCHAR(100) NOT NULL,
        sub_location VARCHAR(100) NOT NULL,
        pin_code VARCHAR(20) NOT NULL,
        source VARCHAR(100) NOT NULL,
        visiting_card VARCHAR(255),
        user_qualification VARCHAR(255) NOT NULL,
        residental_address TEXT NOT NULL,
        customer_type VARCHAR(100) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    )";
    if (!$conn->query($create_table_sql)) {
        throw new Exception("Error creating table: " . $conn->error);
    }

    $sql = "INSERT INTO tbl_appointment (
        database_id, unique_id, mobile_number, lead_name, email_id, company_name, alternative_mobile, state, location, sub_location, pin_code, source, visiting_card, user_qualification, residental_address, customer_type
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Error preparing statement: " . $conn->error);
    }

    $visiting_card = isset($data['visiting_card']) ? $data['visiting_card'] : '';

    $stmt->bind_param(
        "isssssssssssssss",
        $data['database_id'],
        $data['unique_id'],
        $data['mobile_number'],
        $data['lead_name'],
        $data['email_id'],
        $data['company_name'],
        $data['alternative_mobile'],
        $data['state'],
        $data['location'],
        $data['sub_location'],
        $data['pin_code'],
        $data['source'],
        $visiting_card,
        $data['user_qualification'],
        $data['residental_address'],
        $data['customer_type']
    );

    logDebug("Executing query with parameters: " . print_r([
        'database_id' => $data['database_id'],
        'unique_id' => $data['unique_id'],
        'mobile_number' => $data['mobile_number'],
        'lead_name' => $data['lead_name'],
        'email_id' => $data['email_id'],
        'company_name' => $data['company_name'],
        'alternative_mobile' => $data['alternative_mobile'],
        'state' => $data['state'],
        'location' => $data['location'],
        'sub_location' => $data['sub_location'],
        'pin_code' => $data['pin_code'],
        'source' => $data['source'],
        'visiting_card' => $visiting_card,
        'user_qualification' => $data['user_qualification'],
        'residental_address' => $data['residental_address'],
        'customer_type' => $data['customer_type']
    ], true));

    if (!$stmt->execute()) {
        throw new Exception("Error executing statement: " . $stmt->error);
    }

    $appointment_id = $conn->insert_id;
    logDebug("Successfully added appointment with ID: " . $appointment_id);

    $stmt->close();
    $conn->close();

    sendJsonResponse(true, "Appointment added successfully", ["id" => $appointment_id]);

} catch (Exception $e) {
    logDebug("Error: " . $e->getMessage());
    sendJsonResponse(false, "Error: " . $e->getMessage());
}

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once __DIR__ . '/db_config.php';

if (!defined('DB_HOST') || !defined('DB_USER') || !defined('DB_PASS') || !defined('DB_NAME')) {
    echo json_encode([
        'success' => false,
        'message' => 'Database configuration constants are not properly set',
        'data' => []
    ]);
    exit();
}

try {
    $json = file_get_contents('php://input');
file_put_contents(__DIR__ . '/appointment_debug.log', "\n[" . date('Y-m-d H:i:s') . "] Incoming JSON: " . $json, FILE_APPEND);
    $data = json_decode($json, true);

    // Validate required fields
    $required = [
        'database_id', 'unique_id', 'mobile_number', 'lead_name', 'email_id', 'company_name',
        'alternative_mobile', 'state', 'location', 'sub_location', 'pin_code', 'source',
        'user_qualification', 'residental_address', 'customer_type'
    ];
    foreach ($required as $field) {
        if (!isset($data[$field]) || $data[$field] === '') {
            throw new Exception('Missing required field: ' . $field);
        }
    }

    // visiting_card is optional
    $visiting_card = isset($data['visiting_card']) ? $data['visiting_card'] : null;

    $conn = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $query = "INSERT INTO tbl_appointment (
        database_id, unique_id, mobile_number, lead_name, email_id, company_name, alternative_mobile, state, location, sub_location, pin_code, source, visiting_card, user_qualification, residental_address, customer_type
    ) VALUES (
        :database_id, :unique_id, :mobile_number, :lead_name, :email_id, :company_name, :alternative_mobile, :state, :location, :sub_location, :pin_code, :source, :visiting_card, :user_qualification, :residental_address, :customer_type
    )";
    $stmt = $conn->prepare($query);
    $stmt->execute([
        ':database_id' => $data['database_id'],
        ':unique_id' => $data['unique_id'],
        ':mobile_number' => $data['mobile_number'],
        ':lead_name' => $data['lead_name'],
        ':email_id' => $data['email_id'],
        ':company_name' => $data['company_name'],
        ':alternative_mobile' => $data['alternative_mobile'],
        ':state' => $data['state'],
        ':location' => $data['location'],
        ':sub_location' => $data['sub_location'],
        ':pin_code' => $data['pin_code'],
        ':source' => $data['source'],
        ':visiting_card' => $visiting_card,
        ':user_qualification' => $data['user_qualification'],
        ':residental_address' => $data['residental_address'],
        ':customer_type' => $data['customer_type']
    ]);

    echo json_encode([
        'success' => true,
        'message' => 'Appointment added successfully',
        'data' => [ 'id' => $conn->lastInsertId() ]
    ]);
} catch (Exception $e) {
    $errorLog = "\n[" . date('Y-m-d H:i:s') . "] Error: " . $e->getMessage();
    file_put_contents(__DIR__ . '/appointment_debug.log', $errorLog, FILE_APPEND);
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage(),
        'data' => []
    ]);
}
