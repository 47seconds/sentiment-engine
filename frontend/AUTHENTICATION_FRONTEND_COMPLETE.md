# Frontend Login System - Complete! ✅

## What Was Built

### 1. Authentication Infrastructure
- ✅ **authService.js** - Login, register, logout, token management
- ✅ **AuthContext.jsx** - Global auth state with React Context
- ✅ **LoginPage.jsx** - Beautiful login UI with demo credentials
- ✅ **ProtectedRoute.jsx** - Route guards for authenticated/admin access

### 2. Updated Components
- ✅ **App.jsx** - Wrapped with AuthProvider, protected routes configured
- ✅ **Sidebar.jsx** - Added user profile display and logout button

---

## User Data Storage

### Backend Database (PostgreSQL)

**Table: `users`**
Location: `sentiment_db` database on port 5432

```sql
-- User table schema
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,                    -- Auto-increment user ID
    driver_id BIGINT UNIQUE,                     -- Driver ID (if user is a driver)
    name VARCHAR(100) NOT NULL,                  -- Full name
    email VARCHAR(150) NOT NULL UNIQUE,          -- Email (used for login)
    phone_number VARCHAR(20),                    -- Phone number
    password VARCHAR(255) NOT NULL,              -- BCrypt hashed password
    role VARCHAR(20) NOT NULL,                   -- DRIVER, ADMIN, SUPPORT, MANAGER, ANALYST
    is_active BOOLEAN DEFAULT TRUE,              -- Account active status
    profile_picture_url VARCHAR(255),            -- Profile picture URL
    license_number VARCHAR(50),                  -- Driver license (for drivers)
    vehicle_number VARCHAR(30),                  -- Vehicle number (for drivers)
    last_login_at TIMESTAMP,                     -- Last login timestamp
    password_changed_at TIMESTAMP,               -- Password change timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Additional table for user permissions
CREATE TABLE user_permissions (
    user_id BIGINT REFERENCES users(id),
    permission VARCHAR(100)
);
```

**Existing Users in Database:**

Based on the seed data, these users already exist:

1. **Admin User**
   - Email: `admin@moveinsync.com`
   - Password: `admin123` (hashed with BCrypt when registered)
   - Role: `ADMIN`
   - Name: Admin User

2. **Manager User**
   - Email: `manager@moveinsync.com`
   - Password: `manager123`
   - Role: `MANAGER`
   - Name: Manager User

3. **Driver Users (7 total)**
   - `john.driver@moveinsync.com` / `password123` (John Smith)
   - `mary.driver@moveinsync.com` / `password123` (Mary Johnson)
   - `james.driver@moveinsync.com` / `password123` (James Williams)
   - `sarah.driver@moveinsync.com` / `password123` (Sarah Brown)
   - `michael.driver@moveinsync.com` / `password123` (Michael Jones)
   - `emma.driver@moveinsync.com` / `password123` (Emma Davis)
   - `oliver.driver@moveinsync.com` / `password123` (Oliver Wilson)

**Note:** Passwords are stored as BCrypt hashes (60 characters), NOT plain text!

---

### Frontend Storage (Browser)

**LocalStorage:**
- **Key:** `jwt_token`
  - **Value:** JWT token (e.g., `eyJhbGciOiJIUzUxMiJ9...`)
  - **Expires:** 24 hours from login
  - **Purpose:** Sent with every API request in `Authorization: Bearer <token>` header

- **Key:** `user_info`
  - **Value:** JSON object with user details
  - **Example:**
    ```json
    {
      "id": 1,
      "name": "Admin User",
      "email": "admin@moveinsync.com",
      "role": "ADMIN",
      "driverId": null,
      "isActive": true
    }
    ```
  - **Purpose:** Display user info in UI without API calls

**Session Flow:**
1. User enters email/password in LoginPage
2. Frontend sends `POST /api/auth/login` with credentials
3. Backend validates password (BCrypt compare)
4. Backend generates JWT token (signed with secret key)
5. Frontend stores token + user info in localStorage
6. Frontend sets token in Axios headers: `Authorization: Bearer <token>`
7. All subsequent API requests include this token
8. Backend JwtAuthenticationFilter validates token on each request
9. On logout: localStorage cleared, token removed from headers

---

## Database Connection Details

**How to View User Data:**

### Option 1: Using Docker Exec
```bash
# Connect to PostgreSQL container
docker exec -it sentiment-postgres psql -U sentiment_user -d sentiment_db

# List all users
SELECT id, name, email, role, driver_id, is_active, created_at FROM users;

# Check specific user
SELECT * FROM users WHERE email = 'admin@moveinsync.com';

# View user permissions
SELECT u.name, u.email, u.role, p.permission 
FROM users u 
LEFT JOIN user_permissions p ON u.id = p.user_id;

# Exit
\q
```

### Option 2: Using pgAdmin / DBeaver
- **Host:** localhost
- **Port:** 5432
- **Database:** sentiment_db
- **Username:** sentiment_user
- **Password:** sentiment_password

---

## How Login Works (Technical Flow)

### 1. User Submits Login Form
```javascript
// LoginPage.jsx
const handleSubmit = async (e) => {
    e.preventDefault();
    await login(email, password);  // Calls AuthContext
};
```

### 2. AuthContext Calls AuthService
```javascript
// AuthContext.jsx
const login = async (email, password) => {
    const user = await authService.login(email, password);
    setUser(user);  // Update global state
    return user;
};
```

### 3. AuthService Makes API Call
```javascript
// authService.js
async login(email, password) {
    const response = await api.post('/auth/login', { email, password });
    const { token, user } = response.data.data;
    
    // Store in localStorage
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    
    // Set in Axios headers
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    
    return user;
}
```

### 4. Backend Validates Credentials
```java
// AuthController.java
@PostMapping("/login")
public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    // 1. Find user by email
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    
    // 2. Verify password (BCrypt compare)
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()  // Plain text password
        )
    );
    // BCrypt.checkpw() happens inside authenticationManager
    
    // 3. Generate JWT token
    String token = jwtTokenProvider.generateToken(authentication);
    
    // 4. Return token + user info
    return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
}
```

### 5. JWT Token Generation
```java
// JwtTokenProvider.java
public String generateToken(Authentication authentication) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + 86400000); // 24 hours
    
    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    
    return Jwts.builder()
        .setSubject(email)           // User email
        .setIssuedAt(now)            // Current time
        .setExpiration(expiryDate)   // 24 hours from now
        .signWith(key, SignatureAlgorithm.HS512)  // HMAC SHA-512
        .compact();
}
```

### 6. Frontend Redirects Based on Role
```javascript
// LoginPage.jsx
await login(email, password);

// Redirect based on role
if (isAdmin()) {
    navigate('/dashboard');  // Admin sees analytics
} else {
    navigate('/feedback');   // Employee sees feedback form
}
```

### 7. Protected Routes Guard Access
```javascript
// ProtectedRoute.jsx
if (!isAuthenticated) {
    return <Navigate to="/login" replace />;  // Not logged in
}

if (adminOnly && !isAdmin()) {
    return <Navigate to="/feedback" replace />;  // Not admin
}

return children;  // Allowed!
```

### 8. Subsequent API Requests Include Token
```javascript
// api.js (Axios interceptor)
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
```

### 9. Backend Validates Token on Each Request
```java
// JwtAuthenticationFilter.java
protected void doFilterInternal(HttpServletRequest request, ...) {
    // 1. Extract token from Authorization header
    String jwt = getJwtFromRequest(request);  // "Bearer eyJhbGc..."
    
    // 2. Validate token signature and expiration
    if (tokenProvider.validateToken(jwt)) {
        // 3. Extract email from token
        String email = tokenProvider.getEmailFromToken(jwt);
        
        // 4. Load user from database
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        
        // 5. Set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
```

---

## Testing the Login System

### Test 1: Admin Login
1. Start backend: `java -jar backend/target/sentiment-engine-1.0.0.jar`
2. Start frontend: Already running on port 3001
3. Navigate to: `http://localhost:3001/login`
4. Enter credentials:
   - Email: `admin@moveinsync.com`
   - Password: `admin123`
5. Click "Sign In"
6. Should redirect to `/dashboard`
7. Check localStorage in DevTools → Application:
   - `jwt_token`: Long encoded string
   - `user_info`: `{"id":1,"name":"Admin User","role":"ADMIN",...}`

### Test 2: Employee Login
1. Navigate to: `http://localhost:3001/login`
2. Enter credentials:
   - Email: `john.driver@moveinsync.com`
   - Password: `password123`
3. Click "Sign In"
4. Should redirect to `/feedback`
5. Try navigating to `/dashboard` → redirects back to `/feedback` (not admin!)

### Test 3: Invalid Credentials
1. Navigate to: `http://localhost:3001/login`
2. Enter: `wrong@email.com` / `wrongpassword`
3. Should show error: "Invalid email or password"

### Test 4: Logout
1. Login as any user
2. Click "Logout" button in sidebar (bottom)
3. Should redirect to `/login`
4. Check localStorage → both keys removed
5. Try accessing `/dashboard` → redirects to `/login`

### Test 5: Token Persistence
1. Login as admin
2. Refresh page (F5)
3. Should stay logged in (token read from localStorage)
4. Should still show admin dashboard

---

## Password Security

**How Passwords Are Secured:**

1. **Registration:**
   ```java
   // User enters: "password123"
   String plainPassword = "password123";
   
   // BCrypt hashes it (10 rounds, random salt)
   String hashedPassword = passwordEncoder.encode(plainPassword);
   // Result: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
   
   // Store ONLY the hash in database
   user.setPassword(hashedPassword);
   userRepository.save(user);
   ```

2. **Login:**
   ```java
   // User enters: "password123"
   String enteredPassword = "password123";
   
   // Get stored hash from database
   String storedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye...";
   
   // BCrypt compares them
   boolean matches = BCrypt.checkpw(enteredPassword, storedHash);
   // Returns true if match, false otherwise
   
   if (!matches) {
       throw new BadCredentialsException("Invalid password");
   }
   ```

**Why BCrypt?**
- ✅ Each password gets a unique random salt
- ✅ Computationally expensive (slow brute-force attacks)
- ✅ Future-proof (can increase rounds as hardware improves)
- ✅ Even same password produces different hashes:
  ```
  "password123" → "$2a$10$abc..." (user 1)
  "password123" → "$2a$10$xyz..." (user 2)
  ```

---

## Quick Reference

### Existing Users for Testing

| Email | Password | Role | Access Level |
|-------|----------|------|--------------|
| `admin@moveinsync.com` | `admin123` | ADMIN | Full access (Dashboard, Drivers, Alerts, Admin) |
| `manager@moveinsync.com` | `manager123` | MANAGER | Full access (same as ADMIN) |
| `john.driver@moveinsync.com` | `password123` | DRIVER | Limited (Feedback, Profile, Settings only) |
| `mary.driver@moveinsync.com` | `password123` | DRIVER | Limited |

### API Endpoints

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/auth/login` | POST | No | User login |
| `/api/auth/register` | POST | No | User registration |
| `/api/auth/me` | GET | Yes | Get current user |
| `/api/auth/logout` | POST | Yes | Logout (client-side) |

### Frontend Routes

| Route | Auth Required | Admin Only | Description |
|-------|---------------|------------|-------------|
| `/login` | No | No | Login page |
| `/dashboard` | Yes | Yes | Analytics dashboard |
| `/drivers` | Yes | Yes | Driver management |
| `/alerts` | Yes | Yes | Alert management |
| `/admin` | Yes | Yes | Admin settings |
| `/feedback` | Yes | No | Submit feedback |
| `/profile` | Yes | No | User profile |
| `/settings` | Yes | No | User settings |

---

## System is Ready! 🚀

The authentication system is complete and integrated. Users can:
- ✅ Login with email/password
- ✅ See role-appropriate pages (admin vs employee)
- ✅ Stay logged in across page refreshes
- ✅ Logout securely

**All user data is stored in PostgreSQL database with BCrypt password encryption!**
