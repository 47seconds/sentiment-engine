-- Use the CORRECT hash from seed-data.sql for admin123
UPDATE users 
SET password = '$2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu' 
WHERE email = 'admin@moveinsync.com';

UPDATE users 
SET password = '$2a$10$8IaYq8uT5TadBDWzxub6MOcY9hTCSpuP0oQbZLUqKpVwog7GToCCu' 
WHERE email = 'manager@moveinsync.com';

SELECT email, password, role FROM users WHERE email IN ('admin@moveinsync.com', 'manager@moveinsync.com');
