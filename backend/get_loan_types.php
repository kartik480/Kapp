<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_config.php';

try {
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }
    
    $sql = "SELECT loan_type FROM tbl_loan_type ORDER BY loan_type";
    $result = $conn->query($sql);
    
    if ($result) {
        $loanTypes = array();
        while ($row = $result->fetch_assoc()) {
            $loanTypes[] = $row['loan_type'];
        }
        
        echo json_encode([
            'success' => true,
            'loan_types' => $loanTypes
        ]);
    } else {
        throw new Exception("Error fetching loan types: " . $conn->error);
    }
    
    $conn->close();
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}
?> 