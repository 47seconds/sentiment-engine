-- Create test user with known password (password123)
INSERT INTO users (email, name, password, role, is_active, created_at, updated_at) 
VALUES (
  'test@test.com', 
  'Test User', 
  '$2a$10$N9qo8uLOickgx2ZMRZoMye1J5Ul2hp6M9A1.F0Q8MRfOYWpuMnH2q', 
  'ADMIN', 
  true,
  NOW(),
  NOW()
) 
ON CONFLICT (email) DO UPDATE 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye1J5Ul2hp6M9A1.F0Q8MRfOYWpuMnH2q';

SELECT email, password, role FROM users WHERE email = 'test@test.com';
