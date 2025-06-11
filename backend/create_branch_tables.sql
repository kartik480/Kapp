-- Create branch state table
CREATE TABLE IF NOT EXISTS tbl_branch_state (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_state_name VARCHAR(100) NOT NULL
);

-- Create branch location table
CREATE TABLE IF NOT EXISTS tbl_branch_location (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_location VARCHAR(100) NOT NULL
);

-- Insert sample states
INSERT INTO tbl_branch_state (branch_state_name) VALUES
('Andhra Pradesh'),
('Telangana'),
('Karnataka'),
('Tamil Nadu'),
('Kerala');

-- Insert sample locations
INSERT INTO tbl_branch_location (branch_location) VALUES
('Hyderabad'),
('Bangalore'),
('Chennai'),
('Kochi'),
('Vijayawada'); 