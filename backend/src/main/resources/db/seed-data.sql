-- Seed Data for Testing - Minimal Version
-- Only insert users, let the application create driver_stats when needed  
-- Passwords are BCrypt-hashed (strength 10) - VERIFIED WORKING HASHES
-- password123 -> $2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW
-- admin123 -> $2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu

-- Insert Users (including employees - note: role is EMPLOYEE, not DRIVER)
INSERT INTO users (email, name, role, is_active, password, created_at, phone_number) 
VALUES 
    -- Demo Drivers for Testing
    ('rajesh.kumar@moveinsync.com', 'Rajesh Kumar', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), '+91-9876501234'),
    ('priya.sharma@moveinsync.com', 'Priya Sharma', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), '+91-9876501235'),
    ('amit.patel@moveinsync.com', 'Amit Patel', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), '+91-9876501236'),
    ('sunita.singh@moveinsync.com', 'Sunita Singh', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), '+91-9876501237'),
    ('vikram.reddy@moveinsync.com', 'Vikram Reddy', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), '+91-9876501238'),
    -- Other demo employees
    ('john.employee@moveinsync.com', 'John Smith', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    ('mary.employee@moveinsync.com', 'Mary Johnson', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    ('james.employee@moveinsync.com', 'James Williams', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    ('sarah.employee@moveinsync.com', 'Sarah Brown', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    ('michael.employee@moveinsync.com', 'Michael Davis', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    ('emma.employee@moveinsync.com', 'Emma Wilson', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    ('oliver.employee@moveinsync.com', 'Oliver Martinez', 'EMPLOYEE', true, '$2a$10$3pr7kG4SqQkq7VkSP6NY0Oj3B8WRRPaZ2Kp6RQ7SdY8nCwN41iWoW', NOW(), NULL),
    -- Admin users
    ('admin@moveinsync.com', 'Admin User', 'ADMIN', true, '$2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu', NOW(), NULL),
    ('manager@moveinsync.com', 'Manager User', 'MANAGER', true, '$2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu', NOW(), NULL)
ON CONFLICT (email) DO NOTHING;

SELECT 'Successfully created ' || COUNT(*) || ' users' FROM users;

