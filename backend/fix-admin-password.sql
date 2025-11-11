-- Fix admin password with correct BCrypt hash for "admin123"
UPDATE users 
SET password = '$2a$10$8kPbHFn5sGfU5Vwp8QpMTegQbODcVfSV2xVcMWw.hZAL9GVbL0g76' 
WHERE email = 'admin@moveinsync.com';

SELECT email, password, role, is_active 
FROM users 
WHERE email = 'admin@moveinsync.com';
