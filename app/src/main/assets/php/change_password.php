<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db_config.php';

// Get POST data
$user_id = $_POST['user_id'] ?? null;
$current_password = $_POST['current_password'] ?? null;
$new_password = $_POST['new_password'] ?? null;

// Validate required fields
if (!$user_id || !$current_password || !$new_password) {
    echo json_encode([
        'success' => false,
        'message' => 'Missing required fields'
    ]);
    exit();
}

try {
    // Create connection
    $conn = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // First, verify the current password
    $stmt = $conn->prepare("SELECT password FROM tbl_user WHERE id = ?");
    $stmt->execute([$user_id]);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$result) {
        throw new Exception("User not found");
    }

    if ($result['password'] !== $current_password) {
        throw new Exception("Current password is incorrect");
    }

    // Update the password
    $stmt = $conn->prepare("UPDATE tbl_user SET password = ? WHERE id = ?");
    $stmt->execute([$new_password, $user_id]);
    
    echo json_encode([
        'success' => true,
        'message' => 'Password changed successfully'
    ]);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}
?> 