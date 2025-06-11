<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

// Start output buffering
ob_start();

try {
    // Include database configuration
    require_once 'db_config.php';
    
    // Log connection attempt
    error_log("Attempting to connect to database: " . DB_HOST);
    
    // Create connection
    $conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
    
    // Check connection
    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }
    
    error_log("Database connection successful");
    
    // Check if table exists
    $tableCheck = $conn->query("SHOW TABLES LIKE 'tbl_branch_location'");
    if ($tableCheck->num_rows === 0) {
        throw new Exception("Table 'tbl_branch_location' does not exist");
    }
    
    // Prepare and execute query
    $query = "SELECT DISTINCT branch_location FROM tbl_branch_location ORDER BY branch_location";
    error_log("Executing query: " . $query);
    
    $stmt = $conn->prepare($query);
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    if (!$result) {
        throw new Exception("Failed to get result: " . $stmt->error);
    }
    
    $locations = array();
    while ($row = $result->fetch_assoc()) {
        $locations[] = $row['branch_location'];
    }
    
    error_log("Found " . count($locations) . " locations");
    
    // Clear any output buffer
    ob_clean();
    
    // Send JSON response
    if (empty($locations)) {
        echo json_encode([]); // Return empty array if no locations found
    } else {
        echo json_encode($locations);
    }
    
} catch (Exception $e) {
    // Log the error
    error_log("Error in get_branch_locations.php: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    
    // Clear any output buffer
    ob_clean();
    
    // Send error response
    http_response_code(500);
    echo json_encode([
        'error' => $e->getMessage(),
        'trace' => $e->getTraceAsString(),
        'details' => [
            'db_host' => DB_HOST,
            'db_name' => DB_NAME,
            'db_user' => DB_USER,
            'php_version' => PHP_VERSION,
            'mysql_version' => isset($conn) ? $conn->server_info : 'Not connected'
        ]
    ]);
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($conn)) {
        $conn->close();
    }
    // End output buffering
    ob_end_flush();
}
?> 