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
        "success" => false, 
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

        // Define allowed columns for tbl_database_relation_bank
        $allowedColumns = [
            'r_bank_name',
            'r_loan_type',
            'r_loan_amount',
            'r_roi',
            'r_tenure',
            'r_emi',
            'first_emi_date',
            'last_emi_date',
            'loan_account_name'
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

        // Create table if not exists
        $create_table_sql = "CREATE TABLE IF NOT EXISTS tbl_database_relation_bank (
            id INT AUTO_INCREMENT PRIMARY KEY,
            r_bank_name VARCHAR(255) NOT NULL,
            r_loan_type VARCHAR(50) NOT NULL,
            r_loan_amount VARCHAR(50) NOT NULL,
            r_roi VARCHAR(10) NOT NULL,
            r_tenure VARCHAR(10) NOT NULL,
            r_emi VARCHAR(50) NOT NULL,
            first_emi_date VARCHAR(20) NOT NULL,
            last_emi_date VARCHAR(20) NOT NULL,
            loan_account_name VARCHAR(50) NOT NULL
        )";

        if (!$conn->query($create_table_sql)) {
            throw new Exception("Error creating table: " . $conn->error);
        }

        // Build and execute the INSERT query
        $sql = "INSERT INTO tbl_database_relation_bank (" . implode(', ', $columns) . ") VALUES (" . implode(', ', $values) . ")";
        
        if ($conn->query($sql)) {
            echo json_encode([
                "success" => true,
                "message" => "Relationship with bank added successfully",
                "data" => null
            ]);
        } else {
            throw new Exception($conn->error);
        }
    } else {
        // Handle GET request to check if table exists
        $result = $conn->query("SHOW TABLES LIKE 'tbl_database_relation_bank'");
        if ($result->num_rows == 0) {
            http_response_code(404);
            echo json_encode([
                "success" => false, 
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

        $sql = "SELECT * FROM tbl_database_relation_bank";
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
            "success" => true,
            "message" => "Data retrieved successfully",
            "data" => $data
        ]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false, 
        "message" => $e->getMessage()
    ]);
}

$conn->close();
?> 