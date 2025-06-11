<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once 'db_connect.php';

try {
    // Check if tables exist
    $tables = ['tbl_branch_state', 'tbl_branch_location'];
    $result = [];

    foreach ($tables as $table) {
        $query = "SHOW TABLES LIKE '$table'";
        $tableResult = $conn->query($query);
        
        if ($tableResult->num_rows > 0) {
            // Table exists, check for data
            $dataQuery = "SELECT COUNT(*) as count FROM $table";
            $dataResult = $conn->query($dataQuery);
            $row = $dataResult->fetch_assoc();
            
            $result[$table] = [
                'exists' => true,
                'row_count' => $row['count']
            ];
            
            // Get sample data
            $sampleQuery = "SELECT * FROM $table LIMIT 5";
            $sampleResult = $conn->query($sampleQuery);
            $sampleData = [];
            while ($sampleRow = $sampleResult->fetch_assoc()) {
                $sampleData[] = $sampleRow;
            }
            $result[$table]['sample_data'] = $sampleData;
        } else {
            $result[$table] = [
                'exists' => false,
                'row_count' => 0
            ];
        }
    }
    
    echo json_encode($result);
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