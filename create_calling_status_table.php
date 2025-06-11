<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once 'db_config.php';

try {
    $conn = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Create table
    $query = "CREATE TABLE IF NOT EXISTS tbl_database_calling_status (
        id INT AUTO_INCREMENT PRIMARY KEY,
        appt_bank VARCHAR(255) NOT NULL,
        appt_product VARCHAR(255) NOT NULL,
        appt_status VARCHAR(255) NOT NULL,
        appt_sub_status VARCHAR(255) NOT NULL,
        notes TEXT
    )";
    
    $conn->exec($query);

    echo json_encode([
        'success' => true,
        'message' => 'Table created successfully'
    ]);

} catch(PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
}
?> 