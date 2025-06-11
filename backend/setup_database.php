<?php
// Enable error reporting for debugging
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
    
    // Create database if it doesn't exist
    $sql = "CREATE DATABASE IF NOT EXISTS " . DB_NAME;
    if (!$conn->query($sql)) {
        throw new Exception("Error creating database: " . $conn->error);
    }
    
    // Select the database
    if (!$conn->select_db(DB_NAME)) {
        throw new Exception("Error selecting database: " . $conn->error);
    }
    
    // Create branch state table
    $sql = "CREATE TABLE IF NOT EXISTS tbl_branch_state (
        id INT AUTO_INCREMENT PRIMARY KEY,
        branch_state_name VARCHAR(100) NOT NULL
    )";
    if (!$conn->query($sql)) {
        throw new Exception("Error creating branch state table: " . $conn->error);
    }
    
    // Create branch location table
    $sql = "CREATE TABLE IF NOT EXISTS tbl_branch_location (
        id INT AUTO_INCREMENT PRIMARY KEY,
        branch_location VARCHAR(100) NOT NULL
    )";
    if (!$conn->query($sql)) {
        throw new Exception("Error creating branch location table: " . $conn->error);
    }
    
    // Check if tables are empty
    $result = $conn->query("SELECT COUNT(*) as count FROM tbl_branch_state");
    $row = $result->fetch_assoc();
    if ($row['count'] == 0) {
        // Insert sample states
        $sql = "INSERT INTO tbl_branch_state (branch_state_name) VALUES 
            ('Andhra Pradesh'),
            ('Telangana'),
            ('Karnataka'),
            ('Tamil Nadu'),
            ('Kerala')";
        if (!$conn->query($sql)) {
            throw new Exception("Error inserting states: " . $conn->error);
        }
    }
    
    $result = $conn->query("SELECT COUNT(*) as count FROM tbl_branch_location");
    $row = $result->fetch_assoc();
    if ($row['count'] == 0) {
        // Insert sample locations
        $sql = "INSERT INTO tbl_branch_location (branch_location) VALUES 
            ('Hyderabad'),
            ('Bangalore'),
            ('Chennai'),
            ('Kochi'),
            ('Vijayawada')";
        if (!$conn->query($sql)) {
            throw new Exception("Error inserting locations: " . $conn->error);
        }
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'Database and tables set up successfully'
    ]);
    
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