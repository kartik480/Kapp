<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Initialize database connection
try {
    require_once 'db_config.php';
    
    // Check if $conn is set and valid
    if (!isset($conn) || !($conn instanceof PDO)) {
        throw new Exception('Database connection not properly initialized');
    }
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database connection error: ' . $e->getMessage()
    ]);
    exit;
}

try {
    // Get mobile number from request - check both GET and POST
    $mobile = null;
    
    // Check GET parameters
    if (isset($_GET['mobile_number'])) {
        $mobile = $_GET['mobile_number'];
    }
    // Check POST parameters
    else if (isset($_POST['mobile_number'])) {
        $mobile = $_POST['mobile_number'];
    }
    // Check raw input for JSON
    else {
        $input = file_get_contents('php://input');
        if (!empty($input)) {
            $data = json_decode($input, true);
            if (isset($data['mobile_number'])) {
                $mobile = $data['mobile_number'];
            }
        }
    }

    // Debug log
    error_log("Received mobile parameter: " . print_r($mobile, true));
    error_log("GET parameters: " . print_r($_GET, true));
    error_log("POST parameters: " . print_r($_POST, true));

    if (!$mobile) {
        echo json_encode([
            'success' => false,
            'message' => 'Mobile number is required',
            'debug' => [
                'get' => $_GET,
                'post' => $_POST,
                'input' => file_get_contents('php://input')
            ]
        ]);
        exit;
    }

    // Query to get appointments for the given mobile number and customer type
    $query = "SELECT 
        a.id,
        a.database_id,
        a.unique_id,
        a.mobile_number,
        a.lead_name,
        a.email_id,
        a.company_name,
        a.alternative_mobile,
        a.state,
        a.location,
        a.sub_location,
        a.pin_code,
        a.source,
        a.visiting_card,
        a.user_qualification,
        a.residental_address,
        a.customer_type
    FROM tbl_appointment a
    WHERE a.mobile_number = :mobile 
    AND (a.customer_type = 'NRI' OR a.customer_type = 'nri' OR a.customer_type LIKE '%NRI%')
    ORDER BY a.id DESC";

    $stmt = $conn->prepare($query);
    $stmt->execute(['mobile' => $mobile]);
    $appointments = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Format the data vertically
    function formatDataVertically($appointments) {
        $formatted = [];
        foreach ($appointments as $appointment) {
            $formatted[] = [
                'id' => $appointment['id'],
                'database_id' => $appointment['database_id'],
                'unique_id' => $appointment['unique_id'],
                'mobile_number' => $appointment['mobile_number'],
                'lead_name' => $appointment['lead_name'],
                'email_id' => $appointment['email_id'],
                'company_name' => $appointment['company_name'],
                'alternative_mobile' => $appointment['alternative_mobile'],
                'state' => $appointment['state'],
                'location' => $appointment['location'],
                'sub_location' => $appointment['sub_location'],
                'pin_code' => $appointment['pin_code'],
                'source' => $appointment['source'],
                'visiting_card' => $appointment['visiting_card'],
                'user_qualification' => $appointment['user_qualification'],
                'residental_address' => $appointment['residental_address'],
                'customer_type' => $appointment['customer_type']
            ];
        }
        return $formatted;
    }

    if (count($appointments) > 0) {
        $verticalAppointments = formatDataVertically($appointments);
        echo json_encode([
            'success' => true,
            'appointments' => $verticalAppointments
        ]);
    } else {
        echo json_encode([
            'success' => true,
            'appointments' => [],
            'message' => 'No appointments found for this mobile number'
        ]);
    }

} catch (PDOException $e) {
    error_log("Database error: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    error_log("General error: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?> 