<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

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
    error_log("DB_HOST defined: " . (defined('DB_HOST') ? 'yes' : 'no'));
    error_log("DB_USER defined: " . (defined('DB_USER') ? 'yes' : 'no'));
    error_log("DB_PASS defined: " . (defined('DB_PASS') ? 'yes' : 'no'));
    error_log("DB_NAME defined: " . (defined('DB_NAME') ? 'yes' : 'no'));
    
    echo json_encode([
        'success' => false,
        'message' => 'Database configuration constants are not properly set',
        'data' => []
    ]);
    exit();
}

try {
    // Debug database connection parameters
    error_log("Attempting database connection with:");
    error_log("Host: " . DB_HOST);
    error_log("Database: " . DB_NAME);
    error_log("Username: " . DB_USER);
    
    // Create database connection using the configuration from db_config.php
    $conn = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    error_log("Database connection successful");

    // Prepare and execute query to get bank account types
    $query = "SELECT DISTINCT account_type FROM tbl_bank_account_type WHERE account_type IS NOT NULL AND account_type != '' ORDER BY account_type";
    error_log("Executing query: " . $query);
    
    $stmt = $conn->prepare($query);
    $stmt->execute();
    
    // Fetch all account types
    $accountTypes = $stmt->fetchAll(PDO::FETCH_COLUMN);
    error_log("Fetched account types: " . print_r($accountTypes, true));
    
    // Ensure we have an array, even if empty
    if (!is_array($accountTypes)) {
        $accountTypes = [];
    }
    
    // Prepare response
    $response = [
        'success' => true,
        'message' => 'Bank account types fetched successfully',
        'data' => array_values($accountTypes) // Ensure sequential array
    ];
    
    // Debug output
    error_log("Response before JSON encoding: " . print_r($response, true));
    
    // Send response
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE);
    
    // Check for JSON encoding errors
    if ($jsonResponse === false) {
        throw new Exception('JSON encoding error: ' . json_last_error_msg());
    }
    
    // Debug output
    error_log("Final JSON response: " . $jsonResponse);
    
    // Ensure no output before headers
    if (ob_get_length()) ob_clean();
    
    // Send response
    echo $jsonResponse;
    exit();

} catch(PDOException $e) {
    // Handle database errors
    error_log("Database error: " . $e->getMessage());
    error_log("Error code: " . $e->getCode());
    
    $response = [
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage(),
        'data' => []
    ];
    
    http_response_code(500);
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit();
} catch(Exception $e) {
    // Handle other errors
    error_log("General error: " . $e->getMessage());
    error_log("Error trace: " . $e->getTraceAsString());
    
    $response = [
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'data' => []
    ];
    
    http_response_code(500);
    echo json_encode($response, JSON_UNESCAPED_UNICODE);
    exit();
} finally {
    // Close database connection
    if (isset($conn)) {
        $conn = null;
    }
}
?> 