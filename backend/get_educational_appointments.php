<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'db_config.php';

try {
    // Get mobile number from request
    $mobile = isset($_POST['mobile_number']) ? $_POST['mobile_number'] : null;

    if (!$mobile) {
        echo json_encode([
            'success' => false,
            'message' => 'Mobile number is required'
        ]);
        exit;
    }

    // Query to get educational appointments
    $query = "SELECT * FROM tbl_appointment 
              WHERE mobile_number = :mobile 
              AND customer_type LIKE '%Educational%'
              ORDER BY id DESC";

    $stmt = $conn->prepare($query);
    $stmt->execute(['mobile' => $mobile]);
    $appointments = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (count($appointments) > 0) {
        echo json_encode([
            'success' => true,
            'appointments' => $appointments
        ]);
    } else {
        echo json_encode([
            'success' => true,
            'appointments' => [],
            'message' => 'No educational appointments found for this mobile number'
        ]);
    }

} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?> 