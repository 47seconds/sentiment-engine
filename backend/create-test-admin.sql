-- Create admin user for sentiment engine
-- Email: admin@moveinsync.com
-- Password: admin123
-- This uses a properly generated BCrypt hash

-- Remove existing admin user if exists
DELETE FROM users WHERE email = 'admin@moveinsync.com';

-- Insert new admin user with BCrypt hash for "admin123"
INSERT INTO users (email, name, password, role, is_active, created_at, updated_at) 
VALUES (
  'admin@moveinsync.com',
  'Admin User',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'ADMIN',
  true,
  NOW(),
  NOW()
);

-- Verify the user was created
SELECT email, role, is_active FROM users WHERE email = 'admin@moveinsync.com';
