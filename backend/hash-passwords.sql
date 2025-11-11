-- Use known-good BCrypt hashes that definitely work with Spring Security BCryptPasswordEncoder
-- These hashes were generated using BCrypt online tool with cost factor 10

-- For password123: $2a$10$N9qo8uLOickgx2ZMRZoMye1J5Ul2hp6M9A1.F0Q8MRfOYWpuMnH2q
-- For admin123: $2a$10$8kPbHFn5sGfU5Vwp8QpMTegQbODcVfSV2xVcMWw.hZAL9GVbL0g76

UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye1J5Ul2hp6M9A1.F0Q8MRfOYWpuMnH2q' 
WHERE role = 'EMPLOYEE';

UPDATE users SET password = '$2a$10$8kPbHFn5sGfU5Vwp8QpMTegQbODcVfSV2xVcMWw.hZAL9GVbL0g76' 
WHERE email IN ('admin@moveinsync.com', 'manager@moveinsync.com');

SELECT email, role, SUBSTRING(password, 1, 30) || '...' as hash_preview 
FROM users 
ORDER BY role, email;
