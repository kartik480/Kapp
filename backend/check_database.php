<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once 'db_config.php';

try {
    // Create connection
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS);
    
    // Check connection
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }
    
    $response = [
        'database_exists' => false,
        'tables_exist' => [
            'tbl_branch_state' => false,
            'tbl_branch_location' => false
        ],
        'table_structures' => [],
        'sample_data' => []
    ];
    
    // Check if database exists
    $result = $conn->query("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" . DB_NAME . "'");
    $response['database_exists'] = $result->num_rows > 0;
    
    if ($response['database_exists']) {
        // Select the database
        $conn->select_db(DB_NAME);
        
        // Check if tables exist and get their structure
        $tables = ['tbl_branch_state', 'tbl_branch_location'];
        foreach ($tables as $table) {
            $result = $conn->query("SHOW TABLES LIKE '$table'");
            $response['tables_exist'][$table] = $result->num_rows > 0;
            
            if ($response['tables_exist'][$table]) {
                // Get table structure
                $result = $conn->query("DESCRIBE $table");
                $structure = [];
                while ($row = $result->fetch_assoc()) {
                    $structure[] = $row;
                }
                $response['table_structures'][$table] = $structure;
                
                // Get sample data
                $result = $conn->query("SELECT * FROM $table LIMIT 5");
                $data = [];
                while ($row = $result->fetch_assoc()) {
                    $data[] = $row;
                }
                $response['sample_data'][$table] = $data;
            }
        }
    }
    
    echo json_encode($response, JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'error' => $e->getMessage(),
        'trace' => $e->getTraceAsString()
    ]);
} finally {
    if (isset($conn)) {
        $conn->close();
    }
}
?> 