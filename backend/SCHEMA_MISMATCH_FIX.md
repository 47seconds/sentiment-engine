# Schema Mismatch Fix - User Role Enum

## Issue
User creation endpoint was failing with error:
```
Cannot deserialize value of type `com.moveinsync.sentiment.model.User$UserRole` 
from String "EMPLOYEE": not one of the values accepted for Enum class: 
[DRIVER, ANALYST, MANAGER, SUPPORT, ADMIN]
```

## Root Cause
**Database vs Java Mismatch:**
- Database CHECK constraint allowed: `EMPLOYEE`, `ADMIN`, `SUPPORT`, `MANAGER`, `ANALYST`
- Java User enum had: `DRIVER`, `ADMIN`, `SUPPORT`, `MANAGER`, `ANALYST`

The Java code used `DRIVER` while the database schema required `EMPLOYEE`.

## Files Fixed

### 1. User.java
- Changed enum: `DRIVER` → `EMPLOYEE`
- Updated default role: `UserRole.DRIVER` → `UserRole.EMPLOYEE`
- Updated `isDriver()` method to check for `EMPLOYEE` role

### 2. AuthController.java
- Updated default registration role: `DRIVER` → `EMPLOYEE`

### 3. RegisterRequest.java
- Updated default role: `DRIVER` → `EMPLOYEE`

### 4. UserController.java
- Updated driver query to use `EMPLOYEE` role

### 5. UserRepository.java
- Updated JPQL queries to use `'EMPLOYEE'` instead of `'DRIVER'`

### 6. UserService.java
- Updated driver count query to use `EMPLOYEE` role

## Additional Fixes

### BCrypt Password Hashes
**Issue:** The BCrypt hashes in `seed-data.sql` were incorrect and didn't match the claimed passwords.

**Solution:** 
- Deleted old admin/manager users
- Recreated them using the `/api/auth/register` endpoint
- This ensures passwords are hashed correctly by the backend's BCryptPasswordEncoder

**Current Credentials:**
- Admin: `admin@moveinsync.com` / `admin123`
- Manager: `manager@moveinsync.com` / `manager123`

## Testing Completed ✓

1. ✅ Admin login successful
2. ✅ User creation endpoint working (`POST /api/users`)
3. ✅ Created user can login successfully
4. ✅ Role validation working (EMPLOYEE, ADMIN, MANAGER, SUPPORT, ANALYST)

## API Endpoints Verified

### Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@moveinsync.com",
  "password": "admin123"
}
```

### Create User (Admin Only)
```bash
POST http://localhost:8080/api/users
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "email": "newuser@test.com",
  "password": "Password123!",
  "name": "New User",
  "phoneNumber": "1234567890",
  "role": "EMPLOYEE"
}
```

## Date Fixed
November 10, 2025
