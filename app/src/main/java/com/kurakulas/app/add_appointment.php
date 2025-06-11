<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once 'db_config.php';

// Create connection
$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);

if ($conn->connect_error) {
    die(json_encode([
        "status" => "error", 
        "message" => "Database connection failed"
    ]));
}

try {
    // Check if this is a POST request
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        // Get the raw POST data
        $raw_data = file_get_contents("php://input");
        $data = json_decode($raw_data, true);

        if (json_last_error() !== JSON_ERROR_NONE) {
            throw new Exception("Invalid JSON data");
        }

        // Validate mobile number (required field)
        if (empty($data['mobile_number'])) {
            throw new Exception("Mobile number is required");
        }

        // Define allowed columns for tbl_appointment
        $allowedColumns = [
            'mobile_number',
            'database_id',
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

        $columns = [];
        $values = [];

        // Process each field from the request
        foreach ($data as $key => $value) {
            if (in_array($key, $allowedColumns) && $value !== null) {
                $columns[] = "`$key`";
                $values[] = "'" . $conn->real_escape_string($value) . "'";
            }
        }

        if (empty($columns)) {
            throw new Exception("No valid data provided for insertion");
        }

        // Build and execute the INSERT query
        $sql = "INSERT INTO tbl_appointment (" . implode(', ', $columns) . ") VALUES (" . implode(', ', $values) . ")";
        
        if ($conn->query($sql)) {
            echo json_encode([
                "status" => "success",
                "message" => "Appointment added successfully",
                "insert_id" => $conn->insert_id
            ]);
        } else {
            throw new Exception($conn->error);
        }
    } else {
        // Handle GET request to check if table exists
        $result = $conn->query("SHOW TABLES LIKE 'tbl_appointment'");
        if ($result->num_rows == 0) {
            http_response_code(404);
            echo json_encode([
                "status" => "error", 
                "message" => "Table not found"
            ]);
            exit;
        }

        // Handle SELECT operation
        $where = [];
        foreach ($_GET as $key => $value) {
            if ($key !== 'table' && $key !== 'action' && !empty($value)) {
                $where[] = "`$key` = '" . $conn->real_escape_string($value) . "'";
            }
        }

        $sql = "SELECT * FROM tbl_appointment";
        if (!empty($where)) {
            $sql .= " WHERE " . implode(' AND ', $where);
        }

        $result = $conn->query($sql);
        if (!$result) {
            throw new Exception($conn->error);
        }

        $data = [];
        while ($row = $result->fetch_assoc()) {
            $data[] = $row;
        }

        echo json_encode([
            "status" => "success",
            "table" => "tbl_appointment",
            "count" => count($data),
            "data" => $data
        ]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "status" => "error", 
        "message" => $e->getMessage()
    ]);
}

$conn->close();
?> 