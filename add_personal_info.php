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
    $requiredFields = [
        'mobile_number',
        'lead_name',
        'email_id',
        'company_name',
        'alternative_mobile',
        'state',
        'location',
        'sub_location',
        'pin_code',
        'source',
        'visiting_card',
        'user_qualification',
        'residental_address',
        'customer_type'
    ];

    foreach ($requiredFields as $field) {
        if (!isset($data[$field])) {
            throw new Exception("Missing required field: $field");
        }
    }

    // Create database connection
    $conn = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Start transaction
    $conn->beginTransaction();

    try {
        // Insert into tbl_database
        $query1 = "INSERT INTO tbl_database (
                    mobile_number, lead_name, email_id, company_name, 
                    alternative_mobile, state, location, sub_location, 
                    pin_code, source, visiting_card, user_qualification, 
                    residental_address, customer_type
                  ) VALUES (
                    :mobile_number, :lead_name, :email_id, :company_name,
                    :alternative_mobile, :state, :location, :sub_location,
                    :pin_code, :source, :visiting_card, :user_qualification,
                    :residental_address, :customer_type
                  )";
        
        $stmt1 = $conn->prepare($query1);
        $stmt1->execute([
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
            ':visiting_card' => $data['visiting_card'],
            ':user_qualification' => $data['user_qualification'],
            ':residental_address' => $data['residental_address'],
            ':customer_type' => $data['customer_type']
        ]);

        // Get the inserted ID from tbl_database
        $databaseId = $conn->lastInsertId();

        // Insert into tbl_appointment
        $query2 = "INSERT INTO tbl_appointment (
                    mobile_number, lead_name, email_id, company_name, 
                    alternative_mobile, state, location, sub_location, 
                    pin_code, source, visiting_card, user_qualification, 
                    residental_address, customer_type
                  ) VALUES (
                    :mobile_number, :lead_name, :email_id, :company_name,
                    :alternative_mobile, :state, :location, :sub_location,
                    :pin_code, :source, :visiting_card, :user_qualification,
                    :residental_address, :customer_type
                  )";
        
        $stmt2 = $conn->prepare($query2);
        $stmt2->execute([
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
            ':visiting_card' => $data['visiting_card'],
            ':user_qualification' => $data['user_qualification'],
            ':residental_address' => $data['residental_address'],
            ':customer_type' => $data['customer_type']
        ]);

        // Get the inserted ID from tbl_appointment
        $appointmentId = $conn->lastInsertId();

        // Commit transaction
        $conn->commit();

        // Prepare response
        $response = [
            'success' => true,
            'message' => 'Personal information added successfully to both tables',
            'data' => [
                'database_id' => $databaseId,
                'appointment_id' => $appointmentId
            ]
        ];

        echo json_encode($response, JSON_UNESCAPED_UNICODE);

    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollBack();
        throw $e;
    }

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