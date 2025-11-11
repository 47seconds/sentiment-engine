-- Create a fresh test admin user with NEW password hash
-- Password will be: "Test@1234"
-- Hash generated fresh: $2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRwwX5UQkGYzW8J4dGbWkb5QCTZzO

DELETE FROM users WHERE email = 'testadmin@test.com';

INSERT INTO users (email, name, password, phone_number, role, is_active, created_at, updated_at) 
VALUES (
  'testadmin@test.com',
  'Test Admin',
  '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRwwX5UQkGYzW8J4dGbWkb5QCTZzO',
  '1234567890',
  'ADMIN',
  true,
  NOW(),
  NOW()
);

SELECT email, password, role, is_active FROM users WHERE email = 'testadmin@test.com';
