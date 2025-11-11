-- Update admin to use a simple password we can test
-- Password: Password123!
-- Hash generated from trusted source: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
WHERE email = 'admin@moveinsync.com';

SELECT email, password, role FROM users WHERE email = 'admin@moveinsync.com';
